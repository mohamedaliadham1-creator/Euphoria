package com.example.euphoria.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E0D38),
    cardAlpha: Float = 0.65f,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = backgroundColor.copy(alpha = cardAlpha),
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
        ) {
            content()
        }
    }
}
