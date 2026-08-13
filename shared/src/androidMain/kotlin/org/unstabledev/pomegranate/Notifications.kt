package org.unstabledev.pomegranate

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri

actual class Notifications actual constructor(){
    companion object{
        lateinit var context: Context
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setSound("https://www.myinstants.com/media/sounds/notification_o14egLP.mp3".toUri())
            .build()
        NotificationManagerCompat.from(context).notify(1, notification)
    }
}