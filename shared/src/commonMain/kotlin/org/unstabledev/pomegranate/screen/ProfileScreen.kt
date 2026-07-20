package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.components.GeneratedProfileImage
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.screen.control.ProfileScreenController
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.components.ProfileImage


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

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: Profile) : ProfileState()
    object NotFound : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navWayObj: NavigationWays) {
    val viewModel = viewModel { ProfileScreenController() }
    val snackbarHostState = remember { SnackbarHostState() }
    var profileState by remember { mutableStateOf<ProfileState>(ProfileState.Loading) }

    LaunchedEffect(Unit) {
        val email = Repository.lastOpponentEmail
        val hasProfile = viewModel.getProfile(email)

        profileState = if(hasProfile) ProfileState.Success(viewModel.profile.value)
        else ProfileState.NotFound
    }

    Scaffold(
        modifier = applyScreenPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            IconButton(
                onClick = { navWayObj.back() },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        when (val state = profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileState.Success -> {
                ProfileContent(state.profile, snackbarHostState)
            }

            is ProfileState.NotFound -> {
                GeneratedProfileCard(
                    email = Repository.lastOpponentEmail,
                    snackbarHostState
                )
            }

            is ProfileState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ошибка: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: Profile, snackbarHostState: SnackbarHostState) {
    val validAvatar = profile.avatarUrl.isNotBlank()
    LazyColumn(Modifier.padding(top = 50.dp)) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                ProfileImage(profile, profile.displayName, 96.dp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile.displayName.ifBlank { "Неизвестный пользователь" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (profile.jobTitle.isNotBlank() || profile.company.isNotBlank()) {
                    Text(
                        text = "${profile.jobTitle} • ${profile.company}".trim(' ', '•'),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    if (profile.description.isNotBlank()) {
                        InfoRow(label = "О себе", value = profile.description)
                        Divider()
                    }
                    if (profile.location.isNotBlank()) {
                        InfoRow(label = "Локация", value = profile.location)
                        Divider()
                    }
                    if (profile.profileUrl.isNotBlank()) {
                        InfoRow(
                            label = "Ссылка",
                            value = profile.profileUrl,
                            valueColor = MaterialTheme.colorScheme.primary,
                            canBeCopied = true,
                            snackbarHostState = snackbarHostState
                        )
                        Divider()
                    }
                    InfoRow(
                        label = "Email",
                        value = Repository.lastOpponentEmail,
                        canBeCopied = true,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratedProfileCard(email: String, snackbarHostState: SnackbarHostState) {
    val name = email.substringBefore('@').replaceFirstChar { it.uppercase() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        GeneratedProfileImage(
            name = email,
            size = 96.dp,
            fontSize = 36.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = email,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Профиль не найден",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, snackbarHostState: SnackbarHostState? = null, valueColor: Color = MaterialTheme.colorScheme.onBackground, canBeCopied: Boolean = false) {
    if(value.isBlank()) return
    var showSnackbar by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val baseMod = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth()

    if (showSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState?.showSnackbar("Скопировано")
            showSnackbar = false
        }
    }

    Column(if(!canBeCopied) baseMod else baseMod.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
        clipboardManager.setText(AnnotatedString(value))
        showSnackbar = true
    }) {
        Text(text = value, color = valueColor, fontSize = 16.sp)
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp)
    )
}