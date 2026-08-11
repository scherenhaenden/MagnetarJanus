package com.magnetar.janus.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
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
            val muxer = MediaMuxer(output.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            try {
                val tracks = IntArray(extractor.trackCount) { -1 }
                for (index in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(index)
                    if (includeTrack(index, format)) tracks[index] = muxer.addTrack(format)
                }
                check(tracks.any { it >= 0 }) { "No compatible media tracks found" }
                muxer.start()
                val selectedFormats = tracks.indices.mapNotNull { index -> extractor.getTrackFormat(index).takeIf { tracks[index] >= 0 } }
                val buffer = ByteBuffer.allocate(bufferCapacity(selectedFormats))
                val info = MediaCodec.BufferInfo()
                for (index in tracks.indices) {
                    if (tracks[index] < 0) continue
                    extractor.selectTrack(index)
                    if (startUs > 0) extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                    var baseUs = -1L
                    while (true) {
                        if (isCancelled()) throw CancellationException("Operation cancelled")
                        val timestamp = extractor.sampleTime
                        if (timestamp < 0) break
                        if (timestamp < startUs) {
                            extractor.advance()
                            continue
                        }
                        if (endUs != null && timestamp >= endUs) break
                        if (baseUs < 0) baseUs = timestamp
                        info.offset = 0
                        info.size = extractor.readSampleData(buffer, 0)
                        if (info.size < 0) break
                        info.presentationTimeUs = timestamp - baseUs
                        info.flags = extractor.sampleFlags.toBufferFlags()
                        muxer.writeSampleData(tracks[index], buffer, info)
                        extractor.advance()
                    }
                    extractor.unselectTrack(index)
                }
                muxer.stop()
            } finally {
                muxer.release()
            }
            val verification = requireNotNull(openDescriptor(context, outputUri, "r")) { "Output validation failed" }
            check(verification.statSize > 0) { "Output validation failed" }
            verification.close()
        } finally {
            extractor.release()
            input.close()
            output.close()
        }
    }
}
