package com.magnetar.janus.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.File

data class PendingSplitOutput(
    val uri: android.net.Uri,
    val location: String,
    val isMediaStoreItem: Boolean,
)

/** Creates user-visible split files under Movies/Magnetar Janus/Splits. */
object PublicSplitOutput {
    fun create(
        context: Context,
        displayName: String,
        directoryName: String,
    ): PendingSplitOutput {
        val location = "${Environment.DIRECTORY_MOVIES}/Magnetar Janus/Splits/$directoryName"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val directory = File(requireNotNull(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)), "Splits/$directoryName")
            check(directory.mkdirs() || directory.isDirectory) { "Unable to create split output directory" }
            return PendingSplitOutput(
                File(directory, displayName).toUri(),
                directory.absolutePath,
                isMediaStoreItem = false,
            )
        }
        val values =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, location)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        val uri =
            requireNotNull(context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)) {
                "Unable to create a public split output"
            }
        return PendingSplitOutput(uri, location, isMediaStoreItem = true)
    }

    fun publish(
        context: Context,
        output: PendingSplitOutput,
    ) {
        if (!output.isMediaStoreItem) return
        context.contentResolver.update(output.uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
    }

    fun discard(
        context: Context,
        output: PendingSplitOutput,
    ) {
        if (output.isMediaStoreItem) {
            context.contentResolver.delete(output.uri, null, null)
        } else {
            File(requireNotNull(output.uri.path)).delete()
        }
    }
}
