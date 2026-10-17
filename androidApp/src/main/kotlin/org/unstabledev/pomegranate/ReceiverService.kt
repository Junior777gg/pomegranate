package org.unstabledev.pomegranate


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class ReceiverService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channelId = "1"
        val channel = NotificationChannel(
            channelId, "Receiver Service", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this,
            MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pomegranate").setContentText("Ожидание входящих соединений")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setSilent(true).setOngoing(true)
            .setContentIntent(pendingIntent).build()
        startForeground(Random.nextInt(), notification)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlobalScope.launch {
            ConnectionReceiver.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}