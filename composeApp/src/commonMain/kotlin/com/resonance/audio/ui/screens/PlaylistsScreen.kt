package com.resonance.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.audio.app.AppStore
import com.resonance.audio.core.formatDuration
import com.resonance.audio.core.model.Playlist
import com.resonance.audio.core.model.Route
import com.resonance.audio.ui.components.ArtworkTile
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.components.bodyMd
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineLg
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.theme.provideFonts
import com.resonance.audio.ui.components.titleMd

@Composable
fun PlaylistsScreen(store: AppStore, onOpenTab: (Route) -> Unit) {
    val playlists by store.playlists.collectAsState()
    var creating by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var openPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val fonts = provideFonts()

    val opened = openPlaylist
    if (opened != null) {
        val ts = store.playlistTracks(opened)
        Column(
            Modifier
                .fillMaxSize()
                .background(Palette.background),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    "Back",
                    tint = Palette.onSurface,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                        .clickable { openPlaylist = null },
                )
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(opened.name, style = headlineSm(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${ts.size} tracks", style = bodySm(), color = Palette.onSurfaceVariant)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99))
                        .background(Palette.primaryContainer)
                        .clickable { store.playPlaylist(opened) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Palette.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Text("Play", style = labelMd(), color = Palette.onPrimaryContainer)
                }
            }
            if (ts.isEmpty()) {
                EmptyPlaylistHint()
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 216.dp)) {
                    itemsIndexed(ts, key = { _, t -> "pl-${opened.id}-${t.id}" }) { i, track ->
                        SimpleTrackRow(
                            index = i + 1,
                            title = track.title,
                            artist = track.artist,
                            duration = formatDuration(track.durationMs),
                            seed = track.seed,
                        ) {
                            store.play(ts, i, "Playing from ${opened.name}")
                            store.openNowPlaying()
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Palette.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 220.dp),
    ) {
        item(key = "head") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("CURATED QUEUES", style = com.resonance.audio.ui.components.labelSm(), color = Palette.secondary)
                    Text("Playlists & Queues", style = headlineLg(), color = Palette.onSurface)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99))
                        .background(Palette.primaryContainer.copy(alpha = 0.20f))
                        .border(1.dp, Palette.primaryContainer.copy(alpha = 0.40f), RoundedCornerShape(99))
                        .clickable { creating = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Filled.Add, null, tint = Palette.primary, modifier = Modifier.size(16.dp))
                    Text("New", style = labelMd(), color = Palette.primary)
                }
            }
        }
        item(key = "quick") {
            AssembleQuick(store, onOpenTab)
        }
        item(key = "plHead") {
            Text(
                "Your Playlists",
                style = headlineSm(),
                color = Palette.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        itemsIndexed(playlists, key = { _, p -> p.id }) { _, p ->
            PlaylistCard(store, p, onOpen = { openPlaylist = p })
        }
        if (playlists.isEmpty()) {
            item(key = "none") {
                Text(
                    "No playlists yet — create one or add tracks from your Library's row options.",
                    style = bodySm(),
                    color = Palette.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }

    if (creating) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .glassPanel(radius = 20, bg = Palette.panel.copy(alpha = 0.96f), borderColor = Palette.primaryContainer.copy(alpha = 0.35f), topHairline = Palette.primary.copy(alpha = 0.3f))
                    .padding(18.dp),
            ) {
                Text("New Playlist", style = headlineSm(), color = Palette.onSurface)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Palette.surfaceContainer)
                        .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    if (nameInput.isEmpty()) {
                        Text("Name your queue...", style = bodyMd(), color = Palette.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    BasicTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        textStyle = bodyMd().copy(color = Palette.onSurface, fontFamily = fonts.jakarta),
                        cursorBrush = Brush.verticalGradient(listOf(Palette.secondary, Palette.secondary)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Cancel",
                        style = labelMd(),
                        color = Palette.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { creating = false }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                    Text(
                        "Create",
                        style = labelMd(),
                        color = Palette.onPrimaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.primaryContainer)
                            .clickable {
                                store.createPlaylist(nameInput)
                                nameInput = ""
                                creating = false
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AssembleQuick(store: AppStore, onOpenTab: (Route) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassPanel(radius = 16, borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Palette.secondary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.QueueMusic, null, tint = Palette.secondary, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("Auto Queue", style = headlineSm(), color = Palette.onSurface)
                Text("Shuffle your entire indexed library", style = bodySm(), color = Palette.onSurfaceVariant)
            }
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(99))
                .background(Palette.primaryContainer)
                .clickable { store.shuffleAll() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Shuffle, null, tint = Palette.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PlaylistCard(store: AppStore, p: Playlist, onOpen: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val count = store.playlistTracks(p).size
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.80f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .clickable(onClick = onOpen)
            .padding(12.dp),
    ) {
        ArtworkTile(
            seed = p.seed,
            modifier = Modifier
                .size(52.dp)
                .clip(Shapes.cardMd),
        )
        Column(Modifier.weight(1f)) {
            Text(p.name, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count tracks", style = bodySm(), color = Palette.onSurfaceVariant)
        }
        Icon(
            Icons.Filled.PlayArrow,
            "Play playlist",
            tint = Palette.primary,
            modifier = Modifier
                .size(36.dp)
                .padding(6.dp)
                .clickable { store.playPlaylist(p) },
        )
        Box {
            Icon(
                Icons.Filled.MoreVert,
                "Options",
                tint = Palette.onSurfaceVariant,
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp)
                    .clickable { menu = true },
            )
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Delete playlist", style = bodyMd(), color = Palette.error) },
                    onClick = {
                        store.deletePlaylist(p.id)
                        menu = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SimpleTrackRow(
    index: Int,
    title: String,
    artist: String,
    duration: String,
    seed: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("$index", style = com.resonance.audio.ui.components.labelMd(), color = Palette.onSurfaceVariant, modifier = Modifier.padding(end = 2.dp))
        ArtworkTile(
            seed = seed,
            modifier = Modifier
                .size(44.dp)
                .clip(Shapes.cardMd),
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = bodyMd().copy(color = Palette.onSurface), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(artist, style = bodySm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(duration, style = com.resonance.audio.ui.components.labelMd(), color = Palette.onSurfaceVariant)
    }
}

@Composable
private fun EmptyPlaylistHint() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.QueueMusic, null, tint = Palette.outline, modifier = Modifier.size(40.dp))
        Text("This queue is empty", style = headlineSm(), color = Palette.onSurface)
        Text("Add tracks from your Library's row options.", style = bodySm(), color = Palette.onSurfaceVariant)
    }
}
