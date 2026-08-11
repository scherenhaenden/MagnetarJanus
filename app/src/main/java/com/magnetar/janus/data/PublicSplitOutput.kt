package com.magnetar.janus.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

data class PendingSplitOutput(
    val uri: android.net.Uri,
    val location: String,
)

/** Creates user-visible split files under Movies/Magnetar Janus/Splits. */
object PublicSplitOutput {
    fun create(
        context: Context,
        displayName: String,
        directoryName: String,
    ): PendingSplitOutput {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { "Public split output requires Android 10 or later" }
        val location = "${Environment.DIRECTORY_MOVIES}/Magnetar Janus/Splits/$directoryName"
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
        return PendingSplitOutput(uri, location)
    }

    fun publish(
        context: Context,
        output: PendingSplitOutput,
    ) {
        context.contentResolver.update(output.uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
    }

    fun discard(
        context: Context,
        output: PendingSplitOutput,
    ) {
        context.contentResolver.delete(output.uri, null, null)
    }
}
