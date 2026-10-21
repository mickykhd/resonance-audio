package com.resonance.audio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.resonance.audio.core.model.Route
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.provideFonts

@Composable
fun IconFor(route: Route): ImageVector = when (route) {
    Route.LIBRARY -> Icons.Filled.LibraryMusic
    Route.FOLDERS -> Icons.Filled.FolderOpen
    Route.STREAM -> Icons.Filled.TravelExplore
    Route.PLAYLISTS -> Icons.Filled.QueueMusic
    Route.SETTINGS -> Icons.Filled.Tune
}

@Composable
fun ResonanceBottomNav(
    current: Route,
    onSelect: (Route) -> Unit,
) {
    val routes = listOf(Route.LIBRARY, Route.FOLDERS, Route.STREAM, Route.PLAYLISTS, Route.SETTINGS)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Palette.surfaceLowest.copy(alpha = 0.94f))
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Palette.outlineVariant.copy(alpha = 0.14f), Color.Transparent),
                    ),
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (r in routes) {
            val active = r == current
            val scale by animateFloatAsState(if (active) 1.06f else 1f, label = "navScale${r.name}")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(r) }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Icon(
                    imageVector = IconFor(r),
                    contentDescription = r.name,
                    tint = if (active) Palette.secondary else Palette.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    r.name.replaceFirstChar { it.uppercase() },
                    style = labelSm().copy(fontFamily = provideFonts().grotesk),
                    color = if (active) Palette.secondary else Palette.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 3.dp),
                )
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(if (active) Palette.secondary else Color.Transparent, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}
