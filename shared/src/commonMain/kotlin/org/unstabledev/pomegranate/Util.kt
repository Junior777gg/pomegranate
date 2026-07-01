package org.unstabledev.pomegranate

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlin.random.Random

class Util {
    companion object {
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        fun isValidEmail(str: String): Boolean {
            return str.isNotBlank() && str.matches(EMAIL_REGEX)
        }

        fun randomColor(seed: Int): Color {
            val rand = Random(seed)
            return Color(rand.nextInt(256),rand.nextInt(256),rand.nextInt(256))
        }

        fun randomColor(seed: Int, dark: Boolean): Color {
            val rand = Random(seed)
            return if(dark) Color(rand.nextInt(128),rand.nextInt(128),rand.nextInt(128))
                else Color(rand.nextInt(128,256),rand.nextInt(128,256),rand.nextInt(128,256))
        }

        @Composable
        fun isKeyboardVisible(): Boolean {
            return WindowInsets.ime.getBottom(LocalDensity.current) > 0
        }
    }
}