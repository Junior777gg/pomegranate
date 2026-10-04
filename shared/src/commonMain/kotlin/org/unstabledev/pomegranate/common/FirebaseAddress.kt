package org.unstabledev.pomegranate.common

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseAddress(
    val id: String,
    val title: String,
    val url: String
)
