package com.teguholica.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.ui.auth.AuthScreen
import com.teguholica.chat.ui.chatdetail.ChatDetailScreen
import com.teguholica.chat.ui.chatlist.ChatListScreen
import com.teguholica.chat.ui.creategroup.CreateGroupScreen
import com.teguholica.chat.ui.newchat.NewChatScreen
import com.teguholica.chat.ui.theme.ChatTheme
import org.koin.compose.koinInject

sealed class Screen {
    object Auth : Screen()
    object ChatList : Screen()
    data class ChatDetail(val chatId: String, val chatName: String, val personal: Boolean) : Screen()
    object CreateGroup : Screen()
    object NewChat : Screen()
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
                    when (val current = screen) {
                        Screen.Auth -> AuthScreen(
                            onAuthenticated = { screen = Screen.ChatList },
                        )
                        Screen.ChatList -> ChatListScreen(
                            onChatClick = { id, name, personal ->
                                screen = Screen.ChatDetail(id, name, personal)
                            },
                            onLogout = {
                                authRepository.logout()
                                screen = Screen.Auth
                            },
                            onCreateGroup = { screen = Screen.CreateGroup },
                            onNewChat = { screen = Screen.NewChat },
                        )
                        is Screen.ChatDetail -> ChatDetailScreen(
                            chatId = current.chatId,
                            chatName = current.chatName,
                            personal = current.personal,
                            onBack = { screen = Screen.ChatList },
                        )
                        Screen.CreateGroup -> CreateGroupScreen(
                            onCreated = { screen = Screen.ChatList },
                            onBack = { screen = Screen.ChatList },
                        )
                        Screen.NewChat -> NewChatScreen(
                            onContactSelected = { chatId, chatName ->
                                screen = Screen.ChatDetail(chatId, chatName, true)
                            },
                            onBack = { screen = Screen.ChatList },
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
