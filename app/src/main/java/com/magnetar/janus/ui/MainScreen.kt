@file:Suppress("ktlint:standard:function-naming")

package com.magnetar.janus.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magnetar.janus.model.MediaInfo
import com.magnetar.janus.model.MediaKind
import com.magnetar.janus.model.Operation
import com.magnetar.janus.model.Segment
import com.magnetar.janus.model.SplitPlanner
import com.magnetar.janus.ui.theme.JanusBackground
import com.magnetar.janus.ui.theme.JanusError
import com.magnetar.janus.ui.theme.JanusOnSurfaceVariant
import com.magnetar.janus.ui.theme.JanusOutline
import com.magnetar.janus.ui.theme.JanusPrimary
import com.magnetar.janus.ui.theme.JanusSurface
import com.magnetar.janus.ui.theme.JanusSurfaceLow
import kotlinx.coroutines.launch

@Composable
fun JanusApp(
    media: MediaInfo?,
    loading: Boolean = false,
    processing: Boolean = false,
    errorMessage: String? = null,
    processingMessage: String? = null,
    onClearError: () -> Unit = {},
    onSelectMedia: () -> Unit,
    onClearMedia: () -> Unit,
    onPrimaryAction: (Operation, List<Segment>) -> Unit = { _, _ -> },
    onCancelProcessing: () -> Unit = {},
) {
    JanusScreen(
        media,
        loading,
        processing,
        errorMessage,
        processingMessage,
        onClearError,
        onSelectMedia,
        onClearMedia,
        onPrimaryAction,
        onCancelProcessing,
    )
}

