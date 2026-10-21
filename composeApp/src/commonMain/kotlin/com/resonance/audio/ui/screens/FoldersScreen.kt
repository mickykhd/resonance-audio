package com.resonance.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.audio.app.AppStore
import com.resonance.audio.core.formatGb
import com.resonance.audio.core.formatMbOrGb
import com.resonance.audio.core.model.Badge
import com.resonance.audio.core.model.FolderEntry
import com.resonance.audio.core.model.ScanState
import com.resonance.audio.core.model.StorageInfo
import com.resonance.audio.ui.components.AppSwitch
import com.resonance.audio.ui.components.PulseDot
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineLg
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelLg
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.components.labelSm
import com.resonance.audio.ui.components.titleMd

@Composable
fun FoldersScreen(
    store: AppStore,
    onAddFolder: () -> Unit,
) {
    val storage by store.storage.collectAsState()
    val folders by store.folders.collectAsState()
    val scanning by store.scanning.collectAsState()
    val ignoreShort by store.ignoreShort.collectAsState()
    val autoScan by store.autoScan.collectAsState()

    Box(Modifier.fillMaxWidth()) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 220.dp),
        ) {
            item(key = "head") {
                HeadsSection(storage, scanning)
            }
            item(key = "storage") {
                StorageCard(store, storage)
            }
            item(key = "mounts") {
                MountsRow()
            }
            item(key = "dirsHead") {
                SectionHeader(
                    icon = Icons.Filled.Folder,
                    tint = Palette.primary,
                    title = "Directory Permissions",
                    trailing = "${folders.size} Paths Configured",
                )
            }
            itemsIndexed(folders, key = { _, f -> "fo-${f.path}" }) { _, folder ->
                FolderRow(folder, onToggle = { store.toggleFolder(folder.path, it) })
            }
            item(key = "rulesHead") {
                SectionHeader(
                    icon = Icons.Filled.Tune,
                    tint = Palette.secondary,
                    title = "Filter & Scan Rules",
                    trailing = "Live Rules",
                    tinted = true,
                )
            }
            item(key = "rules") {
                RulesCard(
                    ignoreShort = ignoreShort,
                    autoScan = autoScan,
                    onIgnoreShort = { store.setIgnoreShort(it) },
                    onAutoScan = { store.setAutoScan(it) },
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 86.dp, end = 16.dp),
        ) {
            AddFolderFab(onAddFolder)
        }
    }
}

@Composable
private fun HeadsSection(storage: StorageInfo, scanning: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "OFFLINE STORAGE",
                style = labelSm(),
                color = Palette.secondary,
            )
            Text("Folders & Indexer", style = headlineLg(), color = Palette.onSurface)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(99))
                .background(Palette.surfaceHighest)
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(99))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            PulseDot(8.dp, if (scanning || storage.scanState == ScanState.INDEXED) Palette.secondary else Palette.outline)
            Text(
                if (scanning) "Scanning" else if (storage.scanState == ScanState.INDEXED) "Indexed" else "Idle",
                style = labelMd(),
                color = Palette.secondary,
            )
        }
    }
}

