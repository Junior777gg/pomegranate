package org.unstabledev.pomegranate

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App(chatDao, messagesDao) }