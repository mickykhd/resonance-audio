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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.audio.app.AppStore
import com.resonance.audio.data.StreamCatalog
import com.resonance.audio.core.model.PodcastEpisode
import com.resonance.audio.core.model.Station
import com.resonance.audio.core.model.StreamTrack
import com.resonance.audio.ui.components.ArtPalettes
import com.resonance.audio.ui.components.ArtworkTile
import com.resonance.audio.ui.components.PulseDot
import com.resonance.audio.ui.components.SwitchAccent
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.components.bodyMd
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineLg
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelLg
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.components.labelSm
import com.resonance.audio.ui.theme.provideFonts
import com.resonance.audio.ui.components.titleMd

private val FEED_FILTERS = listOf("All Feeds", "Jamendo Free", "Free Music Archive", "Web Audio RSS")

@Composable
fun StreamScreen(store: AppStore) {
    val wifiOnly by store.wifiOnly.collectAsState()
    val quality by store.quality.collectAsState()
    val playState by store.playerState.collectAsState()

    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All Feeds") }

    val stations = remember(filter) {
        if (filter == "All Feeds") StreamCatalog.stations else StreamCatalog.stations
    }
    val downloads = StreamCatalog.downloads
    val podcasts = StreamCatalog.podcasts
    val searchHits = remember(search) {
        if (search.isBlank()) emptyList() else StreamCatalog.search(search)
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Palette.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 220.dp),
    ) {
        item(key = "search") {
            StreamSearchField(search, onSearch = { search = it })
        }
        item(key = "feedChips") {
            FeedsRow(filter, onFilter = { filter = it })
        }
        if (searchHits.isNotEmpty()) {
            item(key = "searchResHead") {
                SectionTitles(Icons.Filled.Radar, "Search Results", trailing = "${searchHits.size} matches")
            }
            items(searchHits.size, key = { "sh-$it" }) { i ->
                val hit = searchHits[i]
                SearchResultRow(store, hit)
            }
        } else {
            item(key = "trendHead") {
                SectionTitles(
                    Icons.Filled.Stream,
                    "Top Trending Open Stream Radio",
                    trailing = "Explore",
                    trailingAction = { store.playStation(StreamCatalog.stations.first()) },
                )
            }
            item(key = "feature") {
                FeaturedStationCard(store, StreamCatalog.feature)
            }
            item(key = "trendGrid") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    for (st in StreamCatalog.secondary) {
                        SmallStationCard(store, st, Modifier.weight(1f))
                    }
                }
            }
            item(key = "dlHead") {
                SectionTitles(
                    Icons.Filled.Download,
                    "Download for Offline Listening",
                    sub = "Free Creative Commons Lossless Tracks",
                    iconBar = true,
                )
            }
            items(downloads, key = { "dl-${it.id}" }) { t ->
                DownloadRow(store, t)
            }
            item(key = "pcHead") {
                SectionTitles(Icons.Filled.Podcasts, "Podcast & Audio Shows", trailing = "RSS Feeds")
            }
            item(key = "podRow") {
                PodcastRow(store, podcasts)
            }
            item(key = "saver") {
                BandwidthCard(wifiOnly, quality, onWifiOnly = { store.setWifiOnly(it) }, onQuality = { store.setQuality(it) })
            }
        }
    }
}

