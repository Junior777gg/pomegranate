package org.unstabledev.pomegranate.P2PUtils

import Log

actual class LoggerImpl actual constructor(){
    actual fun init() {
        Logger.logger = object: Log{
            override fun log(message: String) {
                println(message)
            }
        }
    }
}