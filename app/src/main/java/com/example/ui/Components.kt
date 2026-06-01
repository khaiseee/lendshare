package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SimulatedRadarMap(
    centerLat: Double,
    centerLng: Double,
    items: List<Pair<String, Offset>>, // name to offset delta
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = assignmentEasingSpec(),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw slate dark map grid lines
        val gridColor = Color(0xFF263238).copy(alpha = 0.5f)
        val step = 40.dp.toPx()
        for (x in 0..(width / step).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(x * step, 0f),
                end = Offset(x * step, height),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (y in 0..(height / step).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y * step),
                end = Offset(width, y * step),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw secondary concentric circles representing scale radius
        drawCircle(
            color = Color(0xFF00B0FF).copy(alpha = 0.15f),
            radius = width * 0.25f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF00B0FF).copy(alpha = 0.1f),
            radius = width * 0.4f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Draw radar sweeping pulses
        drawCircle(
            color = Color(0xFF00E676).copy(alpha = (1f - pulseRadius) * 0.35f),
            radius = width * 0.45f * pulseRadius,
            center = center
        )

        // Draw Simulated roads using curved vector paths
        val roadPath1 = Path().apply {
            moveTo(0f, height * 0.4f)
            quadraticTo(width * 0.3f, height * 0.35f, width * 0.5f, height * 0.5f)
            quadraticTo(width * 0.7f, height * 0.65f, width, height * 0.6f)
        }
        drawPath(
            path = roadPath1,
            color = Color(0xFF37474F),
            style = Stroke(width = 6.dp.toPx())
        )

        val roadPath2 = Path().apply {
            moveTo(width * 0.3f, 0f)
            quadraticTo(width * 0.35f, height * 0.5f, width * 0.6f, height)
        }
        drawPath(
            path = roadPath2,
            color = Color(0xFF37474F),
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw Item Pinpoints around user position
        items.forEach { (name, offsetDelta) ->
            val pinX = center.x + offsetDelta.x * width * 0.35f
            val pinY = center.y + offsetDelta.y * height * 0.35f
            val pinOffset = Offset(pinX, pinY)

            // Pin glow
            drawCircle(
                color = Color(0xFFFF3D00).copy(alpha = 0.25f),
                radius = 12.dp.toPx(),
                center = pinOffset
            )
            // Pin marker
            drawCircle(
                color = Color(0xFFFF3D00),
                radius = 6.dp.toPx(),
                center = pinOffset
            )
            // Pin tip center
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pinOffset
            )
        }

        // Draw User current position (Glow Center)
        drawCircle(
            color = Color(0xFF00E676).copy(alpha = 0.3f),
            radius = 16.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0xFF00E676),
            radius = 8.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}

private fun assignmentEasingSpec(): androidx.compose.animation.core.TweenSpec<Float> {
    return tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
}

@Composable
fun DrawerListingIllustration(
    spec: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val gradient = when (spec) {
            "gradient_mavic" -> Brush.verticalGradient(listOf(Color(0xFF00E676), Color(0xFF1DE9B6)))
            "gradient_yeti" -> Brush.verticalGradient(listOf(Color(0xFF00B0FF), Color(0xFF2979FF)))
            "gradient_sony" -> Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFFD500F9)))
            "gradient_patagonia" -> Brush.verticalGradient(listOf(Color(0xFFFF9100), Color(0xFFFF3D00)))
            "gradient_segway" -> Brush.verticalGradient(listOf(Color(0xFFFFEA00), Color(0xFFFF9100)))
            "gradient_chainsaw" -> Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF212121)))
            else -> Brush.verticalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF651FFF)))
        }

        // Draw Base Card with smooth rounded corners
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            alpha = 0.9f
        )

        // Draw schematic vector styling depending on item
        val accentColor = Color.White.copy(alpha = 0.25f)
        val strokeColor = Color.White.copy(alpha = 0.8f)
        val strokeWidth = 2.dp.toPx()

        when (spec) {
            "gradient_mavic" -> { // Drone
                // Draw 4 propellers stems
                drawLine(strokeColor, Offset(w * 0.25f, h * 0.25f), Offset(w * 0.75f, h * 0.75f), strokeWidth)
                drawLine(strokeColor, Offset(w * 0.75f, h * 0.25f), Offset(w * 0.25f, h * 0.75f), strokeWidth)
                // Propeller blades caps
                drawCircle(accentColor, 18.dp.toPx(), Offset(w * 0.25f, h * 0.25f))
                drawCircle(accentColor, 18.dp.toPx(), Offset(w * 0.75f, h * 0.25f))
                drawCircle(accentColor, 18.dp.toPx(), Offset(w * 0.25f, h * 0.75f))
                drawCircle(accentColor, 18.dp.toPx(), Offset(w * 0.75f, h * 0.75f))
                // Drone body
                drawRoundRect(Color.White, Offset(w * 0.38f, h * 0.35f), Size(w * 0.24f, h * 0.3f), CornerRadius(8.dp.toPx(), 8.dp.toPx()))
                // Lens eye
                drawCircle(Color(0xFF212121), 6.dp.toPx(), Offset(w * 0.5f, h * 0.58f))
            }
            "gradient_yeti" -> { // Cooler
                // Main box container
                drawRoundRect(strokeColor, Offset(w * 0.22f, h * 0.28f), Size(w * 0.56f, h * 0.44f), CornerRadius(10.dp.toPx(), 10.dp.toPx()), Stroke(strokeWidth))
                // Cooler Lid lines
                drawLine(Color.White, Offset(w * 0.22f, h * 0.38f), Offset(w * 0.78f, h * 0.38f), strokeWidth)
                // Latch locks
                drawRect(Color.White, Offset(w * 0.32f, h * 0.38f), Size(w * 0.05f, h * 0.08f))
                drawRect(Color.White, Offset(w * 0.63f, h * 0.38f), Size(w * 0.05f, h * 0.08f))
                // Yeti Logo placeholder text
                drawRoundRect(accentColor, Offset(w * 0.42f, h * 0.52f), Size(w * 0.16f, h * 0.08f), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
            }
            "gradient_sony" -> { // Camera
                // Lens barrel circle
                drawCircle(Color.White, 24.dp.toPx(), Offset(w * 0.5f, h * 0.54f), style = Stroke(strokeWidth))
                drawCircle(accentColor, 16.dp.toPx(), Offset(w * 0.5f, h * 0.54f))
                // Main body shape
                drawRoundRect(strokeColor, Offset(w * 0.22f, h * 0.34f), Size(w * 0.56f, h * 0.38f), CornerRadius(6.dp.toPx(), 6.dp.toPx()), Stroke(strokeWidth))
                // Flash / EVF bump
                drawRect(Color.White, Offset(w * 0.42f, h * 0.27f), Size(w * 0.16f, h * 0.08f))
                // Red recording dot
                drawCircle(Color(0xFFFF1744), 4.dp.toPx(), Offset(w * 0.3f, h * 0.42f))
            }
            "gradient_patagonia" -> { // Bag duffel
                // Duffel oval body
                drawRoundRect(Color.White, Offset(w * 0.22f, h * 0.32f), Size(w * 0.56f, h * 0.36f), CornerRadius(16.dp.toPx(), 16.dp.toPx()), Stroke(strokeWidth))
                // Duffel straps lines
                drawLine(Color.White, Offset(w * 0.35f, h * 0.32f), Offset(w * 0.35f, h * 0.68f), strokeWidth)
                drawLine(Color.White, Offset(w * 0.65f, h * 0.32f), Offset(w * 0.65f, h * 0.68f), strokeWidth)
                // Grab loop handle
                val handlePath = Path().apply {
                    moveTo(w * 0.32f, h * 0.32f)
                    quadraticTo(w * 0.5f, h * 0.15f, w * 0.68f, h * 0.32f)
                }
                drawPath(handlePath, Color.White, style = Stroke(strokeWidth))
            }
            "gradient_segway" -> { // Scooter
                // Handlebar horizontal
                drawLine(Color.White, Offset(w * 0.35f, h * 0.25f), Offset(w * 0.65f, h * 0.25f), strokeWidth * 1.5f)
                // Stem vertical
                drawLine(Color.White, Offset(w * 0.4f, h * 0.25f), Offset(w * 0.4f, h * 0.72f), strokeWidth)
                // Base foot deck
                drawRoundRect(Color.White, Offset(w * 0.34f, h * 0.68f), Size(w * 0.42f, h * 0.05f), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                // Front & Back Wheels circles
                drawCircle(Color.White, 7.dp.toPx(), Offset(w * 0.38f, h * 0.75f), style = Stroke(strokeWidth))
                drawCircle(Color.White, 7.dp.toPx(), Offset(w * 0.72f, h * 0.75f), style = Stroke(strokeWidth))
            }
            "gradient_chainsaw" -> { // Tools
                // Motor chassis box
                drawRoundRect(Color.White, Offset(w * 0.22f, h * 0.38f), Size(w * 0.26f, h * 0.3f), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
                // Guide bar nose slice (cutting platform)
                drawRoundRect(accentColor, Offset(w * 0.45f, h * 0.46f), Size(w * 0.35f, h * 0.12f), CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                drawLine(Color.White, Offset(w * 0.48f, h * 0.52f), Offset(w * 0.8f, h * 0.52f), strokeWidth)
                // Hand pull shroud
                val pullPath = Path().apply {
                    moveTo(w * 0.26f, h * 0.38f)
                    quadraticTo(w * 0.15f, h * 0.25f, w * 0.22f, h * 0.52f)
                }
                drawPath(pullPath, Color.White, style = Stroke(strokeWidth))
            }
            else -> { // Default generic geometric pattern
                // Ambient dynamic layered lines
                for (i in 1..4) {
                    val progress = i / 5f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = w * 0.4f * progress,
                        center = Offset(w * 0.5f, h * 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
