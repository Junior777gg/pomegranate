package org.unstabledev.pomegranate

actual class Notifications actual constructor(){
    companion object{
        lateinit var currentPush: (message: String) -> Unit
    }
    actual fun push(title: String,message: String) {
        currentPush.invoke(message)
    }
}