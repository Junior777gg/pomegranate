package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessageDC.Companion.description
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.pin

@Composable
fun PinnedMessageDisplay(message: MessageDC, unpinMessage: ()->Unit) {
    Box(applyScreenPadding().fillMaxWidth().padding(all = 3.dp).shadow(4.dp, CircleShape)) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface,
            shape=RoundedCornerShape(50.dp)).padding(vertical = 3.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(3.dp))
            Icon(
                painter = painterResource(Res.drawable.pin),
                contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(45.0f)
            )
            Spacer(Modifier.width(5.dp))
            Column(Modifier.weight(2f), verticalArrangement = Arrangement.Top) {
                Text("Закреплённое сообщение", fontSize = 12.sp, lineHeight = 8.sp)
                Text(message.description(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(5.dp))
            IconButton(unpinMessage) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}