@Composable
private fun StorageCard(store: AppStore, storage: StorageInfo) {
    val total = storage.totalBytes.coerceAtLeast(1)
    val musicFrac = (storage.musicBytes.toFloat() / total).coerceIn(0f, 1f)
    val podFrac = (storage.podcastsBytes.toFloat() / total).coerceIn(0f, 1f)
    val freeFrac = ((total - storage.usedBytes).toFloat() / total).coerceIn(0f, 1f)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassPanel(radius = 14, bg = Palette.panel.copy(alpha = 0.90f), borderColor = Palette.outlineVariant.copy(alpha = 0.30f), topHairline = Palette.primary.copy(alpha = 0.30f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.surfaceHighest)
                        .border(1.dp, Palette.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SdCard, null, tint = Palette.secondary, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Internal Storage", style = labelMd(), color = Palette.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${formatMbOrGb(storage.musicBytes + storage.podcastsBytes)}",
                            style = headlineSm(),
                            color = Palette.onSurface,
                        )
                        Text(
                            "/ ${formatGb(total)} GB",
                            style = bodySm(),
                            color = Palette.onSurfaceVariant,
                            modifier = Modifier.padding(start = 5.dp, bottom = 3.dp),
                        )
                    }
                }
            }
            Text(
                "Hi-Res FLAC",
                style = labelSm(),
                color = Palette.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(99))
                    .background(Palette.surfaceHighest)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        // multi-tone storage bar
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(99))
                .background(Palette.surfaceHighest)
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(
                Modifier
                    .weight(musicFrac.coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .background(Palette.primary, RoundedCornerShape(99)),
            )
            Box(
                Modifier
                    .weight(podFrac.coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .background(Palette.secondary, RoundedCornerShape(99)),
            )
            Spacer(
                Modifier
                    .weight(freeFrac.coerceAtLeast(0.01f))
                    .fillMaxHeight(),
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Legend(color = Palette.primary, "Music (${formatMbOrGb(storage.musicBytes)})")
            Legend(color = Palette.secondary, "Podcasts")
            Legend(color = Palette.outlineVariant, "Free (${formatGb(total - storage.usedBytes)} GB)")
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Update, null, tint = Palette.secondary, modifier = Modifier.size(16.dp))
                Text(
                    "Last scanned ${lastScannedLabel(storage.lastScanAtMs)}",
                    style = labelSm(),
                    color = Palette.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(99))
                    .background(Palette.primary.copy(alpha = 0.10f))
                    .border(1.dp, Palette.primary.copy(alpha = 0.30f), RoundedCornerShape(99))
                    .clickable { store.quickScan() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Filled.Refresh, null, tint = Palette.primary, modifier = Modifier.size(18.dp))
                Text("Rescan Folders", style = labelMd(), color = Palette.primary)
            }
        }
    }
}

private fun lastScannedLabel(ms: Long): String {
    if (ms == 0L) return "0 mins"
    val mins = ((com.resonance.audio.core.epochNowMs() - ms) / 60000).coerceAtLeast(0)
    return if (mins == 0L) "just now" else "$mins mins"
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, style = bodySm(), color = Palette.onSurfaceVariant)
    }
}

@Composable
private fun MountsRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MountChip(true, Icons.Filled.PhoneAndroid, "Internal Device")
        MountChip(false, Icons.Filled.SdCard, "MicroSD (512 GB)")
        MountChip(false, Icons.Filled.Tune, "OTG DAC/Drive")
    }
}

@Composable
private fun MountChip(active: Boolean, icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(99))
            .background(if (active) Palette.surfaceHighest else Palette.surfaceLow)
            .border(
                1.dp,
                if (active) Palette.secondary else Palette.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(99),
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Icon(icon, null, tint = if (active) Palette.secondary else Palette.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(label, style = labelMd(), color = if (active) Palette.onSurface else Palette.onSurfaceVariant)
        if (active) Box(Modifier.size(6.dp).background(Palette.secondary, CircleShape))
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, tint: Color, title: String, trailing: String, tinted: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Text(title, style = headlineSm(), color = Palette.onSurface)
        }
        Text(
            trailing,
            style = labelSm(),
            color = if (tinted) Palette.primary else Palette.onSurfaceVariant,
        )
    }
}

