import androidx.compose.runtime.Composable

actual val isMobile: Boolean
    get() = false

@Composable
actual fun isLandscape(): Boolean {
    return false
}