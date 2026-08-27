package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable

expect val isMobile: Boolean

@Composable
expect fun isLandscape(): Boolean

@Composable
expect fun setStatusBarIcons(lightIcons: Boolean)

const val HAPTIC_DEFAULT_AMPLITUDE = -1
const val HAPTIC_EFFECT_CLICK = 0
const val HAPTIC_EFFECT_DOUBLE_CLICK = 1
const val HAPTIC_EFFECT_HEAVY_CLICK = 5
const val HAPTIC_EFFECT_TICK = 2

expect fun sendHaptic(amplitude: Int)
expect fun sendHaptic(milliseconds: Long, amplitude: Int)
