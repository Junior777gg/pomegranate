package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.platform.isMobile
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.components.LabeledTextField
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.kmpWriteText
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.welcome_mobile

sealed class OnboardingState {
    class Welcome : OnboardingState()
    class Login : OnboardingState()
}

@Composable
fun WelcomeScreen(navController: NavHostController) {
    val state = remember { mutableStateOf<OnboardingState>(OnboardingState.Welcome()) }
    when (state.value) {
        is OnboardingState.Welcome->{
            if(isMobile) {
                Image(
                    painter = painterResource(Res.drawable.welcome_mobile),
                    contentDescription = "Добро пожаловать",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = { state.value = OnboardingState.Login() },
                        modifier = Modifier.width(500.dp).height(50.dp)) {
                        Text("Начать", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column {
                        Text("Добро пожаловать в",
                            fontSize = 30.sp, textAlign = TextAlign.Left)
                        Text("Гранат", color = MaterialTheme.colorScheme.primary,
                            fontSize = 30.sp, textAlign = TextAlign.Left, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { state.value = OnboardingState.Login() }) { Text("Начать") }
                }
            }
        }

        is OnboardingState.Login->{
            val fistFilePath = Repository.fistFilePath
            var isErrorVisible by remember { mutableStateOf(false) }
            var errorText by remember { mutableStateOf("") }
            Column(applyScreenPadding()) {
                IconButton(onClick = { state.value = OnboardingState.Welcome() }) {
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
                    Text("Войдите в аккаунт", fontSize = 30.sp)
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
                            kmpWriteText(email)
                        }
                        navController.navigate(Routes.Home())
                    }){ Text("Войти") }
                }
            }
        }
    }
}