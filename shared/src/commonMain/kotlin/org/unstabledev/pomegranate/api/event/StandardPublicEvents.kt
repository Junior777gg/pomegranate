package org.unstabledev.pomegranate.api.event

import androidx.compose.ui.graphics.Color

object StandardPublicEvents {
    val NONE = PublicEventEntry()
    val TEST = PublicEventEntry("test", Color.Magenta, "Test", "Public event testing")

    val LIST_ALL = listOf(NONE, TEST)
}