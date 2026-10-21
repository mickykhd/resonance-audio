package com.resonance.audio.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.resonance.audio.core.model.stableHash
import com.resonance.audio.ui.theme.Palette
import com.resonance.audio.ui.theme.Shapes
import com.resonance.audio.ui.theme.provideFonts

@Composable
fun headlineSm() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.2).sp,
)

@Composable
fun headlineMd() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.3).sp,
)

@Composable
fun headlineLg() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp,
)

@Composable
fun titleMd() = TextStyle(
    fontFamily = provideFonts().jakarta,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
)

@Composable
fun bodyMd() = TextStyle(
    fontFamily = provideFonts().jakarta,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

@Composable
fun bodySm() = TextStyle(
    fontFamily = provideFonts().jakarta,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

@Composable
fun labelLg() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
)

@Composable
fun labelMd() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

@Composable
fun labelSm() = TextStyle(
    fontFamily = provideFonts().grotesk,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = 12.sp,
    letterSpacing = 0.3.sp,
)

/** Panel with the signature inset hairline + outer border. */
fun Modifier.glassPanel(
    bg: Color = Palette.panel,
    radius: Int = 16,
    borderColor: Color = Palette.outlineVariant.copy(alpha = 0.20f),
    topHairline: Color? = null,
): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .drawBehind {
        drawRect(bg)
        topHairline?.let {
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, it, Color.Transparent),
                ),
                size = size.copy(height = 1.dp.toPx()),
            )
        }
    }
    .border(1.dp, borderColor, RoundedCornerShape(radius.dp))

@Composable
fun CapBadge(
    text: String,
    bg: Color,
    fg: Color,
    borderColor: Color = Color.Transparent,
    style: TextStyle = labelSm(),
    modifier: Modifier = Modifier,
    paddingH: Dp = 6.dp,
    paddingV: Dp = 3.dp,
    radius: Dp = 6.dp,
) {
    Box(
        modifier
            .background(bg, RoundedCornerShape(radius))
            .border(1.dp, borderColor, RoundedCornerShape(radius)),
    ) {
        Text(
            text = text,
            style = style,
            color = fg,
            modifier = Modifier.padding(horizontal = paddingH, vertical = paddingV),
        )
    }
}

@Composable
fun PulseDot(size: Dp, color: Color, modifier: Modifier = Modifier, ring: Boolean = true) {
    if (ring) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val scale by transition.animateFloat(
            1f,
            2.2f,
            infiniteRepeatable(tween(1400), RepeatMode.Restart),
            label = "pulseScale",
        )
        val alpha by transition.animateFloat(
            0.7f,
            0f,
            infiniteRepeatable(tween(1400), RepeatMode.Restart),
            label = "pulseAlpha",
        )
        Box(modifier, contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(color, CircleShape),
            )
            Box(Modifier.size(size).background(color, CircleShape))
        }
    } else {
        Box(Modifier.size(size).background(color, CircleShape))
    }
}

/** Three animated equalizer bars (the "now playing" glyph). */
@Composable
fun EqGlyph(
    modifier: Modifier = Modifier,
    color: Color = Palette.secondary,
    playing: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val a by transition.animateFloat(
        0.25f, 1f, infiniteRepeatable(tween(650, 0, LinearEasing), RepeatMode.Reverse), label = "eqA",
    )
    val b by transition.animateFloat(
        0.25f, 1f, infiniteRepeatable(tween(650, 140, LinearEasing), RepeatMode.Reverse), label = "eqB",
    )
    val c by transition.animateFloat(
        0.25f, 1f, infiniteRepeatable(tween(650, 280, LinearEasing), RepeatMode.Reverse), label = "eqC",
    )
    Row(
        modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
    ) {
        EqBar(if (playing) a else 0.4f, 12.dp, color)
        EqBar(if (playing) b else 0.4f, 9.dp, color)
        EqBar(if (playing) c else 0.65f, 14.dp, color)
    }
}

@Composable
private fun EqBar(value: Float, base: Dp, color: Color) {
    Box(
        Modifier
            .width(2.dp)
            .height((base.value * (0.4f + 0.6f * value)).dp)
            .background(color, RoundedCornerShape(2.dp)),
    )
}

/** Deterministic neon-gradient artwork placeholder. */
object ArtPalettes {
    data class Pairing(val start: Color, val end: Color)

    private val pairs = listOf(
        Pairing(Color(0xFF7D53FF), Color(0xFF22D3EE)),
        Pairing(Color(0xFFF43F5E), Color(0xFF8B5CF6)),
        Pairing(Color(0xFF4F46E5), Color(0xFF10B981)),
        Pairing(Color(0xFF2563EB), Color(0xFFA78BFA)),
        Pairing(Color(0xFF9333EA), Color(0xFFEC4899)),
        Pairing(Color(0xFF06B6D4), Color(0xFF3B82F6)),
        Pairing(Color(0xFF10B981), Color(0xFF4CD7F6)),
        Pairing(Color(0xFF8B5CF6), Color(0xFFFDA4AF)),
    )

    fun forSeed(seed: Int): Pairing {
        val i = ((seed % pairs.size) + pairs.size) % pairs.size
        return pairs[i]
    }

    fun brush(seed: Int, angle: Float = 32f): Brush {
        val p = forSeed(seed)
        val rotation = angle + (seed % 40)
        val f = 0.5f + rotation / 180f
        return Brush.linearGradient(
            colors = listOf(p.end.copy(alpha = 0.85f), p.start.copy(alpha = 0.95f), Palette.obsidian.copy(alpha = 0.55f)),
            start = Offset(f * 1000f, 0f),
            end = Offset((1 - f) * 1000f, 1000f),
        )
    }
}

@Composable
fun ArtworkTile(
    seed: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    ring: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .clip(shape)
            .background(ArtPalettes.brush(seed))
            .drawBehind {
                if (ring) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            endY = size.height / 3f,
                        ),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content?.invoke()
    }
}

/** The signature custom toggle (44x24, dark thumb). */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onTrack: Color = Palette.primaryContainer,
    onThumb: Color = Palette.onPrimaryContainer,
    offTrack: Color = Palette.surfaceHighest,
    offThumb: Color = Palette.outline,
    thumbRing: Color = Palette.obsidian,
) {
    val thumbX = if (checked) 22.dp else 2.dp
    Box(
        modifier
            .width(44.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(if (checked) onTrack else offTrack)
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .align(Alignment.CenterStart)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) onThumb else offThumb)
                .border(1.dp, thumbRing, CircleShape),
        )
    }
}

@Composable
fun SwitchAccent(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.width(48.dp),
        onTrack = Palette.secondary,
        onThumb = Palette.background,
        offTrack = Palette.surfaceHighest,
        offThumb = Palette.onSurfaceVariant.copy(alpha = 0.5f),
    )
}
