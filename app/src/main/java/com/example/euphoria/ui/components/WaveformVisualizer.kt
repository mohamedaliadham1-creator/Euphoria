package com.example.euphoria.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.euphoria.theme.WaveformStyle
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    style: WaveformStyle = WaveformStyle.NEON,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = amplitudes.size.coerceAtLeast(1)
        val spacing = width / barCount
        val barWidth = (spacing * 0.55f).coerceAtLeast(2f)

        when (style) {
            WaveformStyle.NEON, WaveformStyle.BAR -> {
                for (i in 0 until barCount) {
                    val amp = amplitudes[i] * if (isPlaying) pulseAnim else 0.4f
                    val barHeight = (amp * (height * 0.9f)).coerceIn(4f, height)
                    val x = i * spacing + (spacing - barWidth) / 2f
                    val y = centerY - barHeight / 2f

                    // Glowing outer pass
                    if (style == WaveformStyle.NEON) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.35f),
                            topLeft = Offset(x - 2f, y - 2f),
                            size = Size(barWidth + 4f, barHeight + 4f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }

                    // Solid inner gradient bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(primaryColor, secondaryColor)
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            WaveformStyle.THIN -> {
                for (i in 0 until barCount) {
                    val amp = amplitudes[i] * if (isPlaying) pulseAnim else 0.3f
                    val barHeight = (amp * (height * 0.8f)).coerceIn(4f, height)
                    val x = i * spacing + spacing / 2f
                    drawLine(
                        color = primaryColor,
                        start = Offset(x, centerY - barHeight / 2f),
                        end = Offset(x, centerY + barHeight / 2f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WaveformStyle.SMOOTH -> {
                val path = Path()
                path.moveTo(0f, centerY)
                for (i in 0 until barCount) {
                    val x = i * spacing + spacing / 2f
                    val amp = amplitudes[i] * if (isPlaying) pulseAnim else 0.4f
                    val y = centerY + sin((i + 1) * 0.8f) * (amp * height * 0.4f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }

            WaveformStyle.MINIMAL -> {
                drawLine(
                    color = secondaryColor.copy(alpha = 0.4f),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2f
                )
                for (i in 0 until barCount step 2) {
                    val amp = amplitudes[i] * if (isPlaying) 1f else 0.3f
                    val x = i * spacing + spacing / 2f
                    drawCircle(
                        color = primaryColor,
                        radius = (amp * 6f).coerceIn(2f, 7f),
                        center = Offset(x, centerY)
                    )
                }
            }
        }
    }
}
