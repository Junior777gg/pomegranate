package org.unstabledev.pomegranate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.screen.Profile

@Composable
fun ProfileImage(profile: Profile?, chat: ChatDC, size: Dp=50.dp, fontSize: TextUnit = 18.sp, isOnline: Boolean = false) {
    ProfileImage(profile, chat.partnerEmail, size, fontSize, isOnline)
}

@Composable
fun ProfileImage(profile: Profile?, partnerEmail: String, size: Dp=50.dp, fontSize: TextUnit = 18.sp, isOnline: Boolean = false) {
    Box(Modifier.size(size)) {
        if (profile?.profileUrl?.isNotBlank() ?: false) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = profile.displayName,
                modifier = Modifier
                    .size(size)
                    .clip(if(isOnline) CircleWithCutoutShape(0.4f) else CircleShape)
            )
        } else GeneratedProfileImage(partnerEmail, size, fontSize, isOnline)
        if (isOnline) {
            Box(Modifier.align(Alignment.BottomEnd).size(size * 0.3f).clip(CircleShape).background(Color(0xFF4CAF50)))
        }
    }
}

@Composable
private fun GeneratedProfileImage(name: String, size: Dp=50.dp, fontSize: TextUnit = 18.sp, isOnline: Boolean = false) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(if(isOnline) CircleWithCutoutShape(0.4f) else CircleShape)
            .background(Util.randomColor(name.hashCode())),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium
        )
    }
}