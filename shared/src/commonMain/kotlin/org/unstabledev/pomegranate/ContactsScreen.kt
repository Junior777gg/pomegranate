package org.unstabledev.pomegranate

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.database.ChatDC


@Composable
fun ContactsScreen(navWayObj: NavigationWays) {

    val state = rememberTextFieldState()
    MyTextField(state, "", "Email")
    Button(onClick = {
        Repository.lastContact = ChatDC(state.text.toString()) to null
        navWayObj.goTo(Routes.CHAT_SCREEN_ROUTE)
    }){
        Text("Продолжить")
    }
}