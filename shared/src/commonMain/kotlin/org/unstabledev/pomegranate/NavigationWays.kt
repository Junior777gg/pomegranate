package org.unstabledev.pomegranate

data class NavigationWays(
    val goTo :(route: String) -> Unit,
    val back :() -> Unit
)
