package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

@Composable
actual fun setStatusBarIcons(lightIcons: Boolean) {
    SideEffect {
        val style = if (lightIcons) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent

        UIApplication.sharedApplication.setStatusBarStyle(style, animated = true)
    }
}