package org.unstabledev.pomegranate

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong
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

        fun formatBinarySize(
            byteCount: Long,
            decimalPlaces: Int = 2,
            zeroPadFraction: Boolean = false
        ): String {
            require(byteCount>Long.MIN_VALUE) { "Out of range" }
            require(decimalPlaces>=0) { "Negative decimal places unsupported" }
            val isNegative = byteCount < 0
            val absByteCount = abs(byteCount)
            return if (absByteCount < 1024) {
                "$byteCount B"
            } else {
                val zeroBitCount: Int = (63 - absByteCount.countLeadingZeroBits()) / 10
                val absNumber: Double = absByteCount.toDouble() / (1L shl zeroBitCount * 10)
                val roundingFactor: Int = 10.0.pow(decimalPlaces).toInt()
                val absRoundedNumberString = with((absNumber * roundingFactor).roundToLong().toString()) {
                    val splitIndex = length - decimalPlaces - 1
                    val wholeString = substring(0..splitIndex)
                    val fractionString = with(substring(splitIndex + 1)) {
                        if (zeroPadFraction) this else dropLastWhile { digit -> digit == '0' }
                    }
                    if (fractionString.isEmpty()) wholeString else "$wholeString.$fractionString"
                }
                val roundedNumberString = if(isNegative) "-$absRoundedNumberString" else absRoundedNumberString
                "$roundedNumberString ${"KMGTPE"[zeroBitCount - 1]}iB"
            }
        }
    }
}