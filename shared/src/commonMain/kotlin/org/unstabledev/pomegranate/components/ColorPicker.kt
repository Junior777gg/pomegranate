package org.unstabledev.pomegranate.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.HSVColor

@Composable
fun HSVColorPicker(
    initialColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hsvState by remember { mutableStateOf(HSVColor.fromColor(initialColor)) }
    LaunchedEffect(hsvState) {
        onColorChange(hsvState.toColor())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(hsvState.toColor())
        )

        GradientSlider(
            value = hsvState.hue,
            onValueChange = { hsvState = hsvState.copy(hue = it) },
            valueRange = 0f..360f,
            gradientColors = listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
        )

        GradientSlider(
            value = hsvState.saturation,
            onValueChange = { hsvState = hsvState.copy(saturation = it) },
            valueRange = 0f..1f,
            gradientColors = listOf(
                Color.Gray,
                Color.hsv(hsvState.hue, 1f, hsvState.value)
            )
        )

        GradientSlider(
            value = hsvState.value,
            onValueChange = { hsvState = hsvState.copy(value = it) },
            valueRange = 0f..1f,
            gradientColors = listOf(
                Color.Black,
                Color.hsv(hsvState.hue, hsvState.saturation, 1f)
            )
        )
    }
}

@Composable
private fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    gradientColors: List<Color>,
    label: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!label.isBlank()) Text(label, style = MaterialTheme.typography.bodySmall)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors,
                        startX = 0f,
                        endX = size.width
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                    size = size
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.matchParentSize(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}