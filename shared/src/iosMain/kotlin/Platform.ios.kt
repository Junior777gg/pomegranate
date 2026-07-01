import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

actual val isMobile: Boolean
    get() = true

@Composable
actual fun isLandscape(): Boolean {
    val orientation = UIDevice.currentDevice.orientation
    return orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ||
            orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeRight
}