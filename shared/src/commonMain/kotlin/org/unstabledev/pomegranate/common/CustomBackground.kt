package org.unstabledev.pomegranate.common

import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.platform.KMPFile

object ChatBackgroundIds {
    const val DEFAULT_PRIMARY = 0
    const val DEFAULT_IMG01 = 1
    const val DEFAULT_IMG02 = 2
    const val DEFAULT_IMG03 = 3
    const val DEFAULT_IMG04 = 4
    const val DEFAULT_IMG05 = 5
    const val DEFAULT_IMG06 = 6
    const val DEFAULT_IMG07 = 7
    const val DEFAULT_IMG08 = 8
    const val CUSTOM = 16
}

object BackgroundStorage {
    val CUSTOM_BACKGROUND_PATH = "${Repository.pomegranatePath}backgrounds/"

    fun ensureBackgroundDir() {
        KMPFile(CUSTOM_BACKGROUND_PATH).mkdirs()
    }

    fun getCustomBackgroundFile(): KMPFile {
        ensureBackgroundDir()
        return KMPFile(CUSTOM_BACKGROUND_PATH, "custom_bg.jpg")
    }
}