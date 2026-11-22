package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.database.MessageDC
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.pin
import pomegranate.shared.generated.resources.unpin

@Composable
fun PinnedMessageDisplay(message: MessageDC) {
    Row {
        Icon(
            painter = painterResource(Res.drawable.pin),
            contentDescription = null,
            modifier = Modifier.size(20.dp).rotate(45.0f)
        )
        Spacer(Modifier.width(1.dp))
        Column {
            Text(message.data.decodeToString())
        }
    }
}