package com.magnetar.janus

import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.magnetar.janus.model.MediaInfo
import com.magnetar.janus.model.MediaKind
import com.magnetar.janus.ui.JanusApp
import com.magnetar.janus.ui.theme.MagnetarJanusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagnetarJanusTheme { MediaPickerApp() } }
    }
}

@Composable
private fun MediaPickerApp() {
    val context = LocalContext.current
    var media by remember { mutableStateOf<MediaInfo?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null } ?: "Selected media"
        val kind = if (context.contentResolver.getType(uri).orEmpty().startsWith("audio")) MediaKind.AUDIO else MediaKind.VIDEO
        media = MediaInfo(name = name, kind = kind, durationSeconds = 277, container = name.substringAfterLast('.', "").uppercase())
    }
    JanusApp(media = media, onClearMedia = { media = null }, onSelectMedia = {
        picker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "audio/*"))
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        })
    })
}
