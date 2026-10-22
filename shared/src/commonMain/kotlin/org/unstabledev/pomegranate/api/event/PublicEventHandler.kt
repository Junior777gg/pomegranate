package org.unstabledev.pomegranate.api.event

import androidx.compose.ui.graphics.Color

/**
 * This thingy is pseudo-centralized.
 * It still needs server address to fetch from and send to the interaction data.
 *
 * TODO: Add networking + API endpoint.
*/

data class PublicEventEntry(
    val id: String = "null", val bgColor: Color = Color.Black,
    val title: String = "None", val description: String = "",
    val deco: Int = EventDeco.GENERIC_CHAT
)

object EventDeco {
    val GENERIC_CHAT=0
    val NEW_YEAR_TIMER=1
}

object PublicEventHandler {
    var currentEvent: PublicEventEntry = StandardPublicEvents.NONE

    fun fetch() {}
    fun sendBool(eventId: String, v: Boolean) {}
    fun sendInt(eventId: String, v: Int) {}
    fun sendFloat(eventId: String, v: Float) {}
    fun sendString(eventId: String, v: String) {}

    fun setDebugEvent(v: PublicEventEntry) { currentEvent = v }
}