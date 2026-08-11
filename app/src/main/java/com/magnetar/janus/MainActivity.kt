@file:Suppress("ktlint:standard:function-naming")

package com.magnetar.janus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.magnetar.janus.data.AudioExtractionRequest
import com.magnetar.janus.data.AudioExtractor
import com.magnetar.janus.data.ConversionRequest
import com.magnetar.janus.data.JobHistoryStore
import com.magnetar.janus.data.JobKind
import com.magnetar.janus.data.JobState
import com.magnetar.janus.data.MediaMetadataReader
import com.magnetar.janus.data.MediaSplitter
import com.magnetar.janus.data.Mp4RemuxConverter
import com.magnetar.janus.data.SplitRequest
import com.magnetar.janus.model.MediaInfo
import com.magnetar.janus.model.Operation
import com.magnetar.janus.model.OutputNaming
import com.magnetar.janus.model.Segment
import com.magnetar.janus.ui.JanusApp
import com.magnetar.janus.ui.theme.MagnetarJanusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagnetarJanusTheme { MediaPickerApp() } }
    }
}

@Composable
private fun MediaPickerApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var media by remember { mutableStateOf<MediaInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var processingMessage by remember { mutableStateOf<String?>(null) }
    var processing by remember { mutableStateOf(false) }
    var pendingConversion by remember { mutableStateOf<MediaInfo?>(null) }
    var pendingOperation by remember { mutableStateOf(Operation.CONVERT) }
    val history = remember { JobHistoryStore(context) }
    val cancelSignal = remember { AtomicBoolean(false) }
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            scope.launch(Dispatchers.IO) {
                loading = true
                error = null
                processingMessage = null
                MediaMetadataReader(context).read(uri).onSuccess { media = it }.onFailure {
                    error =
                        it.message ?: "Unable to inspect the selected media"
                }
                loading = false
            }
        }
    val outputPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { outputUri ->
            val selected = pendingConversion ?: return@rememberLauncherForActivityResult
            if (outputUri == null || selected.sourceUri == null) {
                pendingConversion = null
                return@rememberLauncherForActivityResult
            }
            scope.launch(Dispatchers.IO) {
                processing = true
                processingMessage = "Converting to MP4… 0%"
                cancelSignal.set(false)
                val job =
                    com.magnetar.janus.data.MediaJob(
                        kind =
                            if (pendingOperation ==
                                Operation.AUDIO
                            ) {
                                JobKind.AUDIO
                            } else {
                                JobKind.CONVERT
                            },
                        inputName = selected.name,
                    )
                history.save(job)
                history.update(job.id, JobState.RUNNING)
                val result =
                    if (pendingOperation == Operation.AUDIO) {
                        AudioExtractor(context).extract(AudioExtractionRequest(selected.sourceUri.toUri(), outputUri, cancelSignal::get))
                    } else {
                        Mp4RemuxConverter(
                            context,
                        ).convert(ConversionRequest(selected.sourceUri.toUri(), outputUri, cancelSignal::get)) { progress ->
                            processingMessage = "Converting to MP4… ${(progress.fraction * 100).toInt()}%"
                        }
                    }
                processingMessage =
                    result.fold({
                        history.update(job.id, JobState.COMPLETE, outputPath = outputUri.toString())
                        "Conversion complete: ${selected.name.substringBeforeLast('.')}.mp4"
                    }, {
                        val cancelled = cancelSignal.get()
                        history.update(job.id, if (cancelled) JobState.CANCELLED else JobState.FAILED, it.message)
                        if (cancelled) "Conversion cancelled" else "Conversion failed: ${it.message ?: "unsupported media"}"
                    })
                processing = false
                pendingConversion = null
            }
        }
    JanusApp(
        media = media,
        loading = loading,
        processing = processing,
        errorMessage = error,
        processingMessage = processingMessage,
        onClearError = {
            error =
                null
        },
        onClearMedia = {
            media = null
            processingMessage = null
        },
        onCancelProcessing = {
            cancelSignal.set(true)
            processingMessage =
                "Cancelling…"
        },
        onSelectMedia = {
            picker.launch(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "audio/*"))
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                },
            )
        },
        onPrimaryAction = { operation, segments ->
            val selected = media
            if (operation == Operation.SPLIT && selected?.sourceUri != null) {
                scope.launch(Dispatchers.IO) {
                    processing = true
                    cancelSignal.set(false)
                    val job =
                        com.magnetar.janus.data
                            .MediaJob(kind = JobKind.SPLIT, inputName = selected.name)
                    history.save(job)
                    history.update(job.id, JobState.RUNNING)
                    val outputDirectory = File(context.filesDir, "outputs").apply { mkdirs() }
                    val results =
                        segments.mapIndexed { index, segment ->
                            val output = File(outputDirectory, OutputNaming.splitFileName(selected.name, index + 1))
                            MediaSplitter(
                                context,
                            ).split(SplitRequest(selected.sourceUri.toUri(), Uri.fromFile(output), segment, cancelSignal::get))
                        }
                    val failed = results.firstOrNull { it.isFailure }
                    if (failed == null) {
                        history.update(job.id, JobState.COMPLETE, "${results.size} segments")
                        processingMessage = "Split complete: ${results.size} files"
                    } else {
                        history.update(
                            job.id,
                            if (cancelSignal.get()) JobState.CANCELLED else JobState.FAILED,
                            failed.exceptionOrNull()?.message,
                        )
                        processingMessage = failed.exceptionOrNull()?.message ?: "Split failed"
                    }
                    processing = false
                }
            } else if (operation == Operation.CONVERT || operation == Operation.AUDIO) {
                if (selected == null || selected.sourceUri == null) {
                    error = "Select readable media before converting"
                } else {
                    pendingOperation = operation
                    pendingConversion = selected
                    outputPicker.launch("${selected.name.substringBeforeLast('.')}.${if (operation == Operation.AUDIO) "m4a" else "mp4"}")
                }
            } else if (selected == null || selected.sourceUri == null) {
                error = "Select readable media before converting"
            }
        },
    )
}
