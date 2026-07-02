package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.ColorTheme
import org.unstabledev.pomegranate.LabeledTextField
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.database.ChatDC


@Composable
fun ContactsScreen(navWayObj: NavigationWays) {
    var isErrorVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    IconButton(onClick = {navWayObj.goTo(Routes.HOME_SCREEN)}) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
    Column(modifier = Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val state = rememberTextFieldState()
        LabeledTextField(state, "", "Email")
        if(isErrorVisible) Text(errorText, color = ColorTheme.Warning)
        Button(onClick = {
            val email = state.text.toString().trimIndent()
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
            Repository.lastContact = ChatDC(email) to null
            navWayObj.goTo(Routes.CHAT_SCREEN)
        }) {
            Text("Продолжить")
        }
    }
}