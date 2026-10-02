package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.components.LabeledTextField
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

sealed class ContactsScreenState {
    class NewChat : ContactsScreenState()
    class NewGroup : ContactsScreenState()
    class Base : ContactsScreenState()
}

@Composable
fun ContactsScreen(navController: NavHostController) {
    Column(applyScreenPadding()) {
        ContactsPanel(
            { navController.popBackStack() },
            { navController.navigate(route = it) },
        )
    }
}

@Composable
fun ContactsPanel(
    onBack: () -> Unit,
    onAdd: (route: Routes.ChatScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = remember { mutableStateOf<ContactsScreenState>(ContactsScreenState.Base()) }
    val scope = rememberCoroutineScope()
    val error = remember { mutableStateOf("") }
    IconButton(
        onClick = {
            when (state.value) {
                is ContactsScreenState.Base -> onBack()
                else -> state.value = ContactsScreenState.Base()
            }
        }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
    when (state.value) {
        is ContactsScreenState.NewChat -> {
            Column(
                modifier = modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textState = rememberTextFieldState()
                LabeledTextField(textState, "", "Email")
                if (!error.value.isBlank()) {
                    Text(error.value, color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = {
                    val email = textState.text.toString().trimIndent()
                    if (email.isEmpty()) {
                        error.value = "Email обязателен!"
                        return@Button
                    }
                    /*if(!Util.isValidEmail(email)) {
                        isErrorVisible = true
                        errorText = "Некорректный Email"
                        return@Button
                    }*/
                    scope.launch(Dispatchers.IO) {
                        val profile = try {
                            Gravatar.getProfile(email.sha256())
                        } catch (e: Exception) {
                            null
                        }
                        val chat = ChatDC(
                            chatName = email,
                            personsEmails = listOf(email),
                            chatType = ChatDC.Companion.ChatTypes.CHAT,
                            chatCreator = email
                        )
                        val person = PersonDC(
                            personEmail = email,
                            profile = profile?.serialize()
                        )
                        Repository.personsDao.upsert(person)
                        Repository.chatDao.upsert(chat)
                        withContext(Dispatchers.Main) {
                            onAdd(Routes.ChatScreen(chat))
                        }
                    }
                }) {
                    Text("Продолжить")
                }
            }
        }

        is ContactsScreenState.NewGroup -> {
            Column(
                modifier = modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textState = rememberTextFieldState()
                LabeledTextField(textState, "", "Название")
                if (!error.value.isBlank()) {
                    Text(error.value, color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = {
                    val email = textState.text.toString().trimIndent()
                    if (email.isEmpty()) {
                        error.value = "Название обязательно!"
                        return@Button
                    }
                    /*if(!Util.isValidEmail(email)) {
                        isErrorVisible = true
                        errorText = "Некорректный Email"
                        return@Button
                    }*/
                }) {
                    Text("Продолжить")
                }
            }
        }

        is ContactsScreenState.Base -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.clickable { state.value = ContactsScreenState.NewChat() }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.background(Color(127, 255, 212), shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Написать")
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.clickable { state.value = ContactsScreenState.NewGroup() }) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.background(Color(120, 219, 226), shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Создать чат")
                }
            }
        }
    }
}