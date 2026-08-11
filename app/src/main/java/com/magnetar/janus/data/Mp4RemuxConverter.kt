package com.magnetar.janus.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.nio.ByteBuffer

data class ConversionRequest(val input: Uri, val output: Uri)

interface MediaConverter {
    fun convert(request: ConversionRequest): Result<Unit>
}

/** Remuxes compatible audio/video tracks into an MP4 container without re-encoding. */
class Mp4RemuxConverter(private val context: Context) : MediaConverter {
    override fun convert(request: ConversionRequest): Result<Unit> = runCatching {
        val extractor = MediaExtractor()
        val input = openDescriptor(context, request.input, "r") ?: error("Unable to open input media")
        val output = openDescriptor(context, request.output, "w") ?: error("Unable to open output destination")
        try {
            extractor.setDataSource(input.fileDescriptor)
            val muxer = MediaMuxer(output.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            try {
                val trackMap = IntArray(extractor.trackCount) { -1 }
                for (trackIndex in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(trackIndex)
                    val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                    if (mime.startsWith("audio/") || mime.startsWith("video/")) trackMap[trackIndex] = muxer.addTrack(format)
                }
                check(trackMap.any { it >= 0 }) { "No compatible audio or video tracks found" }
                muxer.start()
                val buffer = ByteBuffer.allocate(1024 * 1024)
                val info = MediaCodec.BufferInfo()
                for (trackIndex in 0 until extractor.trackCount) {
                    if (trackMap[trackIndex] < 0) continue
                    extractor.selectTrack(trackIndex)
                    while (true) {
                        info.offset = 0
                        info.size = extractor.readSampleData(buffer, 0)
                        if (info.size < 0) break
                        info.presentationTimeUs = extractor.sampleTime
                        info.flags = extractor.sampleFlags.toBufferFlags()
                        muxer.writeSampleData(trackMap[trackIndex], buffer, info)
                        extractor.advance()
                    }
                    extractor.unselectTrack(trackIndex)
                }
                muxer.stop()
            } finally {
                muxer.release()
            }
        } finally {
            extractor.release()
            input.close()
            output.close()
        }
    }
}

private fun openDescriptor(context: Context, uri: Uri, mode: String): ParcelFileDescriptor? =
    if (uri.scheme == "file") {
        ParcelFileDescriptor.open(File(requireNotNull(uri.path)), if (mode == "r") ParcelFileDescriptor.MODE_READ_ONLY else ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE)
    } else {
        context.contentResolver.openFileDescriptor(uri, mode)
    }

private fun Int.toBufferFlags(): Int {
    var flags = 0
    if (this and MediaExtractor.SAMPLE_FLAG_SYNC != 0) flags = flags or MediaCodec.BUFFER_FLAG_KEY_FRAME
    if (this and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME != 0) flags = flags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
    return flags
}
