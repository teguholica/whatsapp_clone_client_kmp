package com.teguholica.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.ui.auth.AuthScreen
import com.teguholica.chat.ui.chatlist.ChatListScreen
import com.teguholica.chat.ui.theme.ChatTheme
import org.koin.compose.koinInject

sealed class Screen {
    object Auth : Screen()
    object ChatList : Screen()
}

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf<Screen>(Screen.Auth) }
    val authRepository: AuthRepository = koinInject()

    LaunchedEffect(Unit) {
        if (authRepository.isLoggedIn()) {
            screen = Screen.ChatList
        }
    }

    ChatTheme(darkTheme = darkTheme) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Box(Modifier.fillMaxSize().weight(1f)) {
                    when (screen) {
                        Screen.Auth -> AuthScreen(
                            onAuthenticated = { screen = Screen.ChatList },
                        )
                        Screen.ChatList -> ChatListScreen(
                            onChatClick = { _ -> },
                            onLogout = {
                                authRepository.logout()
                                screen = Screen.Auth
                            },
                            onCreateGroup = { },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { darkTheme = !darkTheme }) {
                        val label = if (darkTheme) "Mode Terang" else "Mode Gelap"
                        Text(label)
                    }
                }
            }
        }
    }
}
