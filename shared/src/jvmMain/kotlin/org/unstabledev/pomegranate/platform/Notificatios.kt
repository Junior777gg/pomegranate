package org.unstabledev.pomegranate.platform

actual class Notifications actual constructor(){
    companion object{
        lateinit var currentPush: (title: String,message: String) -> Unit
    }
    actual fun push(title: String,message: String) {
        currentPush.invoke(title, message)
    }
}