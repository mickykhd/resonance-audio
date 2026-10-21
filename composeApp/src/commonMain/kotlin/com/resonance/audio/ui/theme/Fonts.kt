package com.resonance.audio.ui.theme

import androidx.compose.ui.text.font.FontFamily

class Fonts(val grotesk: FontFamily, val jakarta: FontFamily)

expect fun provideFonts(): Fonts
