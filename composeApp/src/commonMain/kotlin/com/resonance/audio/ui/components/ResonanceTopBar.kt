package com.resonance.audio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.theme.provideFonts

@Composable
fun ResonanceTopBar(
    showSearch: Boolean,
    searchOpen: Boolean,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fonts = provideFonts()
    Column(
        modifier
            .background(Palette.surfaceLow.copy(alpha = 0.8f))
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(listOf(Color.Transparent, Palette.outlineVariant.copy(alpha = 0.16f))),
                    size = size.copy(height = 1.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 1.dp.toPx()),
                )
            }
            .statusBarsPadding()
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(0.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Palette.primaryContainer.copy(alpha = 0.20f))
                        .border(1.dp, Palette.primaryContainer.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = Palette.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                androidx.compose.material3.Text(
                    "Resonance Audio",
                    style = com.resonance.audio.ui.components.headlineMd(),
                    color = Palette.onSurface,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            if (showSearch) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (searchOpen) Palette.onSurface else Palette.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