@Composable
private fun StreamSearchField(search: String, onSearch: (String) -> Unit) {
    val fonts = provideFonts()
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.90f), borderColor = Palette.outlineVariant.copy(alpha = 0.30f))
            .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.TravelExplore, null, tint = Palette.secondary, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            if (search.isEmpty()) {
                Text(
                    "Search Jamendo, FMA, RSS feeds...",
                    style = bodyMd(),
                    color = Palette.onSurfaceVariant.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = search,
                onValueChange = onSearch,
                textStyle = bodyMd().copy(color = Palette.onSurface, fontFamily = fonts.jakarta),
                cursorBrush = Brush.verticalGradient(listOf(Palette.secondary, Palette.secondary)),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.surfaceHighest.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Mic, "Voice search", tint = Palette.primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FeedsRow(selected: String, onFilter: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (f in FEED_FILTERS) {
            val active = f == selected
            val icon: ImageVector? = if (f == "All Feeds") Icons.Filled.Radar else null
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(99))
                    .background(if (active) Palette.secondary.copy(alpha = 0.15f) else Palette.surfaceLow)
                    .border(
                        1.dp,
                        if (active) Palette.secondary else Palette.outlineVariant.copy(alpha = 0.30f),
                        RoundedCornerShape(99),
                    )
                    .clickable { onFilter(f) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                if (icon != null) Icon(icon, null, tint = Palette.secondary, modifier = Modifier.size(16.dp))
                Text(f, style = labelMd(), color = if (active) Palette.secondary else Palette.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionTitles(
    icon: ImageVector,
    title: String,
    sub: String = "",
    trailing: String = "",
    trailingAction: () -> Unit = {},
    iconBar: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = Palette.secondary, modifier = Modifier.size(20.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(title, style = headlineSm(), color = Palette.onSurface)
                if (sub.isNotEmpty()) {
                    Text(sub, style = bodySm(), color = Palette.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        if (trailing.isNotEmpty()) {
            Text(
                trailing,
                style = labelMd(),
                color = Palette.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { trailingAction() }
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun LiveBadge(small: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(99))
            .background(Palette.errorContainer.copy(alpha = 0.80f))
            .border(1.dp, Palette.error.copy(alpha = 0.30f), RoundedCornerShape(99))
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 2.dp else 4.dp),
    ) {
        PulseDot(if (small) 6.dp else 8.dp, Palette.tertiaryContainer)
        Text(
            "LIVE",
            style = labelSm(),
            color = Palette.onErrorContainer,
        )
    }
}

@Composable
private fun FeaturedStationCard(store: AppStore, st: Station) {
    Box(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(176.dp)
            .clip(Shapes.cardXl)
            .background(ArtPalettes.brush(st.seed, 115f))
            .border(1.dp, Palette.outlineVariant.copy(alpha = 0.25f), Shapes.cardXl)
            .clickable { store.playStation(st) },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Palette.surfaceLowest.copy(alpha = 0.30f),
                            Palette.surfaceLowest.copy(alpha = 0.86f),
                        ),
                    ),
                ),
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LiveBadge()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99))
                        .background(Palette.surfaceLowest.copy(alpha = 0.8f))
                        .border(1.dp, Palette.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(99))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(Icons.Filled.Headphones, null, tint = Palette.secondary, modifier = Modifier.size(14.dp))
                    Text("${st.listeners} tuning in", style = labelSm(), color = Palette.secondary)
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(st.provider.uppercase(), style = labelSm(), color = Palette.secondary)
                    Text(st.name, style = headlineSm(), color = Palette.onSurface)
                    Text(st.subtitle, style = bodySm(), color = Palette.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Palette.primary)
                        .clickable { store.playStation(st) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, "Play station", tint = Palette.onPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SmallStationCard(store: AppStore, st: Station, modifier: Modifier = Modifier) {
    Column(
        modifier
            .height(160.dp)
            .clip(Shapes.cardXl)
            .background(Palette.panel.copy(alpha = 0.90f))
            .border(1.dp, Palette.outlineVariant.copy(alpha = 0.25f), Shapes.cardXl)
            .clickable { store.playStation(st) }
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LiveBadge(small = true)
            Text(st.listeners, style = labelSm(), color = Palette.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(st.name, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(st.provider, style = bodySm(), color = Palette.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Palette.surfaceBright)
                    .border(1.dp, Palette.secondary.copy(alpha = 0.3f), CircleShape)
                    .clickable { store.playStation(st) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, "Play", tint = Palette.secondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DownloadRow(store: AppStore, t: StreamTrack) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.80f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(12.dp),
    ) {
        ArtworkTile(
            seed = t.seed,
            modifier = Modifier
                .size(48.dp)
                .clip(Shapes.cardMd),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t.title, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text(
                    t.license,
                    style = labelSm(),
                    color = Palette.secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Palette.surfaceHighest)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                )
            }
            Text("${t.author} • ${t.source}", style = bodySm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
                Text(t.formatLine, style = labelSm(), color = Palette.primary)
                Text("•", style = labelSm(), color = Palette.outline)
                Text(t.sizeLabel, style = labelSm(), color = Palette.outline)
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.secondary.copy(alpha = 0.15f))
                .border(1.dp, Palette.secondary.copy(alpha = 0.40f), CircleShape)
                .clickable {
                    store.downloadStreamTrack(t)
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Download, "Download", tint = Palette.secondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PodcastRow(store: AppStore, podcasts: List<PodcastEpisode>) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(podcasts, key = { it.id }) { p ->
            Column(
                Modifier
                    .width(256.dp)
                    .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.90f), borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArtworkTile(
                        seed = p.seed,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(Shapes.cardMd),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            p.tag,
                            style = labelSm(),
                            color = if (p.tag.contains("Code")) Palette.primary else Palette.secondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99))
                                .background(
                                    (if (p.tag.contains("Code")) Palette.primary else Palette.secondary).copy(alpha = 0.20f),
                                )
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                        )
                        Text(p.show, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(p.episodeLine, style = bodySm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(p.endLine, style = labelSm(), color = Palette.outline)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(99))
                            .background(Palette.surfaceBright)
                            .border(1.dp, Palette.secondary.copy(alpha = 0.20f), RoundedCornerShape(99))
                            .clickable { store.playPodcast(p) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Filled.PlayArrow, "Stream", tint = Palette.secondary, modifier = Modifier.size(16.dp))
                        Text("Stream", style = labelMd(), color = Palette.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BandwidthCard(
    wifiOnly: Boolean,
    quality: Int,
    onWifiOnly: (Boolean) -> Unit,
    onQuality: (Int) -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.90f), borderColor = Palette.outlineVariant.copy(alpha = 0.25f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.NetworkCheck, null, tint = Palette.secondary, modifier = Modifier.size(20.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text("Network Bandwidth Saver", style = headlineSm(), color = Palette.onSurface)
                Text("Manage mobile data and online stream bitrate", style = bodySm(), color = Palette.onSurfaceVariant)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Wifi, null, tint = Palette.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Column(Modifier.padding(start = 10.dp)) {
                    Text("Stream over Wi-Fi only", style = titleMd(), color = Palette.onSurface)
                    Text("Prevent accidental cellular meter charges", style = bodySm(), color = Palette.onSurfaceVariant)
                }
            }
            SwitchAccent(checked = wifiOnly, onCheckedChange = onWifiOnly)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Stream Audio Quality", style = labelMd(), color = Palette.onSurface)
                Text(
                    when (quality) {
                        0 -> "Auto (Dynamic)"
                        1 -> "Radio Saver (128k)"
                        else -> "High Fidelity (320k)"
                    },
                    style = labelSm(),
                    color = Palette.secondary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Palette.surfaceLowest.copy(alpha = 0.8f))
                    .border(1.dp, Palette.outlineVariant.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
            ) {
                QualitySegment("Auto (Dynamic)", quality == 0, Modifier.weight(1f)) { onQuality(0) }
                QualitySegment("128k (Saver)", quality == 1, Modifier.weight(1f)) { onQuality(1) }
                QualitySegment("320k (Lossless)", quality == 2, Modifier.weight(1f)) { onQuality(2) }
            }
        }
    }
}

@Composable
private fun QualitySegment(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) Palette.surfaceHighest else Color.Transparent)
            .border(
                1.dp,
                if (active) Palette.secondary.copy(alpha = 0.4f) else Color.Transparent,
                RoundedCornerShape(9.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = labelMd(),
            color = if (active) Palette.secondary else Palette.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchResultRow(store: AppStore, hit: Any) {
    when (hit) {
        is Station -> DownloadHeaderish(store, hit.name, hit.provider, hit.bitrateLabel, { store.playStation(hit) })
        is StreamTrack -> DownloadRow(store, hit)
        is PodcastEpisode -> PodcastStreamRow(store, hit)
    }
}

@Composable
private fun DownloadHeaderish(store: AppStore, title: String, provider: String, bit: String, action: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.80f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(12.dp),
    ) {
        ArtworkTile(
            seed = title.hashCode(),
            modifier = Modifier
                .size(48.dp)
                .clip(Shapes.cardMd),
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(provider, style = bodySm(), color = Palette.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.primary)
                .clickable(onClick = action),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, "Play", tint = Palette.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PodcastStreamRow(store: AppStore, p: PodcastEpisode) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.80f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(12.dp),
    ) {
        ArtworkTile(
            seed = p.seed,
            modifier = Modifier
                .size(48.dp)
                .clip(Shapes.cardMd),
        )
        Column(Modifier.weight(1f)) {
            Text(p.show, style = titleMd(), color = Palette.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(p.episodeLine, style = bodySm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.primary)
                .clickable { store.playPodcast(p) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, "Play", tint = Palette.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
