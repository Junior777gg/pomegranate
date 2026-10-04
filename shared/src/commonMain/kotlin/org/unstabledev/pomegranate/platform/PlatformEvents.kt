package org.unstabledev.pomegranate.platform

class PlatformEvents {
    companion object {
        var Instance: PlatformEvents? = null
    }
    fun onBack() {
        onBackCallback()
    }
    fun onAppClose() {
        onAppCloseCallback()
    }

    var onBackCallback: ()->Unit = {}
    var onAppCloseCallback: ()->Unit = {}
}