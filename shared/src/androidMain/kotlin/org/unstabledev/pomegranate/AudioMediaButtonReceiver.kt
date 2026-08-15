package org.unstabledev.pomegranate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media.session.MediaButtonReceiver

class AudioMediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AudioPlaybackManager.mediaSession?.let {
            MediaButtonReceiver.handleIntent(it, intent)
        }
    }
}