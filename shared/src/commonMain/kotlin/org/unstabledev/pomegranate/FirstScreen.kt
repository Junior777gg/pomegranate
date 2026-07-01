package org.unstabledev.pomegranate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FirstScreen(navWayObj: NavigationWays) {
    val fistFilePath = "pomegranate${File.sep}firstFile.txt"
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(modifier = Modifier.width(400.dp), text = "Здравствуйте войдите через почту.", fontSize = 30.sp)
        val state = rememberTextFieldState()
        MyTextField(state, "", "Email")
        Button(onClick = {
            val file = File(fistFilePath)
            file.writeText(state.text.toString().trimIndent())
            navWayObj.goTo(Routes.HOME_SCREEN_ROUTE)
        }){
            Text("Войти")
        }
    }
}