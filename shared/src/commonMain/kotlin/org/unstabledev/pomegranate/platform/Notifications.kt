package org.unstabledev.pomegranate.platform

expect class Notifications(){
    fun push(title: String, message: String)

}