package com.resonance.audio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.audio.core.model.PlaybackUiState
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes

@Immutable
data class MiniPlayerActions(
    val onOpen: () -> Unit,
    val onPlayPause: () -> Unit,
    val onNext: () -> Unit,
    val onFavorite: () -> Unit,
)

@Composable
fun MiniPlayer(
    state: PlaybackUiState,
    progressFraction: Float,
    actions: MiniPlayerActions,
    modifier: Modifier = Modifier,
) {
    if (!state.hasCurrent) return

    val playScale by animateFloatAsState(if (state.isPlaying) 1.0f else 0.94f, label = "miniFabScale")

    Box(
        modifier
            .height(66.dp)
            .clip(Shapes.cardXl)
            .background(Palette.surfaceHigh.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = actions.onOpen,
            ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, end = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkTile(
                seed = state.seed,
                modifier = Modifier.size(40.dp).clip(Shapes.cardMd),
                ring = false,
            )

            Row(
                Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isLive) {
                    Box(Modifier.padding(end = 6.dp)) {
                        EqGlyph(playing = state.isPlaying)
                    }
                }
                Text(
                    state.title,
                    style = titleMd(),
                    color = if (state.isLive) Palette.primary else Palette.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = Palette.secondary,
                modifier = Modifier
                    .size(38.dp)
                    .padding(9.dp)
                    .clickable(onClick = actions.onFavorite),
            )

            Box(
                Modifier
                    .padding(start = 2.dp)
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = playScale
                        scaleY = playScale
                    }
                    .clip(CircleShape)
                    .background(Palette.primary)
                    .clickable(onClick = actions.onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Palette.onPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Icon(
                Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = Palette.onSurfaceVariant,
                modifier = Modifier
                    .size(38.dp)
                    .padding(7.dp)
                    .clickable(onClick = actions.onNext),
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(3.dp)
                .background(Palette.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progressFraction.coerceIn(0.01f, 1f))
                    .height(3.dp)
                    .background(Palette.secondary),
            )
        }
    }
}
