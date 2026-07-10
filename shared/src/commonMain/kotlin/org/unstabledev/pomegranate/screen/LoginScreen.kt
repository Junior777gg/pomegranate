package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.unstabledev.pomegranate.ColorTheme
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.LabeledTextField
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.isMobile

@Composable
fun LoginScreen(navWayObj: NavigationWays) {
    val fistFilePath = "pomegranate${File.sep}auth.txt"
    var isErrorVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    IconButton(onClick = {navWayObj.goTo(Routes.WELCOME_SCREEN)}) {
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
            val file = File(fistFilePath)
            file.writeText(email)
            navWayObj.goTo(Routes.HOME_SCREEN)
        }){
            Text("Войти")
        }
    }
}