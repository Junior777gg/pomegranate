package org.unstabledev.pomegranate.screen.nav

data class NavigationWays(
    val goTo :(route: String) -> Unit,
    val back :() -> Unit
)
