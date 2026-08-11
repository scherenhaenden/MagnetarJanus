package com.magnetar.janus

import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.magnetar.janus.data.MediaSplitter
import com.magnetar.janus.data.SplitRequest
import com.magnetar.janus.model.Segment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class RealVideoSplitInstrumentedTest {
    @Test
    fun realH264AacVideosSplitWithBothTracks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val inputs = listOf("problem-video.mp4", "youtube-video.mp4")
        inputs.forEach { inputName ->
            val input = File("/sdcard/Download/$inputName")
            assumeTrue("Push $inputName to /sdcard/Download first", input.exists())
            val sandboxInput = File(context.cacheDir, inputName)
            automation.adoptShellPermissionIdentity()
            try {
                FileInputStream(input).use { source ->
                    FileOutputStream(sandboxInput).use { target -> source.copyTo(target) }
                }
            } finally {
                automation.dropShellPermissionIdentity()
            }
            val outputDirectory = File(context.cacheDir, "instrumented-splits/$inputName").apply { mkdirs() }
            val segments = listOf(Segment(0, 90), Segment(90, 180))

            segments.forEachIndexed { index, segment ->
                val output = File(outputDirectory, "part-$index.mp4")
                val result =
                    MediaSplitter(context).split(
                        SplitRequest(Uri.fromFile(sandboxInput), Uri.fromFile(output), segment),
                    )
                assertTrue("$inputName split $index failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
                assertTrue("$inputName split $index is empty", output.length() > 0)
                val extractor = MediaExtractor()
                try {
                    extractor.setDataSource(output.absolutePath)
                    val codecs =
                        (0 until extractor.trackCount)
                            .map { track ->
                                extractor.getTrackFormat(track).getString(MediaFormat.KEY_MIME)
                            }.toSet()
                    assertEquals("$inputName split $index should contain video and audio", setOf("video/avc", "audio/mp4a-latm"), codecs)
                    codecs.forEach { codec ->
                        val track =
                            (0 until extractor.trackCount).first {
                                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME) ==
                                    codec
                            }
                        assertTrue(
                            "$inputName split $index $codec should have duration",
                            extractor.getTrackFormat(track).getLong(MediaFormat.KEY_DURATION) > 0,
                        )
                    }
                } finally {
                    extractor.release()
                    output.delete()
                }
            }
            outputDirectory.delete()
            sandboxInput.delete()
        }
    }
}
