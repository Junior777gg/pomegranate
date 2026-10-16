package org.unstabledev.pomegranate.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchButton(checked: Boolean, text: String, modifier: Modifier = Modifier, onCheckedChanged: (Boolean)->Unit) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Switch(checked, onCheckedChanged)
        Spacer(Modifier.width(10.dp))
        Text(text)
    }
}