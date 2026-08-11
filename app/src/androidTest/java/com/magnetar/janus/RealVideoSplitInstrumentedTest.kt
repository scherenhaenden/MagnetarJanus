package com.magnetar.janus

import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.magnetar.janus.data.MediaSplitter
import com.magnetar.janus.data.SplitRequest
import com.magnetar.janus.model.Segment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class RealVideoSplitInstrumentedTest {
    @Test
    fun realH264AacVideoSplitsWithBothTracks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val input = File("/sdcard/Download/problem-video.mp4")
        assumeTrue("Push problem-video.mp4 to /sdcard/Download first", input.exists())
        val sandboxInput = File(context.cacheDir, "problem-video.mp4")
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.adoptShellPermissionIdentity()
        try {
            FileInputStream(input).use { source ->
                FileOutputStream(sandboxInput).use { target -> source.copyTo(target) }
            }
        } finally {
            automation.dropShellPermissionIdentity()
        }
        val outputDirectory = File(context.cacheDir, "instrumented-splits").apply { mkdirs() }
        val segments = listOf(Segment(0, 90), Segment(90, 180))

        segments.forEachIndexed { index, segment ->
            val output = File(outputDirectory, "part-$index.mp4")
            val result =
                MediaSplitter(context).split(
                    SplitRequest(Uri.fromFile(sandboxInput), Uri.fromFile(output), segment),
                )
            assertTrue("split $index failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
            assertTrue("split $index is empty", output.length() > 0)
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(output.absolutePath)
                assertEquals("split $index should contain video and audio", 2, extractor.trackCount)
                assertTrue("split $index should have duration", extractor.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION) > 0)
            } finally {
                extractor.release()
                output.delete()
            }
        }
        outputDirectory.delete()
        sandboxInput.delete()
    }
}
