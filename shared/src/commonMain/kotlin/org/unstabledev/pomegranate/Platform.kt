package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable

expect val isMobile: Boolean

@Composable
expect fun isLandscape(): Boolean