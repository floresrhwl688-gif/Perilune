package com.norva.selkit.perilune.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun Backdrop(name: String, dim: Float = 0.72f, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        Art(name, Modifier.fillMaxSize(), ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Ink.copy(alpha = dim),
                        Palette.Ink.copy(alpha = dim * 0.45f),
                        Palette.Ink.copy(alpha = dim + 0.2f)
                    )
                )
            )
        )
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), content = content)
    }
}
