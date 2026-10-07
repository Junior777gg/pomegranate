package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Microwave
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.platform.Clipboard
import org.unstabledev.pomegranate.platform.FileSaver
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.common.Util
import org.unstabledev.pomegranate.components.AudioPlayerWidget
import org.unstabledev.pomegranate.components.GeneratedProfileImage
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.components.chat.ContactRow
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.platform.isMobile
import org.unstabledev.pomegranate.platform.kmpReadBytes
import org.unstabledev.pomegranate.screen.control.ProfileScreenController
import org.unstabledev.pomegranate.screen.nav.Routes

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
fun ProfileScreen(navController: NavHostController, chatKey: Long, email: String?) {
    val viewModel = viewModel { ProfileScreenController(chatKey) }
    val snackBarHostState = remember { SnackbarHostState() }
    var profileState by remember { mutableStateOf<ProfileState>(ProfileState.Loading) }
    val scope = rememberCoroutineScope()
    val onImagePreviewClick: (MessageDC)->Unit = remember {
        { msg -> navController.navigate(Routes.ImagePreview(msg.key)) }
    }
    val onChatDelete: ()->Unit = remember {
        {
            scope.launch {
                val chat = viewModel.getChat()
                Repository.messagesDao.deleteAll(chat.chatName, chat.chatCreator, chat.chatType)
                Repository.chatDao.delete(chat)
            }
        }
    }
    val onContactClick: (String)->Unit = remember {
        { email ->
            navController.navigate(route = Routes.ProfileScreen(chatKey, email))
        }
    }
    val onMutualChatClick: (ChatDC)->Unit = remember {
        { chat ->
            navController.navigate(route = Routes.ChatScreen(chat.key))
        }
    }

    LaunchedEffect(Unit) {
        if (email != null) {
            val profile = viewModel.getProfile(email)
            profileState =
                if (profile != null) ProfileState.Success(viewModel.profile.value)
                else ProfileState.NotFound
        } else profileState = ProfileState.NotFound
    }

    Scaffold(
        modifier = applyScreenPadding(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
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
                    ) { CircularProgressIndicator() }
                }
                is ProfileState.Success -> {
                    ProfileContent(viewModel.getChat(), email, snackBarHostState, scope,
                        onImagePreviewClick, onChatDelete, onContactClick,
                        onMutualChatClick)
                }
                is ProfileState.NotFound -> {
                    ProfileContent(viewModel.getChat(), email, snackBarHostState, scope,
                        onImagePreviewClick, onChatDelete, onContactClick,
                        onMutualChatClick)
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

@Composable
private fun ProfileContent(chatDC: ChatDC, email: String?, snackBarHostState: SnackbarHostState,
                           scope: CoroutineScope, setImagePreview: (MessageDC)->Unit,
                           onChatDelete: ()->Unit, onProfileClick: (String)->Unit,
                           onMutualChatClick: (ChatDC)->Unit) {
    val profilePage = remember { mutableStateOf(ProfilePage.ABOUT) }
    val person = runBlocking { Repository.personsDao.getByEmail(email?:chatDC.chatName) }
    val profile = person?.profile?.deserialize()
    LazyColumn(Modifier.padding(top = if(isMobile) 50.dp else 0.dp)) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                if(email==null) ProfileImage(chatDC, 96.dp)
                else ProfileImage(person, email, 96.dp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = email?:chatDC.chatName,
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
            ContentSwitcher(chatDC, email, profilePage)
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
                                    snackBarHostState = snackBarHostState,
                                    canBeCopied = true,
                                )
                                Divider()
                            }
                            if (chatDC.chatType==ChatDC.Companion.ChatTypes.GROUP && email==null) {
                                InfoRow(label = "Описание", value = "Тут будет описание группы")
                            }
                            if (email!=null) {
                                InfoRow(
                                    label = "Email",
                                    value = email,
                                    canBeCopied = true,
                                    snackBarHostState = snackBarHostState,
                                    valueColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    if (chatDC.chatType==ChatDC.Companion.ChatTypes.GROUP && email==null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier.fillMaxWidth().padding(horizontal =  16.dp, vertical = 5.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Участники", fontWeight = FontWeight.SemiBold)
                                Column {
                                    for (contact in chatDC.personsEmails) {
                                        Row(Modifier.clickable { onProfileClick(contact) }, verticalAlignment = Alignment.CenterVertically) {
                                            ContactRow(contact, modifier = Modifier.weight(1.0f))
                                            if (contact == chatDC.chatCreator) {
                                                Spacer(Modifier.width(3.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = "Владелец",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Row {
                                        Text("+ Добавить участников", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    if (!(chatDC.chatType==ChatDC.Companion.ChatTypes.GROUP && email!=null)) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier.fillMaxWidth().padding(horizontal =  16.dp, vertical = 5.dp)
                        ) {
                            Column {
                                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp).clickable {},
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text("Настройки чата")
                                }
                                Spacer(Modifier.width(5.dp))
                                Row(Modifier.fillMaxWidth().padding(all = 16.dp).clickable { onChatDelete() }, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if(email==null) Icons.Default.ExitToApp else Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(if(email==null) "Покинуть чат" else "Удалить чат",
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                ProfilePage.MEDIA -> {
                    MediaList(chatDC, email, snackBarHostState, scope, setImagePreview)
                }
                ProfilePage.AUDIO -> {
                    AudioList(chatDC, email, snackBarHostState, scope)
                }
                ProfilePage.FILES -> {
                    FilesList(chatDC, email, snackBarHostState, scope)
                }
                ProfilePage.MUTUAL_CHATS -> {
                    MutualChatsList(email, onMutualChatClick)
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
    const val MUTUAL_CHATS=4
}

@Composable
private fun ContentSwitcher(chat: ChatDC, email: String?, profilePage: MutableState<Int>) {
    val countMedia = (
        if(email==null) Repository.messagesDao.countOfType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.IMAGE)
        else Repository.messagesDao.countOfTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.IMAGE, email)
    ).collectAsStateWithLifecycle(0)
    val countAudio = (
        if(email==null) Repository.messagesDao.countOfType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.AUDIO)
        else Repository.messagesDao.countOfTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.AUDIO, email)
    ).collectAsStateWithLifecycle(0)
    val countFiles = (
        if(email==null) Repository.messagesDao.countOfType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.FILE)
        else Repository.messagesDao.countOfTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.FILE, email)
    ).collectAsStateWithLifecycle(0)
    val countMutualChats =
        if(email!=null) Repository.chatDao.countChatsWith(email).collectAsStateWithLifecycle(0)
        else mutableStateOf(0)
    if (countMedia.value==0 && countAudio.value==0 && countFiles.value==0 && countMutualChats.value==0) return
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Row(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            ChatSwitcherButton(Modifier.clickable {profilePage.value=0}, "Описание")
            if(countMedia.value!=0) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=1}, "Медиа ${countMedia.value}")
            }
            if(countAudio.value!=0) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=2}, "Аудио ${countAudio.value}")
            }
            if(countFiles.value!=0) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=3}, "Файлы ${countFiles.value}")
            }
            if(countMutualChats.value!=0) {
                Spacer(modifier = Modifier.width(4.dp).background(MaterialTheme.colorScheme.surface))
                ChatSwitcherButton(Modifier.clickable {profilePage.value=4}, "Общие чаты ${countMutualChats.value}")
            }
        }
    }
}

