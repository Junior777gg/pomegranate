package org.unstabledev.pomegranate

class PlatformKeyEvents {
    companion object {
        var Instance: PlatformKeyEvents? = null
    }
    fun onBack() {
        onBackCallback()
    }

    var onBackCallback: ()->Unit = {}
}