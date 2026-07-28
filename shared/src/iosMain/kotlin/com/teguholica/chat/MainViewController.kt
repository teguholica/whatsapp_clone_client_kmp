package com.teguholica.chat

import androidx.compose.ui.window.ComposeUIViewController
import com.teguholica.chat.data.local.IosTokenStorage
import com.teguholica.chat.data.local.DatabaseDriverFactory
import com.teguholica.chat.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin(IosTokenStorage(), MediaPicker(), DatabaseDriverFactory())
    App()
}