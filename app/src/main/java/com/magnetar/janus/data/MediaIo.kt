package com.magnetar.janus.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

internal fun openDescriptor(
    context: Context,
    uri: Uri,
    mode: String,
): ParcelFileDescriptor? =
    if (uri.scheme == "file") {
        ParcelFileDescriptor.open(
            File(requireNotNull(uri.path)),
            if (mode == "r") {
                ParcelFileDescriptor.MODE_READ_ONLY
            } else {
                ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            },
        )
    } else {
        context.contentResolver.openFileDescriptor(uri, mode)
    }

internal fun Int.toBufferFlags(): Int {
    var flags = 0
    if (this and MediaExtractor.SAMPLE_FLAG_SYNC != 0) flags = flags or MediaCodec.BUFFER_FLAG_KEY_FRAME
    if (this and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME != 0) flags = flags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
    return flags
}

internal fun bufferCapacity(formats: List<MediaFormat>): Int =
    formats
        .maxOfOrNull { if (it.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) it.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) else 0 }
        ?.coerceAtLeast(4 * 1024 * 1024) ?: (4 * 1024 * 1024)

internal fun verifyMediaOutput(
    context: Context,
    uri: Uri,
) {
    val descriptor = requireNotNull(openDescriptor(context, uri, "r")) { "Output validation failed: file cannot be opened" }
    descriptor.use {
        check(it.statSize > 0) { "Output validation failed: file is empty" }
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(it.fileDescriptor)
            check(extractor.trackCount > 0) { "Output validation failed: no media tracks found" }
        } finally {
            extractor.release()
        }
    }
}
