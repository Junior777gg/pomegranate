package org.unstabledev.pomegranate.common

import kotlinx.serialization.Serializable

@Serializable
data class PowerSaveSettings(
    val percentageTrigger: Float = 20.0f,
    val enableOnPowerSave: Boolean = true,
    val accountCharging: Boolean = true,

    val animateGifs: Boolean = false,
    val decodeImages: Boolean = true,
)
