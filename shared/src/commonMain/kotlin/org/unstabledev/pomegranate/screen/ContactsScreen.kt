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
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.components.LabeledTextField
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

sealed class ContactsScreenStates {
    class NewChat : ContactsScreenStates()
    class NewGroup : ContactsScreenStates()
    class Base : ContactsScreenStates()
    class Error(val errorText: String) : ContactsScreenStates()
}

@Composable
fun ContactsScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    Column(applyScreenPadding()) {
        ContactsPanel({ navWayObj.back() }, { navWayObj.goTo(Routes.CHAT_SCREEN) }, chatDao)
    }
}

@Composable
fun ContactsPanel(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    chatDao: ChatDao? = null,
    modifier: Modifier = Modifier
) {
    val state = remember { mutableStateOf<ContactsScreenStates>(ContactsScreenStates.Base()) }
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            when (state.value) {
                is ContactsScreenStates.Base -> onBack()
                else -> state.value = ContactsScreenStates.Base()
            }
        }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
    when (state.value) {
        is ContactsScreenStates.NewChat -> {
            Column(
                modifier = modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textState = rememberTextFieldState()
                LabeledTextField(textState, "", "Email")
                Button(onClick = {
                    val email = textState.text.toString().trimIndent()
                    if (email.isEmpty()) {
                        state.value = ContactsScreenStates.Error("Email обязателен!")
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
                            partnerEmail = listOf(email),
                            profile = mapOf(email to profile?.serialize())
                        )
                        chatDao?.upsertChat(chat)

                        Repository.setLastContact(chat)
                        withContext(Dispatchers.Main) {
                            onAdd()
                        }
                    }
                }) {
                    Text("Продолжить")
                }
            }
        }

        is ContactsScreenStates.Error -> {
            Text((state.value as ContactsScreenStates.Error).errorText, color = ColorTheme.Warning)
        }

        is ContactsScreenStates.NewGroup -> {
            Column(
                modifier = modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textState = rememberTextFieldState()
                LabeledTextField(textState, "", "Название")
                Button(onClick = {
                    val email = textState.text.toString().trimIndent()
                    if (email.isEmpty()) {
                        state.value = ContactsScreenStates.Error("Название обязательно!")
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

        is ContactsScreenStates.Base -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.clickable { state.value = ContactsScreenStates.NewChat() }) {
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
                Row(modifier = Modifier.clickable { state.value = ContactsScreenStates.NewGroup() }) {
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