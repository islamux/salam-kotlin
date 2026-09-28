package com.islamux.khatir.ui.reader.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.islamux.khatir.ui.theme.AppColors

/**
 * The custom slider thumb — purely decorative, drawing only the knob. It grows
 * slightly when pressed, which is why the caller passes the SAME
 * [MutableInteractionSource] it gave the Slider.
 */
@Composable
fun ShinyBlackThumb(interactionSource: MutableInteractionSource) {
    // `by` delegate, so the redraw below happens automatically on press.
    val pressed by interactionSource.collectIsPressedAsState()
    // Animates between the two sizes, which is what makes the press feel responsive.
    val scale by animateFloatAsState(if (pressed) 1.1f else 1f, label = "shinyThumbScale")
    Canvas(
        modifier = Modifier
            .size(24.dp)
            // graphicsLayer transforms in the layer phase, so the GPU animates it without relayout.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Inside Canvas the receiver IS the draw scope, so this.size is in pixels.
        val radius = this.size.minDimension / 2f
        // The shine sits above-left of centre, making the sphere look lit.
        val highlightCenter = Offset(this.size.width * 0.4f, this.size.height * 0.38f)
        // A radial gradient gives the rounded, glossy look.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6E6E6E),
                    Color(0xFF3A3A3A),
                    AppColors.black
                ),
                center = highlightCenter,
                radius = radius * 1.25f
            ),
            radius = radius
        )
        // `copy(alpha = ...)` keeps the colour but makes it see-through.
        drawOval(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(this.size.width * 0.22f, this.size.height * 0.15f),
            size = Size(this.size.width * 0.4f, this.size.height * 0.24f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(this.size.width * 0.56f, this.size.height * 0.5f),
            size = Size(this.size.width * 0.3f, this.size.height * 0.18f)
        )
        // `Stroke` draws the border; `1.dp.toPx()` converts to real pixels.
        drawCircle(
            color = Color(0xFF242424),
            radius = radius,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
