package com.resonance.audio.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.audio.app.AppStore
import com.resonance.audio.core.EqPresets
import com.resonance.audio.core.formatClock
import com.resonance.audio.core.model.RepeatMode
import com.resonance.audio.ui.components.ArtworkTile
import com.resonance.audio.ui.components.PulseDot
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.components.bodyMd
import com.resonance.audio.ui.components.bodySm
import com.resonance.audio.ui.components.glassPanel
import com.resonance.audio.ui.components.headlineMd
import com.resonance.audio.ui.components.headlineSm
import com.resonance.audio.ui.components.labelLg
import com.resonance.audio.ui.components.labelMd
import com.resonance.audio.ui.components.labelSm
import com.resonance.audio.ui.components.titleMd
import com.resonance.audio.core.waveformBars
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NowPlayingScreen(store: AppStore, onCollapse: () -> Unit) {
    val playState by store.playerState.collectAsState()
    val eqState by store.eqState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NPHeaderRow(store, playState, onCollapse)

        QualityBadge(playState)

        NPAlbumArt(playState)

        NPTrackInfo(store, playState)

        WaveformScrubber(state = playState, onCommit = { store.playerSeek(it) })

        NPTransport(store, playState)

        OutputPill(eqState.outputDevice, eqState.bitrateLabel)

        EqCard(store, eqState)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NPHeaderRow(store: AppStore, playState: com.resonance.audio.core.model.PlaybackUiState, onCollapse: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.surfaceLow.copy(alpha = 0.8f))
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.2f), CircleShape)
                .clickable(onClick = onCollapse),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, "Collapse", tint = Palette.onSurface, modifier = Modifier.size(24.dp))
        }
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                playState.sourceLabel.uppercase(),
                style = labelSm(),
                color = Palette.onSurfaceVariant,
            )
            Text("Now Playing", style = headlineSm(), color = Palette.onSurface, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.surfaceLow.copy(alpha = 0.8f))
                .border(1.dp, Palette.secondary.copy(alpha = 0.3f), CircleShape)
                .clickable { store.nowPlayingOpen.value = false; store.setRoute(com.resonance.audio.core.model.Route.LIBRARY) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Tune, "Equalizer", tint = Palette.secondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QualityBadge(state: com.resonance.audio.core.model.PlaybackUiState) {
    Row(
        Modifier
            .clip(RoundedCornerShape(99))
            .background(Palette.surfaceLowest.copy(alpha = 0.85f))
            .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(99))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PulseDot(6.dp, Palette.secondary, ring = true)
        Text(
            if (state.isLive) "Live Stream" else "Hi-Res Lossless",
            style = labelSm(),
            color = Palette.secondary,
        )
        Text("•", color = Palette.outlineVariant, style = bodySm())
        Text(
            if (state.isLive) state.formatLabel else state.formatLabel.ifEmpty { "FLAC 96kHz / 24-bit" },
            style = labelSm().copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = Palette.onSurfaceVariant,
        )
        Icon(Icons.Filled.GraphicEq, null, tint = Palette.secondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun NPAlbumArt(state: com.resonance.audio.core.model.PlaybackUiState) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        // ambient bloom underneath
        Box(
            Modifier
                .padding(top = 10.dp)
                .size(240.dp)
                .clip(RoundedCornerShape(28.dp))
                .blur(30.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Palette.primaryContainer.copy(alpha = 0.40f),
                            Palette.secondary.copy(alpha = 0.18f),
                            Palette.primary.copy(alpha = 0.30f),
                        ),
                    ),
                ),
        )
        ArtworkTile(
            seed = state.seed,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp)),
            ring = true,
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Palette.surfaceLowest.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (state.isLive) "LIVE" else "DSD",
                    style = labelSm(),
                    color = Palette.primary,
                )
                Text(
                    if (state.isLive) "48kHz" else "2.8MHz",
                    style = labelSm(),
                    color = Palette.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NPTrackInfo(store: AppStore, state: com.resonance.audio.core.model.PlaybackUiState) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    state.title,
                    style = headlineMd(),
                    color = Palette.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (!state.isLive) {
                    Text(
                        "MASTER",
                        style = labelSm().copy(fontSize = 9.sp, lineHeight = 9.sp),
                        color = Palette.secondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Palette.surfaceHighest)
                            .border(1.dp, Palette.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(
                state.artist,
                style = bodyMd(),
                color = Palette.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.surfaceLow.copy(alpha = 0.6f))
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.QueueMusic, "Queue", tint = Palette.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        val fav = store.isCurrentFavorite()
        Box(
            Modifier
                .padding(start = 6.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Palette.primaryContainer.copy(alpha = 0.20f))
                .border(1.dp, Palette.primaryContainer.copy(alpha = 0.40f), CircleShape)
                .clickable { store.toggleFavoriteCurrent() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                "Favorite",
                tint = Palette.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** The signature waveform scrubber. */
@Composable
private fun WaveformScrubber(
    state: com.resonance.audio.core.model.PlaybackUiState,
    onCommit: (Long) -> Unit,
) {
    val fraction =
        if (state.durationMs > 0 && !state.isLive) {
            state.positionMs.toFloat() / state.durationMs
        } else {
            0.62f
        }

    val bars = remember(state.seed) { waveformBars(state.seed, 56) }
    var drag by remember { mutableStateOf(-1f) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .pointerInput(state.seed) {
                    detectTapGestures { offset ->
                        onCommit((offset.x / size.width * state.durationMs).toLong())
                    }
                }
                .pointerInput(state.seed) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (drag >= 0) {
                                onCommit((drag * state.durationMs).toLong())
                            }
                            drag = -1f
                        },
                        onDragStart = { offset -> drag = offset.x / size.width },
                    ) { change, _ ->
                        change.consume()
                        val f = (change.position.x / size.width).coerceIn(0f, 1f)
                        if (f >= 0) drag = f
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            val barW = 3.dp.toPx()
            val gap = 2.5.dp.toPx()
            val n = bars.size
            val step = w / n
            val shown = if (drag >= 0f) drag else fraction

            // hairline center
            drawLine(
                color = Palette.outlineVariant.copy(alpha = 0.20f),
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 2.dp.toPx(),
            )
            for (i in 0 until n) {
                val f = ((i + 0.5f) / n)
                val played = f <= shown
                val barH = (h * 0.86f * bars[i])
                val x = i * step + (step - barW) / 2
                val y = h / 2f - barH / 2
                if (played) {
                    drawRoundRect(
                        color = Palette.secondary,
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2),
                    )
                    drawRoundRect(
                        color = Palette.secondary.copy(alpha = 0.25f),
                        topLeft = Offset(x - barW * 0.8f, y - barH * 0.12f),
                        size = Size(barW * 2.6f, barH * 1.24f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW),
                    )
                } else {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.20f),
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2),
                    )
                }
            }
            // thumb
            val tx = w * shown
            drawCircle(Palette.secondary, radius = 7.dp.toPx(), center = Offset(tx, h / 2f))
            drawCircle(
                Palette.surfaceLowest,
                radius = 7.dp.toPx(),
                center = Offset(tx, h / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
            drawCircle(
                Palette.primary.copy(alpha = 0.55f),
                radius = 11.dp.toPx(),
                center = Offset(tx, h / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val shown = if (drag >= 0) drag else fraction
            Text(
                formatClock(if (state.isLive) 0L else (state.durationMs * shown).toLong()),
                style = labelMd(),
                color = Palette.secondary,
            )
            Text(
                if (state.isLive) "LIVE ∞" else formatClock(state.durationMs),
                style = labelMd(),
                color = Palette.outline,
            )
        }
    }
}

@Composable
private fun NPTransport(store: AppStore, state: com.resonance.audio.core.model.PlaybackUiState) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TransportIcon(
            icon = Icons.Filled.Shuffle,
            tinted = state.shuffle,
            size = 22.dp,
        ) { store.playerShuffle(!state.shuffle) }

        TransportIcon(
            icon = Icons.Filled.SkipPrevious,
            size = 30.dp,
            fill = true,
        ) { store.playerActionPrev() }

        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Palette.primary, Palette.primaryContainer)),
                )
                .drawBehind {
                    drawCircle(
                        Palette.accentViolet.copy(alpha = 0.45f),
                        radius = size.width / 1.55f,
                    )
                }
                .clickable { store.playerActionPlayPause() },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.Transparent))),
            )
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Palette.onPrimaryContainer,
                modifier = Modifier.size(34.dp),
            )
        }

        TransportIcon(
            icon = Icons.Filled.SkipNext,
            size = 30.dp,
            fill = true,
        ) { store.playerActionNext() }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TransportIcon(
                icon = if (state.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                tinted = state.repeatMode != RepeatMode.OFF,
                size = 22.dp,
            ) {
                store.playerRepeat()
            }
            if (state.repeatMode != RepeatMode.OFF) {
                Box(
                    Modifier
                        .padding(top = 3.dp)
                        .size(4.dp)
                        .background(Palette.secondary, CircleShape),
                )
            } else {
                Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun TransportIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp,
    tinted: Boolean = false,
    fill: Boolean = false,
    onClick: () -> Unit,
) {
    Icon(
        icon,
        contentDescription = null,
        tint = if (tinted) Palette.secondary else Palette.onSurfaceVariant,
        modifier = Modifier
            .size(if (fill) 48.dp else 44.dp)
            .clickable { onClick() }
            .padding(6.dp),
    )
}

@Composable
private fun OutputPill(outputDevice: String, bitrateLabel: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.72f), borderColor = Palette.outlineVariant.copy(alpha = 0.20f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Palette.secondary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Headphones, null, tint = Palette.secondary, modifier = Modifier.size(16.dp))
            }
            Column(Modifier.padding(start = 10.dp)) {
                Text(
                    "AUDIO OUTPUT",
                    style = labelSm(),
                    color = Palette.outlineVariant,
                    letterSpacing = 1.sp,
                )
                Text(outputDevice, style = bodyMd().copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = Palette.onSurface)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(99))
                .background(Palette.surfaceContainer)
                .border(1.dp, Palette.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(99))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(bitrateLabel, style = labelMd(), color = Palette.secondary)
            Icon(Icons.Filled.ExpandMore, null, tint = Palette.secondary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun EqCard(store: AppStore, eq: com.resonance.audio.core.model.EqUiState) {
    var presetMenu by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .glassPanel(radius = 16, bg = Palette.panel.copy(alpha = 0.90f), borderColor = Palette.primary.copy(alpha = 0.20f), topHairline = Palette.primary.copy(alpha = 0.30f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Equalizer, null, tint = Palette.primary, modifier = Modifier.size(18.dp))
                Text("Studio Parametric EQ", style = headlineSm(), color = Palette.onSurface)
            }
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99))
                        .background(Palette.primaryContainer.copy(alpha = 0.15f))
                        .border(1.dp, Palette.primaryContainer.copy(alpha = 0.30f), RoundedCornerShape(99))
                        .clickable { presetMenu = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    PulseDot(6.dp, Palette.primary, ring = false)
                    Text(eq.currentPreset, style = labelMd(), color = Palette.primary)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = Palette.primary, modifier = Modifier.size(14.dp))
                }
                DropdownMenu(expanded = presetMenu, onDismissRequest = { presetMenu = false }) {
                    for (p in eq.availablePresets) {
                        DropdownMenuItem(
                            text = { Text(p, style = bodyMd(), color = if (p == eq.currentPreset) Palette.primary else Palette.onSurface) },
                            onClick = {
                                store.applyEqPreset(p)
                                presetMenu = false
                            },
                        )
                    }
                }
            }
        }

        if (!eq.available) {
            Text(
                "Hardware EQ unavailable on this output — presets will apply when audio is routed through the device DAC.",
                style = bodySm(),
                color = Palette.onSurfaceVariant.copy(alpha = 0.85f),
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for ((i, band) in eq.bands.withIndex()) {
                val frac = ((band.levelDb - band.minDb).toFloat() / (band.maxDb - band.minDb)).coerceIn(0f, 1f)
                val color = bandColor(i, eq.bands.size)
                var startLevel by remember(eq.currentPreset, eq.bands.size) { mutableStateOf(band.levelDb.toFloat()) }
                Column(Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pointerInput(band.freqLabel to eq.currentPreset) {
                                detectVerticalDragGestures { change, _ ->
                                    change.consume()
                                    startLevel = (startLevel - change.positionChange().y / 14f)
                                        .coerceIn(band.minDb.toFloat(), band.maxDb.toFloat())
                                    store.setEqBand(i, startLevel.toInt())
                                }
                            },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val barW = size.width * 0.72f
                            val h = (size.height - 12.dp.toPx()) * frac + 6.dp.toPx()
                            drawRoundRect(
                                color = color,
                                topLeft = Offset((size.width - barW) / 2, size.height - h),
                                size = Size(barW, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                            )
                            drawCircle(
                                Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(size.width / 2, size.height - h),
                            )
                        }
                    }
                    Text(
                        band.freqLabel,
                        style = labelSm().copy(fontSize = 8.sp, lineHeight = 8.sp),
                        color = Palette.outline,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KnobCard("Bass Boost", bassLabel(eq.bassBoost), Palette.primary, eq.bassBoost, modifier = Modifier.weight(1f)) { store.setBassBoost(it) }
            KnobCard("Spatial 3D", pctLabel(eq.spatial), Palette.secondary, eq.spatial, modifier = Modifier.weight(1f)) { store.setSpatialFrac(it) }
            AcousticsCard(Modifier.weight(1f))
        }
    }
}

private fun bassLabel(f: Float): String {
    val db = (f * 15).toInt()
    return if (db == 0) "OFF" else "+${db}dB"
}

private fun pctLabel(f: Float): String = "${(f * 100).toInt()}%"

private fun bandColor(index: Int, count: Int): Color {
    val f = index.toFloat() / (count - 1).coerceAtLeast(1)
    return when {
        f < 0.30f -> Palette.primary
        f < 0.60f -> Palette.outlineVariant.copy(alpha = 0.85f)
        else -> Palette.secondary
    }
}

@Composable
private fun KnobCard(
    title: String,
    value: String,
    color: Color,
    frac: Float,
    modifier: Modifier = Modifier,
    onChange: (Float) -> Unit,
) {
    Column(
        modifier
            .glassPanel(radius = 12, bg = Palette.surfaceContainer.copy(alpha = 0.6f), borderColor = Palette.outlineVariant.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = labelSm(), color = Palette.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = labelSm(), color = color, maxLines = 1)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99))
                .background(Palette.surfaceHighest)
                .pointerInput(title) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        onChange((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                },
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac.coerceIn(0f, 1f))
                    .background(color, RoundedCornerShape(99)),
            )
        }
    }
}

@Composable
private fun AcousticsCard(modifier: Modifier = Modifier) {
    Column(
        modifier
            .glassPanel(radius = 12, bg = Palette.surfaceContainer.copy(alpha = 0.6f), borderColor = Palette.outlineVariant.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Acoustics", style = labelSm(), color = Palette.onSurfaceVariant)
            Text("Studio", style = labelSm(), color = Palette.onSurface)
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PulseDot(6.dp, Palette.secondary, ring = false)
            Text("Room A", style = labelMd().copy(fontSize = 9.sp), color = Palette.secondary, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
