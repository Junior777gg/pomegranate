package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.ProfileScreenController
import org.unstabledev.pomegranate.Repository


@Serializable
data class Profile(
    val hash: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("profile_url") val profileUrl: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    val location: String = "",
    val description: String = "",
    @SerialName("job_title") val jobTitle: String = "",
    val company: String = "",
    @SerialName("background_color") val backgroundColor: String = "#9d7967"
)

private val Orange = Color(0xFFF57C00)
private val GrayBg = Color(0xFFF2F2F2)
private val GrayText = Color(0xFF8A8A8A)
private val DarkText = Color(0xFF2B2B2B)

// KMP-парсер hex-цвета (android.graphics.Color.parseColor недоступен в common)
private fun String.toColor(): Color =
    Color(removePrefix("#").toLong(16) or 0xFF000000)

@Composable
fun ProfileScreen(navWayObj: NavigationWays) {
    val viewModel = viewModel { ProfileScreenController() }
    viewModel.getProfile(Repository.lastOpponentEmail)
    val profile = viewModel.profile.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(profile.backgroundColor.toColor())
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = profile.displayName,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = profile.displayName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${profile.jobTitle} • ${profile.company}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                InfoRow(label = "О себе", value = profile.description)
                Divider()
                InfoRow(label = "Локация", value = profile.location)
                Divider()
                InfoRow(label = "Ссылка", value = profile.profileUrl, valueColor = Orange)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = DarkText) {
    if (value.isBlank()) return
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(text = value, color = valueColor, fontSize = 16.sp)
        Text(text = label, color = GrayText, fontSize = 13.sp)
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        color = GrayBg,
        modifier = Modifier.padding(start = 16.dp)
    )
}