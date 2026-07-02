package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isMobile
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Routes
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.welcome_mobile

@Composable
fun WelcomeScreen(navWayObj: NavigationWays) {
    if(isMobile) {
        Image(
            painter = painterResource(Res.drawable.welcome_mobile),
            contentDescription = "Welcome",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(onClick = {
                navWayObj.goTo(Routes.LOGIN_SCREEN)
            }, modifier = Modifier.width(500.dp).height(50.dp)) {
                Text("Начать", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(modifier = Modifier.width(400.dp), text = "Добро пожаловать в Гранат", fontSize = 30.sp, textAlign = TextAlign.Left)
            Button(onClick = {
                navWayObj.goTo(Routes.LOGIN_SCREEN)
            }){
                Text("Начать")
            }
        }
    }
}