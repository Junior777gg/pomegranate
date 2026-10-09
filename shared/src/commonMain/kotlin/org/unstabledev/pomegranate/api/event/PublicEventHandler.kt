package org.unstabledev.pomegranate.api.event

import androidx.compose.ui.graphics.Color

/**
 * This thingy is pseudo-centralized.
 * It still needs server address to fetch from and send to the interaction data.
 *
 * TODO: Add networking + API endpoint.
*/
object PublicEventHandler {
    var eventId: String = ""
    var bgColor: Color = Color.Magenta
    var title: String = ""
    var description: String = ""

    fun fetch() {}
    fun sendBool(eventId: String, v: Boolean) {}
    fun sendInt(eventId: String, v: Int) {}
    fun sendFloat(eventId: String, v: Float) {}
    fun sendString(eventId: String, v: String) {}

    fun setDebugEvent(v: String) { eventId = v }
}