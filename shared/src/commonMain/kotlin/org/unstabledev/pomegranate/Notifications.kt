package org.unstabledev.pomegranate

expect class Notifications(){
    fun push(title: String, message: String)

}