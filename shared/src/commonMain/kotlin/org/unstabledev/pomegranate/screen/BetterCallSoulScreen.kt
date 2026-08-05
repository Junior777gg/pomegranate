package org.unstabledev.pomegranate.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.screen.control.BetterCallSoulScreenController

@Composable
fun BetterCallSoulScreen(navWayObj: NavigationWays) {
    val viewModel = viewModel { BetterCallSoulScreenController() }
}