@Composable
fun JanusScreen(
    media: MediaInfo?,
    loading: Boolean = false,
    processing: Boolean = false,
    errorMessage: String? = null,
    processingMessage: String? = null,
    onClearError: () -> Unit = {},
    onSelectMedia: () -> Unit,
    onClearMedia: () -> Unit,
    onPrimaryAction: (Operation, List<Segment>) -> Unit = { _, _ -> },
    onCancelProcessing: () -> Unit = {},
) {
    var operation by remember { mutableStateOf(Operation.SPLIT) }
    var selectedDuration by remember { mutableLongStateOf(60L) }
    var manualCuts by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = JanusSurfaceLow) {
                Text(
                    "MAGNETAR JANUS",
                    color = JanusPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(24.dp),
                )
                NavigationDrawerItem(label = { Text("Library") }, selected = true, onClick = { drawerScope.launch { drawerState.close() } })
                NavigationDrawerItem(
                    label = { Text("Processing Queue") },
                    selected = false,
                    onClick = { drawerScope.launch { drawerState.close() } },
                )
                NavigationDrawerItem(
                    label = { Text("History") },
                    selected = false,
                    onClick = { drawerScope.launch { drawerState.close() } },
                )
                NavigationDrawerItem(
                    label = { Text("Device Settings") },
                    selected = false,
                    onClick = { drawerScope.launch { drawerState.close() } },
                )
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(JanusBackground, JanusBackground.copy(blue = .10f))),
                ).verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(onMenu = { drawerScope.launch { drawerState.open() } })
            ImportPanel(media, loading, errorMessage, onClearError, onSelectMedia, onClearMedia)
            OperationSelector(operation) { operation = it }
            if (media != null) {
                when (operation) {
                    Operation.CONVERT -> ConvertWorkspace(media)
                    Operation.SPLIT -> SplitWorkspace(media, selectedDuration, manualCuts, { manualCuts = it }) { selectedDuration = it }
                    Operation.AUDIO -> AudioWorkspace(media)
                }
                OutputConfiguration(operation)
                if (processingMessage != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            processingMessage,
                            color = if (processingMessage.startsWith("Conversion complete")) JanusPrimary else JanusError,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (processing) {
                            TextButton(
                                onClick = onCancelProcessing,
                            ) { Text("CANCEL", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
                Button(
                    enabled = !processing,
                    onClick = {
                        val cuts = manualCuts.split(',').mapNotNull { it.trim().toLongOrNull() }
                        val segments =
                            if (cuts.isEmpty()) {
                                SplitPlanner.automaticSegments(media.durationSeconds, selectedDuration)
                            } else {
                                SplitPlanner.manualSegments(media.durationSeconds, cuts)
                            }
                        onPrimaryAction(operation, segments)
                    },
                    modifier =
                        Modifier.fillMaxWidth().height(
                            52.dp,
                        ),
                    shape =
                        RoundedCornerShape(
                            8.dp,
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = JanusPrimary, contentColor = Color(0xFF003919)),
                ) {
                    Text(
                        if (processing) {
                            "PROCESSING…"
                        } else if (operation ==
                            Operation.CONVERT
                        ) {
                            "CONVERT"
                        } else if (operation ==
                            Operation.SPLIT
                        ) {
                            "SPLIT"
                        } else {
                            "EXPORT"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable private fun Header(onMenu: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "☰",
            color = JanusOnSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable(onClick = onMenu),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("MAGNETAR", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp)
            Text("JANUS", color = JanusPrimary, style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp)
        }
        Text("⚙", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable private fun ImportPanel(
    media: MediaInfo?,
    loading: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onSelectMedia: () -> Unit,
    onClearMedia: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onSelectMedia),
        colors = CardDefaults.cardColors(containerColor = JanusSurfaceLow),
        border = BorderStroke(1.dp, JanusOutline),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (loading) {
            Column(Modifier.fillMaxWidth().padding(vertical = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(Modifier.size(28.dp), color = JanusPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.height(10.dp))
                Text("INSPECTING MEDIA", color = JanusPrimary, style = MaterialTheme.typography.labelLarge)
            }
        } else if (media == null) {
            Column(Modifier.fillMaxWidth().padding(vertical = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▣", color = JanusPrimary, style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text("SELECT MEDIA", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Text("Open video or audio files to begin", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                media.previewPath?.let { path ->
                    BitmapFactory.decodeFile(path)?.asImageBitmap()?.let { bitmap ->
                        Image(bitmap, contentDescription = "Preview of ${media.name}", modifier = Modifier.fillMaxWidth().height(120.dp))
                    }
                }
                Text(
                    if (media.kind ==
                        MediaKind.VIDEO
                    ) {
                        "VIDEO SOURCE"
                    } else {
                        "AUDIO SOURCE"
                    },
                    color = JanusPrimary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(media.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        SplitPlanner.formatDuration(media.durationSeconds),
                        color = JanusOnSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    media.resolution?.let { Text(it, color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelLarge) }
                    media.codec?.let { Text(it, color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelLarge) }
                    media.frameRate?.let { Text(it, color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelLarge) }
                }
                media.bitrate?.let { Text(it, color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
                Text(
                    "REPLACE MEDIA",
                    color = JanusPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable(onClick = onClearMedia),
                )
            }
        }
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = JanusError,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClearError),
            )
        }
    }
}

@Composable private fun OperationSelector(
    selected: Operation,
    onSelected: (Operation) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().border(1.dp, JanusOutline, RoundedCornerShape(8.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Operation.values().forEach { operation ->
            val active = operation == selected
            Text(
                operation.name,
                color = if (active) JanusPrimary else JanusOnSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .weight(
                            1f,
                        ).background(if (active) JanusSurface else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable {
                            onSelected(operation)
                        }.padding(vertical = 13.dp),
            )
        }
    }
}

@Composable private fun SplitWorkspace(
    media: MediaInfo,
    selectedDuration: Long,
    manualCuts: String,
    onManualCutsChanged: (String) -> Unit,
    onDurationSelected: (Long) -> Unit,
) {
    StudioCard {
        Text("TIMELINE", color = JanusPrimary, style = MaterialTheme.typography.labelSmall)
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    72.dp,
                ).background(JanusSurfaceLow, RoundedCornerShape(6.dp))
                .border(1.dp, JanusOutline, RoundedCornerShape(6.dp)),
        ) {
            Row(
                Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(32) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(
                                (
                                    12 +
                                        (it * 17) % 46
                                ).dp,
                            ).background(JanusPrimary.copy(alpha = .35f)),
                    )
                }
            }
        }
        Text("SEGMENT LENGTH · WHATSAPP PRESETS", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                SplitPlanner.presetDurations().size,
            ) { index -> DurationChip(SplitPlanner.presetDurations()[index], selectedDuration, onDurationSelected) }
            item {
                OutlinedButton(
                    onClick = {},
                    border = BorderStroke(1.dp, JanusOutline),
                ) { Text("CUSTOM", style = MaterialTheme.typography.labelSmall) }
            }
        }
        OutlinedTextField(
            value = manualCuts,
            onValueChange = onManualCutsChanged,
            label = { Text("MANUAL CUTS (seconds)") },
            placeholder = { Text("e.g. 45, 120") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val cuts = manualCuts.split(',').mapNotNull { it.trim().toLongOrNull() }
        val segments =
            if (cuts.isEmpty()) {
                SplitPlanner.automaticSegments(media.durationSeconds, selectedDuration)
            } else {
                SplitPlanner.manualSegments(media.durationSeconds, cuts)
            }
        Text(
            "${segments.size} segments · ${segments.joinToString(" · ") { SplitPlanner.formatDuration(it.durationSeconds) }}",
            color = JanusOnSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable private fun DurationChip(
    duration: Long,
    selected: Long,
    onSelected: (Long) -> Unit,
) {
    val active = duration == selected
    OutlinedButton(
        onClick = {
            onSelected(duration)
        },
        border =
            BorderStroke(
                1.dp,
                if (active) JanusPrimary else JanusOutline,
            ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (active) JanusPrimary else JanusOnSurfaceVariant),
    ) {
        Text("${duration}s", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable private fun ConvertWorkspace(media: MediaInfo) =
    StudioCard {
        Text("SOURCE  →  TARGET", color = JanusPrimary, style = MaterialTheme.typography.labelSmall)
        Text(media.container ?: "AUTO", style = MaterialTheme.typography.headlineSmall)
        Text("MP4   WEBM   MKV", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelLarge)
    }

@Composable private fun AudioWorkspace(media: MediaInfo) =
    StudioCard {
        Text(
            if (media.kind ==
                MediaKind.VIDEO
            ) {
                "EXTRACT AUDIO"
            } else {
                "AUDIO OPERATIONS"
            },
            color = JanusPrimary,
            style = MaterialTheme.typography.labelSmall,
        )
        ; Text("MP3   AAC/M4A   WAV   FLAC", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
    }

@Composable private fun OutputConfiguration(operation: Operation) =
    StudioCard {
        Text("OUTPUT CONFIGURATION", color = JanusPrimary, style = MaterialTheme.typography.labelSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("OUTPUT FORMAT", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(
                if (operation ==
                    Operation.AUDIO
                ) {
                    "MP3 ▾"
                } else {
                    "MP4 ▾"
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("QUALITY", color = JanusOnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text("ORIGINAL ▾", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge)
        }
    }

@Composable private fun StudioCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JanusSurface),
        border = BorderStroke(1.dp, JanusOutline),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