@Composable
private fun FolderRow(folder: FolderEntry, onToggle: (Boolean) -> Unit) {
    val enabled = folder.enabled
    val rowAlpha = if (enabled) 1f else 0.75f
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .alpha(rowAlpha)
            .glassPanel(radius = 14, bg = Palette.panel.copy(alpha = if (enabled) 0.70f else 0.40f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(folderIconBg(folder))
                        .border(1.dp, folderIconBorder(folder), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(folderIcon(folder), null, tint = folderIconTint(folder), modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            folder.displayName,
                            style = titleMd(),
                            color = if (enabled) Palette.onSurface else Palette.onSurfaceVariant,
                            textDecoration = if (enabled) null else androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Cap(folder)
                    }
                    Text(
                        folder.path,
                        style = bodySm(),
                        color = Palette.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AppSwitch(checked = enabled, onCheckedChange = onToggle)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "${folder.trackCount} tracks",
                style = labelSm(),
                color = if (enabled) Palette.secondary else Palette.outline,
            )
            Text("•", style = labelSm(), color = Palette.outlineVariant)
            Text(
                if (enabled) folderKindLabel(folder) else "Excluded from audio index",
                style = labelSm(),
                color = Palette.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Cap(folder: FolderEntry) {
    val text = when (folder.badge) {
        Badge.WHITELIST -> "WHITELIST"
        Badge.AUTO_INDEX -> "AUTO-INDEX"
        Badge.BLACKLISTED -> "BLACKLISTED"
    }
    val (bg, fg) = when (folder.badge) {
        Badge.BLACKLISTED -> Palette.errorContainer.copy(alpha = 0.3f) to Palette.error
        else -> Palette.secondary.copy(alpha = 0.15f) to Palette.secondary
    }
    Text(
        text,
        style = labelSm().copy(fontSize = 10.sp, lineHeight = 10.sp),
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

private fun folderIcon(folder: FolderEntry): ImageVector = when {
    folder.path.contains("Podcast", true) -> Icons.Filled.Podcasts
    folder.path.contains("Download", true) -> Icons.Filled.Download
    folder.path.contains("Music", true) -> Icons.Filled.MusicNote
    folder.enabled -> Icons.Filled.Folder
    else -> Icons.Filled.Block
}

private fun folderIconTint(folder: FolderEntry): Color = when {
    !folder.enabled -> Palette.outline
    folder.path.contains("Download", true) -> Palette.secondary
    else -> Palette.primary
}

private fun folderIconBg(folder: FolderEntry): Color = when {
    !folder.enabled -> Palette.surfaceContainer
    folder.path.contains("Music", true) -> Palette.primary.copy(alpha = 0.10f)
    else -> Palette.surfaceHighest
}

private fun folderIconBorder(folder: FolderEntry): Color = when {
    !folder.enabled -> Palette.outlineVariant.copy(alpha = 0.2f)
    folder.path.contains("Music", true) -> Palette.primary.copy(alpha = 0.2f)
    else -> Palette.outlineVariant.copy(alpha = 0.3f)
}

private fun folderKindLabel(folder: FolderEntry): String = when {
    folder.path.contains("Podcast", true) -> "Spoken Word / Episodic"
    folder.path.contains("Download", true) -> "Recent Downloads"
    else -> "Lossless & Master"
}

@Composable
private fun RulesCard(
    ignoreShort: Boolean,
    autoScan: Boolean,
    onIgnoreShort: (Boolean) -> Unit,
    onAutoScan: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassPanel(radius = 14, bg = Palette.panel.copy(alpha = 0.70f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RuleRow(
            title = "Ignore audio under 30 seconds",
            desc = "Prevents notification pings, ringtones, and chat voice clips from entering your library",
            checked = ignoreShort,
            onChange = onIgnoreShort,
        )
        Hairline()
        RuleRow(
            title = "Auto-scan on new media added",
            desc = "Listens to Android MediaStore broadcast events for background updates",
            checked = autoScan,
            onChange = onAutoScan,
        )
        Hairline()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Supported Formats", style = titleMd(), color = Palette.onSurface)
                Text("7 CODECS ACTIVE", style = labelSm(), color = Palette.secondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FormatChip("MP3")
                FormatChip("FLAC 24-bit", active = true)
                FormatChip("ALAC")
                FormatChip("WAV")
                FormatChip("OGG")
                FormatChip("AAC")
                FormatChip("M4A")
            }
        }
    }
}

@Composable
private fun RuleRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = titleMd(), color = Palette.onSurface)
            Text(desc, style = bodySm(), color = Palette.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
        AppSwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Hairline() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Palette.outlineVariant.copy(alpha = 0.15f)),
    )
}

@Composable
private fun FormatChip(label: String, active: Boolean = false) {
    Text(
        label,
        style = bodySm().copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 12.sp),
        color = if (active) Palette.onSurface else Palette.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(99))
            .background(if (active) Palette.primary.copy(alpha = 0.20f) else Palette.surfaceHighest)
            .border(
                1.dp,
                if (active) Palette.primary else Palette.primary.copy(alpha = 0.40f),
                RoundedCornerShape(99),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun AddFolderFab(onAddFolder: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(99))
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(Palette.primaryContainer, Palette.secondaryContainer),
                ),
            )
            .clickable(onClick = onAddFolder)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(Icons.Filled.CreateNewFolder, null, tint = Palette.onPrimary, modifier = Modifier.size(24.dp))
        Text("Add Custom Folder", style = labelLg(), color = Palette.onPrimary)
    }
}
