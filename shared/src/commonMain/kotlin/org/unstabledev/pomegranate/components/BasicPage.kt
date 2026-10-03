package org.unstabledev.pomegranate.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding

@Composable
fun ScrollablePage(navController: NavHostController, header: String, content: @Composable (LazyItemScope.() -> Unit)) {
    BasicPage(Modifier, Arrangement.Top, header, {
        navController.popBackStack()
        AppSettings.save()
    }) {
        LazyColumn {
            item {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BasicPage(modifier: Modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              header: String, navController: NavHostController, content: @Composable (ColumnScope.() -> Unit)) {
    BasicPage(modifier, Arrangement.Center, header, {navController.popBackStack()}, content)
}

@Composable
fun BasicPage(modifier: Modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), vertical: Arrangement.Vertical = Arrangement.Center,
              header: String, onBack: ()->Unit, content: @Composable (ColumnScope.() -> Unit)) {
    Column(applyScreenPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = header,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = vertical) {
            content()
        }
    }
}