package com.resonance.audio.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.audio.app.AppStore
import com.resonance.audio.app.SortMode
import kotlinx.coroutines.launch
import com.resonance.audio.core.formatCount
import com.resonance.audio.core.formatDuration
import com.resonance.audio.core.formatGb
import com.resonance.audio.core.formatMbOrGb
import com.resonance.audio.core.groupTracks
import com.resonance.audio.core.GroupKind
import com.resonance.audio.core.TrackGroup
import com.resonance.audio.core.model.PlaybackUiState
import com.resonance.audio.core.model.Playlist
import com.resonance.audio.core.model.ScanState
import com.resonance.audio.core.model.StorageInfo
import com.resonance.audio.core.model.Track
import com.resonance.audio.ui.components.ArtworkTile
import com.resonance.audio.ui.components.EqGlyph
import com.resonance.audio.ui.components.AppSwitch
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.bodyMd
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelLg
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.components.labelSm
import com.resonance.audio.ui.components.titleMd

private val FILTERS = listOf(
    "All Songs", "Artists", "Albums", "Genres", "Folders", "Favorites", "Recently Added",
)

@Composable
fun LibraryScreen(
    store: AppStore,
    openNowPlaying: () -> Unit,
) {
    val storage by store.storage.collectAsState()
    val tracks by store.tracks.collectAsState()
    val favorites by store.favorites.collectAsState()
    val scanning by store.scanning.collectAsState()
    val permissionDenied by store.permissionDenied.collectAsState()
    val sortMode by store.sortMode.collectAsState()
    val searchOpen by store.searchOpen.collectAsState()
    val searchQuery by store.searchQuery.collectAsState()
    val filter by store.libraryFilter.collectAsState()
    val playState by store.playerState.collectAsState()
    val playlists by store.playlists.collectAsState()
    val scope = rememberCoroutineScope()
    var openGroup by remember { mutableStateOf<TrackGroup?>(null) }

    val sorted = remember(tracks, sortMode, favorites, filter, searchQuery, searchOpen) {
        var list = tracks
        if (searchOpen && searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { "${it.title} ${it.artist} ${it.album}".lowercase().contains(q) }
        }
        list = when (filter) {
            "Favorites" -> list.filter { it.id in favorites }
            "Recently Added" -> list.takeLast(30).reversed()
            else -> list
        }
        when (sortMode) {
            SortMode.DATE_ADDED -> list
            SortMode.TITLE -> list.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> list.sortedBy { it.artist.lowercase() }
            SortMode.DURATION -> list.sortedByDescending { it.durationMs }
        }
    }

    val groupKind = when (filter) {
        "Artists" -> GroupKind.ARTIST
        "Albums" -> GroupKind.ALBUM
        "Genres" -> GroupKind.GENRE
        "Folders" -> GroupKind.FOLDER
        else -> null
    }
    val groups = remember(sorted, groupKind) {
        groupKind?.let { groupTracks(sorted, it) }.orEmpty()
    }

    val detail = openGroup
    if (detail != null) {
        GroupDetail(
            group = detail,
            playState = playState,
            favorites = favorites,
            playlists = playlists,
            onBack = { openGroup = null },
            onPlayTrack = { index -> store.play(detail.tracks, index, "Playing from ${detail.title}") },
            onPlayAll = { store.play(detail.tracks, 0, "Playing from ${detail.title}") },
            onToggleFav = store::toggleFavorite,
            onAddToPlaylist = { pid, track -> store.addToPlaylist(pid, track) },
        )
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Palette.background),
        contentPadding = PaddingValues(bottom = 216.dp),
    ) {
        item(key = "hub") {
            StorageHubCard(
                storage = storage,
                scanning = scanning,
                denied = permissionDenied,
                onQuickScan = store::quickScan,
                onGrant = { scope.launch { store.loadLibrary(interactive = true) } },
            )
        }

        item(key = "chips") {
            FilterChipsRow(
                selected = filter,
                onSelect = {
                    openGroup = null
                    store.libraryFilter.value = it
                },
            )
        }

        if (searchOpen) {
            item(key = "search") {
                SearchFieldRow(store)
            }
        }

        item(key = "quickplay") {
            QuickPlayCard(store)
        }

        item(key = "recent") {
            RecentlyPlayedRow(store, favorites)
        }

        item(key = "tracksHeader") {
            if (groupKind != null) {
                GroupsHeader(title = groupKind.label, count = groups.size)
            } else {
                TracksHeader(
                    count = sorted.size,
                    sortMode = sortMode,
                    onSelectSort = { store.sortMode.value = it },
                )
            }
        }

        if (!permissionDenied) {
            if (groupKind != null) {
                items(groups, key = { "grp-${it.kind}-${it.key}" }) { group ->
                    GroupRow(
                        group = group,
                        onOpen = { openGroup = group },
                        onPlay = { store.play(group.tracks, 0, "Playing from ${group.title}") },
                    )
                }
                if (groups.isEmpty()) {
                    item(key = "empty") {
                        EmptyLibrary()
                    }
                }
            } else {
                itemsIndexed(sorted, key = { _, t -> "tr-${t.id}" }) { _, track ->
                    val active = playState.hasCurrent && playState.title == track.title && playState.sourceLabel.startsWith("Playing")
                    TrackRow(
                        track = track,
                        active = active,
                        playing = playState.isPlaying,
                        favorite = track.id in favorites,
                        playlists = playlists,
                        onPlay = {
                            store.play(sorted, sorted.indexOfFirst { it.id == track.id }.let { if (it < 0) 0 else it })
                        },
                        onToggleFav = { store.toggleFavorite(track) },
                        onAddToPlaylist = { pid -> store.addToPlaylist(pid, track) },
                    )
                }
                if (sorted.isEmpty()) {
                    item(key = "empty") {
                        EmptyLibrary()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFieldRow(store: AppStore) {
    val query by store.searchQuery.collectAsState()
    val fonts = com.resonance.audio.ui.theme.provideFonts()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .glassPanel(radius = 14, borderColor = Palette.outlineVariant.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Filter your library...", style = bodySm(), color = Palette.onSurfaceVariant.copy(alpha = 0.7f))
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = { store.searchQuery.value = it },
                textStyle = bodySm().copy(color = Palette.onSurface, fontFamily = fonts.jakarta),
                cursorBrush = Brush.verticalGradient(listOf(Palette.secondary, Palette.secondary)),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun StorageHubCard(
    storage: StorageInfo,
    scanning: Boolean,
    denied: Boolean,
    onQuickScan: () -> Unit,
    onGrant: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .glassPanel(radius = 14, borderColor = Palette.outlineVariant.copy(alpha = 0.2f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Bubble()
                    Text(
                        "Local Storage",
                        style = headlineSm(),
                        color = Palette.onSurface,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Text(
                    "DAC Direct 192kHz",
                    style = labelSm(),
                    color = Palette.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99))
                        .background(Palette.primary.copy(alpha = 0.10f))
                        .border(1.dp, Palette.primary.copy(alpha = 0.2f), RoundedCornerShape(99))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }

            if (denied) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Storage permission needed",
                        style = headlineSm(),
                        color = Palette.onSurface,
                    )
                    Text(
                        "Grant media access so Resonance can index your lossless and master-grade audio.",
                        style = bodySm(),
                        color = Palette.onSurfaceVariant,
                    )
                    Text(
                        "Grant Access",
                        style = labelLg(),
                        color = Palette.onPrimaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.primaryContainer)
                            .clickable { onGrant() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "INDEXED AUDIO",
                            style = labelSm(),
                            color = Palette.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${formatCount(storage.trackCount.toLong())} Songs Found",
                                style = titleMd(),
                                color = Palette.onSurface,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                            Text(
                                "(${formatMbOrGb(storage.musicBytes)})",
                                style = bodySm(),
                                color = Palette.onSurfaceVariant,
                                modifier = Modifier.padding(start = 6.dp, bottom = 3.dp),
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.surfaceHighest)
                            .border(1.dp, Palette.secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !scanning, onClick = onQuickScan)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        RotatingSync(active = scanning)
                        Text(
                            if (scanning) "Scanning..." else "Quick Scan",
                            style = labelMd(),
                            color = Palette.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Bubble() {
    Box {
        com.resonance.audio.ui.components.PulseDot(10.dp, Palette.secondary)
    }
}

@Composable
private fun FilterChipsRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (filter in FILTERS) {
            val active = filter == selected
            Text(
                filter,
                style = labelSm().copy(
                    color = if (active) {
                        Palette.onPrimaryContainer
                    } else {
                        Palette.onSurfaceVariant
                    },
                ),
                modifier = Modifier
                    .clip(Shapes.chip)
                    .background(
                        if (active) {
                            Palette.primaryContainer.copy(alpha = 0.30f)
                        } else {
                            Palette.surfaceContainer.copy(alpha = 0.7f)
                        },
                    )
                    .border(
                        1.dp,
                        if (active) Palette.primaryContainer.copy(alpha = 0.6f) else Palette.outlineVariant.copy(alpha = 0.28f),
                        Shapes.chip,
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 13.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
fun RotatingSync(active: Boolean) {
    if (active) {
        val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "sync")
        val angle by transition.animateFloat(
            0f,
            360f,
            androidx.compose.animation.core.infiniteRepeatable(
                androidx.compose.animation.core.tween(900, easing = androidx.compose.animation.core.LinearEasing),
            ),
            label = "syncAngle",
        )
        Icon(
            Icons.Filled.Sync,
            contentDescription = null,
            tint = Palette.secondary,
            modifier = Modifier
                .size(18.dp)
                .rotate(angle),
        )
    } else {
        Icon(Icons.Filled.Sync, contentDescription = null, tint = Palette.secondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun QuickPlayCard(store: AppStore) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(Shapes.cardXl)
            .background(
                Brush.linearGradient(
                    listOf(
                        Palette.surfaceHigh.copy(alpha = 0.9f),
                        Palette.surfaceLow,
                        Palette.surfaceLowest,
                    ),
                ),
            )
            .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), Shapes.cardXl),
    ) {
        // cyan glow bottom-right
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(190.dp)
                .offset(x = 44.dp, y = 56.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Palette.secondary.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Quick Play", style = headlineSm(), color = Palette.onSurface)
                Text(
                    "Hi-Res lossless playback engine ready",
                    style = bodySm(),
                    color = Palette.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(99))
                    .background(Palette.primaryContainer)
                    .clickable { store.shuffleAll() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, tint = Palette.onPrimaryContainer, modifier = Modifier.size(20.dp))
                Text("Shuffle All", style = labelLg(), color = Palette.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun RecentlyPlayedRow(store: AppStore, favorites: Set<String>) {
    val recent = store.recentForCarousel(limit = 8)
    Column(Modifier.padding(top = 18.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Recently Played", style = headlineSm(), color = Palette.onSurface)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { store.libraryFilter.value = "Recently Added" },
            ) {
                Text("See all", style = labelMd(), color = Palette.secondary)
                Icon(Icons.Filled.ChevronRight, null, tint = Palette.secondary, modifier = Modifier.size(16.dp))
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 10.dp),
        ) {
            itemsIndexed(recent, key = { _, t -> "rp-${t.id}" }) { _, t ->
                RecentTile(track = t, onClick = {
                    store.play(listOf(t), 0)
                    store.openNowPlaying()
                })
            }
        }
        if (recent.isEmpty()) {
            Text(
                "Your history will appear here after first playback",
                style = bodySm(),
                color = Palette.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun RecentTile(track: Track, onClick: () -> Unit) {
    Column(Modifier.width(128.dp).clickable(onClick = onClick)) {
        Box {
            ArtworkTile(
                seed = track.seed,
                modifier = Modifier
                    .size(128.dp)
                    .clip(Shapes.cardMd)
                    .border(1.dp, Palette.outlineVariant.copy(alpha = 0.2f), Shapes.cardMd),
            )
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            ) {
                Text(
                    qualityChipText(track.formatLabel),
                    style = labelSm().copy(fontSize = 9.sp, lineHeight = 9.sp),
                    color = qualityChipColor(track.formatLabel),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Palette.surfaceLowest.copy(alpha = 0.8f))
                        .border(1.dp, qualityChipColor(track.formatLabel).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Box(
                Modifier
                    .matchParentSize()
                    .background(Palette.primary.copy(alpha = 0.0f)),
            )
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
                    .alpha(0.9f),
            )
        }
        Text(
            track.title,
            style = titleMd(),
            color = Palette.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            track.artist,
            style = bodySm(),
            color = Palette.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun qualityChipText(label: String): String {
    val l = label.uppercase()
    return when {
        label.startsWith("FLAC") -> "FLAC"
        label.startsWith("ALAC") -> "ALAC"
        label.startsWith("WAV") -> "WAV"
        label.startsWith("320") -> "320k"
        else -> label.take(6)
    }
}

private fun qualityChipColor(label: String): Color = when {
    label.startsWith("FLAC") || label.startsWith("WAV") -> Palette.secondary
    label.startsWith("ALAC") || label.startsWith("320") -> Palette.primary
    else -> Palette.onSurfaceVariant
}

@Composable
private fun TracksHeader(count: Int, sortMode: SortMode, onSelectSort: (SortMode) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tracks", style = headlineSm(), color = Palette.onSurface, fontWeight = FontWeight.Bold)
            Text(
                "(${formatCount(count.toLong())})",
                style = labelMd(),
                color = Palette.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(99))
                    .background(Palette.surfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.surfaceLow)
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .clickable { menu = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Filled.Sort, null, tint = Palette.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(sortMode.label, style = labelMd(), color = Palette.onSurfaceVariant)
            Icon(Icons.Filled.ArrowDropDown, null, tint = Palette.onSurfaceVariant, modifier = Modifier.size(16.dp))
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                for (mode in SortMode.entries) {
                    DropdownMenuItem(
                        text = { Text(mode.label, style = bodyMd(), color = if (mode == sortMode) Palette.primary else Palette.onSurface) },
                        onClick = {
                            onSelectSort(mode)
                            menu = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupsHeader(title: String, count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = headlineSm(), color = Palette.onSurface, fontWeight = FontWeight.Bold)
        Text(
            "(${formatCount(count.toLong())})",
            style = labelMd(),
            color = Palette.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(99))
                .background(Palette.surfaceContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun GroupRow(group: TrackGroup, onOpen: () -> Unit, onPlay: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(Shapes.cardMd)
            .background(Palette.surfaceLow.copy(alpha = 0.55f))
            .border(1.dp, Palette.outlineVariant.copy(alpha = 0.25f), Shapes.cardMd)
            .clickable(onClick = onOpen)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val artShape = if (group.kind == GroupKind.ARTIST) CircleShape else Shapes.cardMd
        ArtworkTile(
            seed = group.seed,
            modifier = Modifier
                .size(52.dp)
                .clip(artShape)
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), artShape),
        )
        Column(Modifier.weight(1f)) {
            Text(
                group.title,
                style = titleMd(),
                color = Palette.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                group.subtitle,
                style = bodySm(),
                color = Palette.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Palette.surfaceHighest)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play ${group.title}",
                tint = Palette.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun GroupDetail(
    group: TrackGroup,
    playState: PlaybackUiState,
    favorites: Set<String>,
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onPlayTrack: (Int) -> Unit,
    onPlayAll: () -> Unit,
    onToggleFav: (Track) -> Unit,
    onAddToPlaylist: (String, Track) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.background),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Palette.surfaceLow)
                    .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back to ${group.kind.label}",
                    tint = Palette.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    group.title,
                    style = headlineSm(),
                    color = Palette.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    group.subtitle,
                    style = bodySm(),
                    color = Palette.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Palette.primaryContainer)
                    .clickable(onClick = onPlayAll)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Palette.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "Play all",
                    style = labelMd(),
                    color = Palette.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 216.dp)) {
            itemsIndexed(group.tracks, key = { _, t -> "gd-${t.id}" }) { index, track ->
                val active = playState.hasCurrent && playState.title == track.title
                TrackRow(
                    track = track,
                    active = active,
                    playing = playState.isPlaying,
                    favorite = track.id in favorites,
                    playlists = playlists,
                    onPlay = { onPlayTrack(index) },
                    onToggleFav = { onToggleFav(track) },
                    onAddToPlaylist = { pid -> onAddToPlaylist(pid, track) },
                )
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    active: Boolean,
    playing: Boolean,
    favorite: Boolean,
    playlists: List<Playlist>,
    onPlay: () -> Unit,
    onToggleFav: () -> Unit,
    onAddToPlaylist: (String) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(Shapes.cardMd)
            .background(if (active) Palette.surfaceLow.copy(alpha = 0.9f) else Color.Transparent)
            .border(
                1.dp,
                if (active) Palette.primary.copy(alpha = 0.3f) else Color.Transparent,
                Shapes.cardMd,
            ),
    ) {
        if (active) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(56.dp)
                    .background(Palette.secondary, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlay)
                .padding(start = if (active) 8.dp else 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                ArtworkTile(
                    seed = track.seed,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(Shapes.cardMd)
                        .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), Shapes.cardMd),
                )
                if (active) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(Shapes.cardMd)
                            .background(Color(0x660C0E13)),
                        contentAlignment = Alignment.Center,
                    ) {
                        EqGlyph(playing = playing)
                    }
                }
            }
            Row(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = titleMd(),
                        color = if (active) Palette.primary else Palette.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Text(
                            track.artist,
                            style = bodySm(),
                            color = Palette.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text("•", style = labelSm(), color = Palette.outline)
                        Text(
                            track.formatLabel,
                            style = labelSm().copy(fontSize = 10.sp, lineHeight = 10.sp),
                            color = if (active) Palette.secondary else Palette.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (active) Palette.secondary.copy(alpha = 0.15f) else Palette.surfaceHighest,
                                )
                                .border(
                                    1.dp,
                                    if (active) Palette.secondary.copy(alpha = 0.3f) else Palette.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Text(
                formatDuration(track.durationMs),
                style = labelMd(),
                color = if (active) Palette.secondary else Palette.onSurfaceVariant,
            )
            Box {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = Palette.onSurfaceVariant,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp)
                        .clickable { menu = true },
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (favorite) "Remove Favorite" else "Add Favorite",
                                style = bodyMd(),
                                color = Palette.onSurface,
                            )
                        },
                        onClick = {
                            onToggleFav()
                            menu = false
                        },
                    )
                    if (playlists.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist", style = bodyMd(), color = Palette.onSurface) },
                            onClick = {
                                menu = false
                            },
                        )
                    }
                    for (p in playlists.take(4)) {
                        DropdownMenuItem(
                            text = { Text("  ${p.name}", style = bodySm(), color = Palette.onSurfaceVariant) },
                            onClick = {
                                onAddToPlaylist(p.id)
                                menu = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.MusicNote, null, tint = Palette.outline, modifier = Modifier.size(40.dp))
        Text("No tracks indexed yet", style = headlineSm(), color = Palette.onSurface)
        Text(
            "Tap Quick Scan to index your device audio, or stream open radio on the Stream tab.",
            style = bodySm(),
            color = Palette.onSurfaceVariant,
        )
    }
}