@Composable
private fun MutualChatsList(email: String?, onMutualChatClick: (ChatDC)->Unit) {
    if (email==null) {
        Text("Вот и как ты это сделал?")
        return
    }
    val mutualChats = Repository.chatDao.getAllChatsWith(email).collectAsStateWithLifecycle(listOf())
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.fillMaxWidth().padding(horizontal =  16.dp, vertical = 5.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Column {
                for (chat in mutualChats.value) {
                    Row(Modifier.clickable { onMutualChatClick(chat) }, verticalAlignment = Alignment.CenterVertically) {
                        ContactRow(chat.chatName, false, "", "", false) {
                            GeneratedProfileImage(chat.chatName)
                        }
                        /*if (contact == chatDC.chatCreator) {
                            Spacer(Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Владелец",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }*/
                    }
                }
                Row {
                    Text("+ Добавить участников", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun FilesList(chat: ChatDC, email: String?, snackBarHostState: SnackbarHostState, scope: CoroutineScope) {
    val msgs = (
            if(email==null) Repository.messagesDao.getByType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.FILE)
            else Repository.messagesDao.getByTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.FILE, email)
    ).collectAsStateWithLifecycle(initialValue = emptyList())

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
                            FileSaver.save(path)
                            snackBarHostState.showSnackbar("Файл сохранён")
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
private fun MediaList(
    chat: ChatDC, email: String?,
    snackBarHostState: SnackbarHostState,
    scope: CoroutineScope,
    setImagePreview: (MessageDC) -> Unit
) {
    val msgs = (
            if(email==null) Repository.messagesDao.getByType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.IMAGE)
            else Repository.messagesDao.getByTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.IMAGE, email)
    ).collectAsStateWithLifecycle(initialValue = emptyList())

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
                KMPFile(message.data.decodeToString()).kmpReadBytes().decodeToImageBitmap()
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
private fun AudioList(chat: ChatDC, email: String?, snackBarHostState: SnackbarHostState, scope: CoroutineScope) {
    val msgs = (
            if(email==null) Repository.messagesDao.getByType(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.AUDIO)
            else Repository.messagesDao.getByTypeFrom(chat.chatName, chat.chatCreator, chat.chatType, MessageDC.AUDIO, email)
    ).collectAsStateWithLifecycle(initialValue = emptyList())

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
private fun InfoRow(label: String, value: String, snackBarHostState: SnackbarHostState? = null, valueColor: Color = MaterialTheme.colorScheme.onBackground, canBeCopied: Boolean = false) {
    if(value.isBlank()) return
    var showSnackBar by remember { mutableStateOf(false) }
    val baseMod = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth()

    if (showSnackBar) {
        LaunchedEffect(Unit) {
            snackBarHostState?.showSnackbar("Скопировано")
            showSnackBar = false
        }
    }

    Column(if(!canBeCopied) baseMod else baseMod.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
        Clipboard().copyText(value)
        showSnackBar = true
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