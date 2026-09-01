package org.unstabledev.pomegranate.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CircleWithCutoutShape(private val cutoutRatio: Float = 0.25f): Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline = Outline.Generic(Path().apply {
        val rad = size.minDimension / 2f
        val x = size.width / 2
        val y = size.height / 2
        addOval(Rect(x - rad, y - rad, x + rad, y + rad))

        val cutRad = rad * cutoutRatio
        val cutX = x + rad * 0.7f
        val cutY = y + rad * 0.7f
        op(this,
            Path().apply { addOval(Rect(cutX - cutRad, cutY - cutRad, cutX + cutRad, cutY + cutRad)) },
            PathOperation.Difference)
    })
}