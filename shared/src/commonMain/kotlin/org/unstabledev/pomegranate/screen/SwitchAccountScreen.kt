package org.unstabledev.pomegranate.screen

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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.components.LabeledTextField
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.components.chat.ContactRow
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.platform.kmpWriteText

sealed class AccountState {
    class Available : AccountState()
    class Login : AccountState()
}

@Composable
fun SwitchAccountScreen(navController: NavHostController) {
    val state = remember { mutableStateOf<AccountState>(AccountState.Available()) }
    when (state.value) {
        is AccountState.Available->{
            Column(applyScreenPadding()) {
                IconButton(onClick = {navController.popBackStack()}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(Modifier.padding(5.dp).clickable {
                        KMPFile(Repository.fistFilePath).delete()
                        navController.navigate(Routes.Onboarding())
                    }, verticalAlignment = Alignment.CenterVertically) {
                        ContactRow(Repository.myEmail, modifier = Modifier.height(64.dp).fillMaxWidth(0.6f))
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(Modifier.padding(5.dp).clickable {
                        state.value = AccountState.Login()
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("Добавить", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        is AccountState.Login->{
            val fistFilePath = Repository.fistFilePath
            var isErrorVisible by remember { mutableStateOf(false) }
            var errorText by remember { mutableStateOf("") }
            Column(applyScreenPadding()) {
                IconButton(onClick = {state.value=AccountState.Available()}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(modifier = Modifier.width(400.dp), text = "Вход", fontSize = 30.sp, textAlign = TextAlign.Left)
                    val state = rememberTextFieldState()
                    LabeledTextField(state, "", "Email")
                    if(isErrorVisible) Text(errorText, color = ColorTheme.Warning)
                    Button(enabled = !state.text.isEmpty(), onClick = {
                        val email = state.text.toString().trimIndent().lowercase()
                        if(email.isEmpty()) {
                            isErrorVisible = true
                            errorText = "Email пустой"
                            return@Button
                        }
                        /*if(!Util.isValidEmail(email)) {
                            isErrorVisible = true
                            errorText = "Некорректный Email"
                            return@Button
                        }*/
                        KMPFile(fistFilePath).apply {
                            createNewFile()
                            kmpWriteText(email)}
                        navController.navigate(Routes.Home())
                    }){
                        Text("Войти")
                    }
                }
            }
        }
    }
}