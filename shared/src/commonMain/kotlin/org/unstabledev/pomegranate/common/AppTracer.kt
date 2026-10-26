package org.unstabledev.pomegranate.common

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class AppTrace(val origin: String, val time: Long)

object AppTracer {
    private var traces: MutableList<AppTrace> = mutableListOf()
    private var isRunning: Boolean = false

    fun start() {
        if (isRunning) {
            println("Tried to start app tracer while it was already running")
            return
        }
        traces.clear()
        isRunning = true
    }

    fun stop() {
        isRunning = false
    }

    fun dump() : List<AppTrace> {
        return traces.toList()
    }

    fun trace(v: String) {
        if (isRunning) traces.add(AppTrace(v, Clock.System.now().toEpochMilliseconds()))
    }

    fun isRunning(): Boolean = isRunning
}