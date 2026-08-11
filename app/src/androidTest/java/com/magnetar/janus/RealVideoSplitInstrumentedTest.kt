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
                    assertEquals("$inputName split $index should contain video and audio", 2, extractor.trackCount)
                    assertTrue("$inputName split $index should have duration", extractor.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION) > 0)
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
