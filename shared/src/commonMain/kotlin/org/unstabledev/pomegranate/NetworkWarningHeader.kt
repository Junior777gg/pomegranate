package org.unstabledev.pomegranate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.GridFlow.Companion.Row
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun NetworkWarningHeader() {
    val isOnline by produceState(initialValue = true) {
        while (isActive) {
            value = Firebase.isAvailable()
            if (value) {
                delay(10000)
            } else {
                delay(4000)
            }
        }
    }
    if(!isOnline) {
        Row(
            modifier = Modifier
                .height(40.dp).fillMaxWidth()
                .background(MaterialTheme.colorScheme.error),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(26.dp),
                imageVector = Icons.Default.SignalWifiConnectedNoInternet4,
                contentDescription = "no signal",
                tint = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Нет соединения", color = MaterialTheme.colorScheme.background)
        }
    }
}