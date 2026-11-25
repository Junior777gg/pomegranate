package org.unstabledev.pomegranate.platform

expect class Sharing() {
    fun text(text: String)
    fun file(uri: String)
}