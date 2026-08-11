package com.magnetar.janus.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import com.magnetar.janus.model.Segment
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException

data class AudioExtractionRequest(
    val input: Uri,
    val output: Uri,
    val isCancelled: () -> Boolean = { false },
)

data class SplitRequest(
    val input: Uri,
    val output: Uri,
    val segment: Segment,
    val isCancelled: () -> Boolean = { false },
)

/** Extracts compatible audio tracks into an audio-only MP4/M4A document without re-encoding. */
class AudioExtractor(
    private val context: Context,
) {
    fun extract(request: AudioExtractionRequest): Result<Unit> =
        runCatching {
            MediaTrackWriter(context).write(request.input, request.output, request.isCancelled) { _, format ->
                format.getString(MediaFormat.KEY_MIME).orEmpty().startsWith("audio/")
            }
        }
}

/** Writes one time-bounded MP4 segment, preserving compatible audio and video samples. */
class MediaSplitter(
    private val context: Context,
) {
    fun split(request: SplitRequest): Result<Unit> =
        runCatching {
            require(request.segment.startSeconds >= 0) { "Segment start must be non-negative" }
            require(request.segment.endSeconds > request.segment.startSeconds) { "Segment end must be after start" }
            MediaTrackWriter(context).write(
                request.input,
                request.output,
                request.isCancelled,
                startUs = request.segment.startSeconds * 1_000_000,
                endUs = request.segment.endSeconds * 1_000_000,
            ) { _, format ->
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                mime.startsWith("audio/") || mime.startsWith("video/")
            }
        }
}

private class MediaTrackWriter(
    private val context: Context,
) {
    private companion object {
        const val TAG = "Janus.MediaTrackWriter"
    }

    fun write(
        inputUri: Uri,
        outputUri: Uri,
        isCancelled: () -> Boolean,
        startUs: Long = 0L,
        endUs: Long? = null,
        includeTrack: (Int, MediaFormat) -> Boolean,
    ) {
        val input = openDescriptor(context, inputUri, "r") ?: error("Unable to open input media")
        val output = openDescriptor(context, outputUri, "rw") ?: error("Unable to open output destination")
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(input.fileDescriptor)
            Log.i(TAG, "input=$inputUri tracks=${extractor.trackCount} startUs=$startUs endUs=$endUs")
            val muxer = MediaMuxer(output.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            try {
                val tracks = IntArray(extractor.trackCount) { -1 }
                for (index in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(index)
                    val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else -1
                    val maxInput =
                        if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) else -1
                    Log.i(
                        TAG,
                        "track=$index mime=${format.getString(MediaFormat.KEY_MIME)} durationUs=$durationUs " +
                            "maxInput=$maxInput",
                    )
                    if (includeTrack(index, format)) {
                        requireMp4RemuxCodec(format)
                        tracks[index] = muxer.addTrack(format)
                    }
                }
                check(tracks.any { it >= 0 }) { "No compatible media tracks found" }
                muxer.start()
                val selectedFormats = tracks.indices.mapNotNull { index -> extractor.getTrackFormat(index).takeIf { tracks[index] >= 0 } }
                val buffer = ByteBuffer.allocate(bufferCapacity(selectedFormats))
                val info = MediaCodec.BufferInfo()
                val selected = tracks.indices.filter { tracks[it] >= 0 }
                selected.forEach { extractor.selectTrack(it) }
                if (startUs > 0) extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                val active = BooleanArray(tracks.size) { tracks[it] >= 0 }
                val baseUs = LongArray(tracks.size) { -1L }
                while (active.any { it }) {
                    if (isCancelled()) throw CancellationException("Operation cancelled")
                    val sourceTrack = extractor.sampleTrackIndex
                    if (sourceTrack < 0 || !active[sourceTrack]) break
                    val timestamp = extractor.sampleTime
                    if (timestamp < startUs) {
                        extractor.advance()
                        continue
                    }
                    if (endUs != null && timestamp >= endUs) {
                        extractor.unselectTrack(sourceTrack)
                        active[sourceTrack] = false
                        continue
                    }
                    if (baseUs[sourceTrack] < 0) baseUs[sourceTrack] = timestamp
                    info.offset = 0
                    info.size = extractor.readSampleData(buffer, 0)
                    if (info.size < 0) {
                        extractor.unselectTrack(sourceTrack)
                        active[sourceTrack] = false
                        continue
                    }
                    info.presentationTimeUs = timestamp - baseUs[sourceTrack]
                    info.flags = extractor.sampleFlags.toBufferFlags()
                    muxer.writeSampleData(tracks[sourceTrack], buffer, info)
                    extractor.advance()
                }
                selected.filter { active[it] }.forEach { extractor.unselectTrack(it) }
                selected.forEach { Log.i(TAG, "track=$it samplesWritten=true") }
                muxer.stop()
            } finally {
                muxer.release()
            }
            verifyMediaOutput(context, outputUri)
            Log.i(TAG, "verified output=$outputUri")
        } finally {
            extractor.release()
            input.close()
            output.close()
        }
    }
}
