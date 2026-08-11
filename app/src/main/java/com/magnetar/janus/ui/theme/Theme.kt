@file:Suppress("ktlint:standard:function-naming")

package com.magnetar.janus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JanusColorScheme =
    darkColorScheme(
        primary = JanusPrimary,
        onPrimary = Color(0xFF003919),
        primaryContainer = Color(0xFF145B35),
        onPrimaryContainer = Color(0xFF9BFFB8),
        background = JanusBackground,
        onBackground = Color(0xFFD8E4EA),
        surface = JanusSurface,
        onSurface = Color(0xFFD8E4EA),
        surfaceVariant = JanusSurfaceHigh,
        onSurfaceVariant = JanusOnSurfaceVariant,
        outline = JanusOutline,
        error = JanusError,
    )

@Composable
fun MagnetarJanusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JanusColorScheme, typography = Typography, content = content)
}
