package org.unstabledev.pomegranate.P2PUtils

import Log

actual class LoggerImpl actual constructor(){
    actual inline fun init() {
        Logger.logger = object: Log{
            override fun log(lambda: () -> Unit) {
                lambda()
            }
        }
    }
}