package org.unstabledev.pomegranate

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
    actual fun push(title: String, message: String, callback: ()->Unit) {
        val channelId = "2"
        val channel = NotificationChannel(
            channelId,
            "Messages Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(context, NotificationManager::class.java)!!
        manager.createNotificationChannel(channel)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, Class.forName("${context.packageName}.MainActivity")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "2")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setSound("https://www.myinstants.com/media/sounds/notification_o14egLP.mp3".toUri())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, notification)
    }
}