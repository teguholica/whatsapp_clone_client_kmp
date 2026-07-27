package com.teguholica.chat

import androidx.compose.ui.window.ComposeUIViewController
import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin(TokenStorage(), MediaPicker())
    App()
}