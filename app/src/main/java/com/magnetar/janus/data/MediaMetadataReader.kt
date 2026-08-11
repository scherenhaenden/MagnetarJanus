package com.magnetar.janus.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.magnetar.janus.model.MediaInfo
import com.magnetar.janus.model.MediaKind
import java.io.File

class MediaMetadataReader(private val context: Context) {
    fun read(uri: Uri): Result<MediaInfo> = runCatching {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty()
        require(mime.startsWith("audio/") || mime.startsWith("video/")) { "Only audio and video files are supported" }
        val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uri.lastPathSegment ?: "Selected media"
        val size = resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
        }
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.div(1000) ?: 0L
            require(duration > 0) { "The selected media has no readable duration" }
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()?.let { "${it / 1000} kbps" }
            val kind = if (mime.startsWith("audio/")) MediaKind.AUDIO else MediaKind.VIDEO
            val previewPath = if (kind == MediaKind.VIDEO) {
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let { bitmap ->
                    val file = File(context.cacheDir, "janus-preview-${uri.toString().hashCode()}.jpg")
                    file.outputStream().use { stream -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 82, stream) }
                    bitmap.recycle()
                    file.absolutePath
                }
            } else null
            MediaInfo(
                name = name,
                kind = kind,
                durationSeconds = duration,
                sourceUri = uri.toString(),
                previewPath = previewPath,
                sizeBytes = size,
                resolution = if (width != null && height != null && width > 0 && height > 0) "${width}×${height}" else null,
                frameRate = frameRate?.takeIf { it.isNotBlank() }?.let { "$it fps" },
                bitrate = bitrate,
                codec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.substringAfter('/')?.uppercase(),
                container = name.substringAfterLast('.', "").uppercase().ifBlank { null }
            )
        } finally {
            retriever.release()
        }
    }
}
