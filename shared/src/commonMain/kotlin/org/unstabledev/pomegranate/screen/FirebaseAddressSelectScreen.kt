package org.unstabledev.pomegranate.screen

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.FirebaseAddress
import org.unstabledev.pomegranate.NavigationWays

@Composable
fun FirebaseAddressSelectScreen(navWayObj: NavigationWays) {
    val settings by AppSettings.state.collectAsState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {navWayObj.back()}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Адрес Firebase",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                FirebaseAddressPanel(
                    addresses = settings.firebaseAddresses,
                    selectedId = settings.selectedFirebaseAddressId,
                    onSelect = AppSettings::selectFirebaseAddress,
                    onAdd = AppSettings::addFirebaseAddress,
                    onUpdate = AppSettings::updateFirebaseAddress,
                    onRemove = AppSettings::removeFirebaseAddress
                )
            }
        }
    }
}

private enum class FirebaseConnectivity {
    CHECKING,
    AVAILABLE,
    UNAVAILABLE
}

@Composable
private fun FirebaseAddressPanel(
    addresses: List<FirebaseAddress>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onUpdate: (String, String, String) -> Unit,
    onRemove: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAddressId by remember { mutableStateOf<String?>(null) }

    val editingAddress = addresses.firstOrNull {
        it.id == editingAddressId
    }

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        addresses.forEach { address ->
            key(address.id) {
                FirebaseAddressRow(
                    address = address,
                    selected = address.id == selectedId,
                    onSelect = {
                        onSelect(address.id)
                    },
                    onShowInfo = {
                        editingAddressId = address.id
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                    showAddDialog = true
                }
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Добавить адрес",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showAddDialog) {
        FirebaseAddressEditorDialog(
            dialogTitle = "Новый Firebase адрес",
            confirmText = "Добавить",
            initialTitle = "",
            initialUrl = "",
            onDismiss = {
                showAddDialog = false
            },
            onSave = { title, url ->
                onAdd(title, url)
                showAddDialog = false
            }
        )
    }

    editingAddress?.let { address ->
        key(address.id, address.url) {
            FirebaseAddressEditorDialog(
                dialogTitle = "Firebase адрес",
                confirmText = "Сохранить",
                initialTitle = address.title,
                initialUrl = address.url,
                onDismiss = {
                    editingAddressId = null
                },
                onSave = { title, url ->
                    onUpdate(address.id, title, url)
                    editingAddressId = null
                },
                onDelete = if (address.id != "default") {
                    {
                        onRemove(address.id)
                        editingAddressId = null
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun FirebaseAddressRow(
    address: FirebaseAddress,
    selected: Boolean,
    onSelect: () -> Unit,
    onShowInfo: () -> Unit
) {
    val connectivity by produceState(
        initialValue = FirebaseConnectivity.CHECKING,
        key1 = address.id,
        key2 = address.url
    ) {
        value = FirebaseConnectivity.CHECKING

        value = if (Firebase.isAvailable(address.url)) {
            FirebaseConnectivity.AVAILABLE
        } else {
            FirebaseConnectivity.UNAVAILABLE
        }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onSelect),
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null
            )
        },
        headlineContent = {
            Text(
                text = address.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        },
        supportingContent = {
            FirebaseConnectivityLabel(connectivity)
        },
        trailingContent = {
            if (address.id=="default") return@ListItem
            IconButton(onClick = onShowInfo) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Информация и редактирование",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
private fun FirebaseConnectivityLabel(
    connectivity: FirebaseConnectivity
) {
    val availableColor = Color(0xFF43A047)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        when (connectivity) {
            FirebaseConnectivity.CHECKING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Проверка подключения…",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FirebaseConnectivity.AVAILABLE -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(availableColor)
                )

                Text(
                    text = "Доступен",
                    color = availableColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FirebaseConnectivity.UNAVAILABLE -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )

                Text(
                    text = "Недоступен",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FirebaseAddressEditorDialog(
    dialogTitle: String,
    confirmText: String,
    initialTitle: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(initialTitle) {
        mutableStateOf(initialTitle)
    }

    var url by remember(initialUrl) {
        mutableStateOf(initialUrl)
    }

    val trimmedUrl = url.trim()

    val validUrl = trimmedUrl.startsWith("https://") ||
            trimmedUrl.startsWith("http://")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Название")
                    }
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = url.isNotBlank() && !validUrl,
                    label = {
                        Text("Firebase URL")
                    },
                    supportingText = {
                        if (url.isNotBlank() && !validUrl) {
                            Text(
                                text = "Адрес должен начинаться с http:// или https://",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Укажите адрес Realtime Database без .json"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = validUrl,
                onClick = {
                    onSave(title.trim(), trimmedUrl)
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = "Удалить",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}