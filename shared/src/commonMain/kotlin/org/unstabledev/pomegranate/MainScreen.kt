package org.unstabledev.pomegranate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.database.ChatDao
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.menu
import pomegranate.shared.generated.resources.test_avatar

@Composable
fun MainScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    val viewModel = viewModel { MainScreenController(chatDao) }
    val scope = rememberCoroutineScope()
    scope.launch {
        viewModel.update()
    }
    val chats = viewModel.chats.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF))) {
        Row(
            modifier = Modifier.height(50.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                painter = painterResource(Res.drawable.menu),
                contentDescription = "menu"
            )
            Column(
                modifier = Modifier.height(50.dp).width(30.dp)
                    .clickable { navWayObj.goTo(Routes.CONTACTS_SCREEN_ROUTE) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "+", color = Color.Black, fontSize = 25.sp)
            }
            val state = rememberTextFieldState()
            MyTextField(state, "Поиск")
        }
        if (chats.value.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0x12345678))) {
                items(chats.value) { chat ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                            .clickable
                            {
                                Repository.lastContact = chat to Repository.availableChats[chat]
                                navWayObj.goTo(Routes.CHAT_SCREEN_ROUTE)
                            }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(54.dp).padding(2.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                painter = painterResource(Res.drawable.test_avatar),
                                contentDescription = "avatar",
                            )
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(chat.partnerEmail)
                                Text(
                                    text = "zzzzzzzzzzzzzzzzzzzz", style = TextStyle(
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Вы еще ни с кем не общались! Завидите новый чат.")
            }
        }
    }

}