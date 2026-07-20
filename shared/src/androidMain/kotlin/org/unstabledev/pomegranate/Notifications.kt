package org.unstabledev.pomegranate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

actual class Notifications actual constructor(){
    companion object{
        lateinit var context: Context
    }
    actual fun push(title: String, message: String) {
        val CHANNEL_ID = "2"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Messages Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(context, NotificationManager::class.java)!!
        manager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context, "2")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setSound(Uri.parse("https://alexbruni.ru/afx/sound/ringing-phone-sounds"))
            .build()
        NotificationManagerCompat.from(context).notify(1, notification)
    }
}