package org.unstabledev.pomegranate.api.event

import androidx.compose.ui.graphics.Color

object StandardPublicEvents {
    val NONE = PublicEventEntry()
    val TEST = PublicEventEntry("test", Color.Magenta, "Test", "Public event testing")
    val NEW_YEAR = PublicEventEntry("new_year", Color.White, "New Year", "Time left until New Year",
        EventDeco.NEW_YEAR_TIMER)

    val LIST_ALL = listOf(NONE, TEST, NEW_YEAR)
}