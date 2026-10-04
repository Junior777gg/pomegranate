package org.unstabledev.pomegranate.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.components.LabeledTextField
import org.unstabledev.pomegranate.components.BasicPage
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.screen.control.ContactScreenController

sealed class ContactsScreenState {
    class CreateGroup : ContactsScreenState()
    class AddContact : ContactsScreenState()
}

@Composable
fun ContactsScreen(navController: NavHostController) {
    Column(applyScreenPadding()) {
        ContactsPanel(
            { navController.popBackStack() },
            { navController.navigate(Routes.ChatScreen(it.key)) },
        )
    }
}

@Composable
fun ContactsPanel(
    onBack: () -> Unit,
    onAdd: (chat: ChatDC) -> Unit
) {
    val viewModel = viewModel {
        ContactScreenController()
    }
    val state = remember { mutableStateOf<ContactsScreenState>(ContactsScreenState.AddContact()) }
    val error = remember { mutableStateOf("") }
    val pageHeader = remember { mutableStateOf("Создать чат") }
    BasicPage(header = pageHeader.value, onBack = {
        error.value = ""
        when (state.value) {
            is ContactsScreenState.AddContact -> onBack()
            else -> state.value = ContactsScreenState.AddContact()
        }
    }) {
        when (state.value) {
            is ContactsScreenState.CreateGroup -> {
                pageHeader.value = "Новая группа"
                val textState = rememberTextFieldState()
                if (!error.value.isBlank()) {
                    Text(error.value, color = MaterialTheme.colorScheme.error)
                }
                Column {
                    LabeledTextField(textState, "", "Название", singleLineIn = true)
                    Button(enabled = !textState.text.isBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
                        val name = textState.text.toString().trimIndent()
                        if (name.isEmpty()) {
                            error.value = "Название обязательно!"
                            return@Button
                        }
                        val chat = viewModel.createGroups(name)
                        onAdd(chat)
                    }) {
                        Text("Создать группу")
                    }
                }
            }

            is ContactsScreenState.AddContact -> {
                Row(modifier = Modifier.clickable { state.value = ContactsScreenState.CreateGroup() }) {
                    Box(
                        Modifier.background(Color(120, 219, 226), shape = RoundedCornerShape(3.dp))
                            .padding(all = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Создать групповой чат")
                }
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val textState = rememberTextFieldState()
                    if (!error.value.isBlank()) {
                        Text(error.value, color = MaterialTheme.colorScheme.error)
                    }
                    Row(
                        Modifier.height(35.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth().height(34.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                ).padding(horizontal = 16.dp, vertical = 8.dp).weight(1.0f),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            state = textState,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                        AnimatedVisibility(!textState.text.isBlank()) {
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
                                val chat = viewModel.createChat(email)
                                onAdd(chat)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
