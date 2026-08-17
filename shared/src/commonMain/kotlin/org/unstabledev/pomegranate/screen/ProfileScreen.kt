package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.FileSaver
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.components.GeneratedProfileImage
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.screen.control.ProfileScreenController
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.components.AudioPlayerWidget
import org.unstabledev.pomegranate.components.ImagePreviewPanel
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.getBitmapFromBytes
import org.unstabledev.pomegranate.kmpReadBytes


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
    val scope = rememberCoroutineScope()
    val messagePreview = remember { mutableStateOf<MessageDC?>(null) }
    val onImagePreviewClick: (MessageDC) -> Unit = remember {
        { msg -> messagePreview.value = msg }
    }

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
        if (messagePreview.value != null) {
            ImagePreviewPanel({ messagePreview.value = null }, messagePreview.value, snackbarHostState)
        } else {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        ProfileContent(state.profile, Repository.lastOpponentEmail, snackbarHostState, scope, onImagePreviewClick)
                    }

                    is ProfileState.NotFound -> {
                        ProfileContent(null, Repository.lastOpponentEmail, snackbarHostState, scope, onImagePreviewClick)
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
    }
}

@Composable
private fun ProfileContent(profile: Profile?, email: String, snackbarHostState: SnackbarHostState, scope: CoroutineScope, setImagePreview: (MessageDC) -> Unit) {
    val profilePage = remember { mutableStateOf(0) }
    LazyColumn(Modifier.padding(top = 50.dp)) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                ProfileImage(profile, profile?.displayName?:email, 96.dp)

                Spacer(Modifier.height(12.dp))

                val chat = Repository.lastContact.value!!
                val displayName = chat.nickname?:profile?.displayName?:
                    if (email == Repository.myEmail) email else chat.nickname?:chat.partnerEmail

                Text(
                    text = displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (profile?.jobTitle?.isNotBlank()?:false || profile?.company?.isNotBlank()?:false) {
                    Text(
                        text = "${profile.jobTitle} • ${profile.company}".trim(' ', '•'),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            ChatSwitcher(profilePage)
        }

        item {
            when(profilePage.value) {
                ProfilePage.ABOUT -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            if (profile?.description?.isNotBlank()?:false) {
                                InfoRow(label = "О себе", value = profile.description)
                                Divider()
                            }
                            if (profile?.location?.isNotBlank()?:false) {
                                InfoRow(label = "Локация", value = profile.location)
                                Divider()
                            }
                            if (profile?.profileUrl?.isNotBlank()?:false) {
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
                                valueColor = MaterialTheme.colorScheme.primary,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
                ProfilePage.MEDIA -> {
                    MediaList(snackbarHostState, scope, setImagePreview)
                }
                ProfilePage.AUDIO -> {
                    AudioList(snackbarHostState, scope)
                }
                ProfilePage.FILES -> {
                    FilesList(snackbarHostState, scope)
                }
            }
        }
    }
}

private object ProfilePage {
    const val ABOUT=0
    const val MEDIA=1
    const val AUDIO=2
    const val FILES=3
}

@Composable
private fun ChatSwitcher(profilePage: MutableState<Int>) {
    val email = Repository.lastOpponentEmail
    val hasMedia = Repository.messagesDao.hasMessagesOfType(email, MessageDC.IMAGE).collectAsStateWithLifecycle(initialValue = false)
    val hasAudio = Repository.messagesDao.hasMessagesOfType(email, MessageDC.AUDIO).collectAsStateWithLifecycle(initialValue = false)
    val hasFiles = Repository.messagesDao.hasMessagesOfType(email, MessageDC.FILE).collectAsStateWithLifecycle(initialValue = false)
    if (!hasMedia.value && !hasAudio.value && !hasFiles.value) return
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Row(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            ChatSwitcherButton(Modifier.clickable {profilePage.value=0}, "Описание")
            if(hasMedia.value) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=1}, "Медиа")
            }
            if(hasAudio.value) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=2}, "Аудио")
            }
            if(hasFiles.value) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=3}, "Файлы")
            }
        }
    }
}

@Composable
private fun FilesList(snackbarHostState: SnackbarHostState, scope: CoroutineScope) {
    val email = Repository.lastOpponentEmail
    val msgs = Repository.messagesDao.getAllOfTypeByEmail(email, MessageDC.FILE).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(Modifier.padding(horizontal = 16.dp)) {
        for (message in msgs.value) {
            val path = message.data.decodeToString()
            val fileName = path.substringAfterLast('/').substringAfterLast('\\')
            val fileSize = remember(message.key) {
                try {
                    val file = KMPFile(path)
                    Util.formatBinarySize(file.length())
                } catch (_: Exception) {
                    "- MB"
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .clickable {
                        scope.launch {
                            FileSaver().saveFile(path)
                            snackbarHostState.showSnackbar("Файл сохранён")
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = Icons.Default.FilePresent,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(fileName, color = MaterialTheme.colorScheme.onBackground)
                    Text(fileSize, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MediaList(snackbarHostState: SnackbarHostState, scope: CoroutineScope, setImagePreview: (MessageDC) -> Unit) {
    val email = Repository.lastOpponentEmail
    val msgs = Repository.messagesDao.getAllOfTypeByEmail(email, MessageDC.IMAGE).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in msgs.value.chunked(3)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (message in row) {
                    MediaGridItem(
                        message = message,
                        modifier = Modifier.weight(1f),
                        setImagePreview = setImagePreview
                    )
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGridItem(message: MessageDC, modifier: Modifier = Modifier, setImagePreview: (MessageDC) -> Unit) {
    var bitmap by remember(message.key) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(message.key) {
        bitmap = withContext(Dispatchers.Default) {
            try {
                getBitmapFromBytes(KMPFile(message.data.decodeToString()).kmpReadBytes())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable { setImagePreview(message) }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioList(snackbarHostState: SnackbarHostState, scope: CoroutineScope) {
    val email = Repository.lastOpponentEmail
    val msgs = Repository.messagesDao.getAllOfTypeByEmail(email, MessageDC.AUDIO).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(Modifier.padding(horizontal = 16.dp)) {
        for (message in msgs.value) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioPlayerWidget(
                    message.data.decodeToString(),
                    Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChatSwitcherButton(modifier: Modifier, text: String) {
    Box(modifier.padding(4.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
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