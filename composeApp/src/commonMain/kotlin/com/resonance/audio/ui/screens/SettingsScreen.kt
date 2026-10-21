package com.resonance.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.audio.app.AppStore
import com.resonance.audio.core.formatCount
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineLg
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelLg
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.components.labelSm
import com.resonance.audio.ui.components.titleMd

@Composable
fun SettingsScreen(store: AppStore) {
    val storage by store.storage.collectAsState()
    val quality by store.quality.collectAsState()
    val wifiOnly by store.wifiOnly.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 220.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.padding(top = 10.dp)) {
            Text("SYSTEM", style = labelSm(), color = Palette.secondary)
            Text("Settings & Studio", style = headlineLg(), color = Palette.onSurface)
        }

        Text("Engine", style = headlineSm(), color = Palette.onSurfaceVariant)
        Column(
            Modifier
                .fillMaxWidth()
                .glassPanel(radius = 16, borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingIconRow(Icons.Filled.Headphones, "Output", "Device DAC + Bluetooth pipeline", null)
            SettingIconRow(Icons.Filled.MusicNote, "Lossless formats", "FLAC / ALAC / WAV / Hi-Res", null)
            SettingIconRow(Icons.Filled.Tune, "Stream audio quality", qualityLabel(quality, wifiOnly), null)
        }

        Text("Library", style = headlineSm(), color = Palette.onSurfaceVariant)
        Column(
            Modifier
                .fillMaxWidth()
                .glassPanel(radius = 16, borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingIconRow(
                Icons.Filled.Sync,
                "Rescan library",
                "${formatCount(storage.trackCount.toLong())} indexed tracks",
                { store.quickScan() },
            )
            SettingIconRow(Icons.Filled.Folder, "Folders & Indexer", "Manage whitelists and scan rules") {
                store.setRoute(com.resonance.audio.core.model.Route.FOLDERS)
            }
            SettingIconRow(Icons.Filled.GraphicEq, "Quick Play", "Shuffle your whole library") {
                store.shuffleAll()
            }
        }

        Text("About", style = headlineSm(), color = Palette.onSurfaceVariant)
        Column(
            Modifier
                .fillMaxWidth()
                .glassPanel(radius = 16, borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingIconRow(Icons.Filled.Bolt, "Version", "1.0.0 • Kotlin Multiplatform", null)
            SettingIconRow(Icons.Filled.Info, "Design system", "Kinetic Glass Obsidian", null)
        }

        Text(
            "Built with Compose Multiplatform • ExoPlayer • audiofx Studio effects",
            style = bodySm(),
            color = Palette.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

private fun qualityLabel(quality: Int, wifiOnly: Boolean): String = when (quality) {
    0 -> "Auto (Dynamic)" + if (wifiOnly) " • Wi-Fi only" else ""
    1 -> "128k Radio Saver" + if (wifiOnly) " • Wi-Fi only" else ""
    else -> "320k High Fidelity" + if (wifiOnly) " • Wi-Fi only" else ""
}

@Composable
private fun SettingIconRow(icon: ImageVector, title: String, detail: String, onClick: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.surfaceHighest.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Palette.primary, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = bodySm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (onClick != null) {
            Text("Open", style = labelMd(), color = Palette.secondary)
        }
    }
}
