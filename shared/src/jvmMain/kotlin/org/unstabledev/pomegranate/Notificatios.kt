package org.unstabledev.pomegranate

actual class Notifications actual constructor(){
    companion object{
        lateinit var currentPush: (title: String,message: String, callback: ()->Unit) -> Unit
    }
    actual fun push(title: String, message: String, callback: ()->Unit) {
        currentPush.invoke(title, message, callback)
    }
}