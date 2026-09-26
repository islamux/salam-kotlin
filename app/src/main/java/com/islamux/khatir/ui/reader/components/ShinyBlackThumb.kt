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
 * The custom slider thumb — the small draggable circle at the bottom of the
 * reader. Purely decorative: the slider's behaviour, value and bounds stay in
 * Material's Slider, and this composable only draws the knob.
 *
 * It reacts to being pressed by growing slightly, which is why the caller must
 * pass in the SAME [MutableInteractionSource] it gave to the Slider: that shared
 * object is how the thumb learns about press state that Material tracks.
 */
@Composable
fun ShinyBlackThumb(interactionSource: MutableInteractionSource) {
    // `collectIsPressedAsState()` exposes the press state as Compose state, so the
    // redraw below happens automatically. The `by` delegate avoids .value noise.
    val pressed by interactionSource.collectIsPressedAsState()
    // Animates between the two sizes instead of snapping, which is what makes the
    // press feel responsive. `by` again, this time for the animated Float.
    val scale by animateFloatAsState(if (pressed) 1.1f else 1f, label = "shinyThumbScale")
    Canvas(
        modifier = Modifier
            .size(24.dp)
            // graphicsLayer draws in the layer phase: a scale transform here is
            // applied by the GPU without re-running layout, so the animation is
            // cheap and does not disturb the slider's measurements.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Inside Canvas the receiver IS the draw scope, so `this.size` is the
        // canvas size in pixels. Deriving everything from it means the drawing
        // scales with the 24.dp box instead of using hardcoded pixel numbers.
        val radius = this.size.minDimension / 2f
        // The shine sits slightly above and left of centre — the classic trick for
        // making a sphere look lit from the upper left.
        val highlightCenter = Offset(this.size.width * 0.4f, this.size.height * 0.38f)
        // The body: a radial gradient from grey through dark grey to the app's
        // black, which is what produces the rounded, glossy look.
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
        // Two translucent white ovals: a strong highlight and a dimmer bounce
        // light on the lower right. `copy(alpha = ...)` keeps the colour but
        // makes it see-through.
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
        // A thin outline ring. `Stroke` draws the shape's border instead of
        // filling it, and `1.dp.toPx()` converts the density-independent size
        // into real pixels — required because drawing works in pixels.
        drawCircle(
            color = Color(0xFF242424),
            radius = radius,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
