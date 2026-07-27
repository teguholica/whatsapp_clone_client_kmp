package com.teguholica.chat.ui.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teguholica.chat.domain.model.Chat
import com.teguholica.chat.domain.model.ChatType
import com.teguholica.chat.domain.model.MessageType
import com.teguholica.chat.domain.model.PresenceStatus
import org.koin.compose.koinInject

@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    onLogout: () -> Unit,
    onCreateGroup: () -> Unit,
    viewModel: ChatListViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ChatListUiState.Error) {
            val msg = (uiState as ChatListUiState.Error).message
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            is ChatListUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ChatListUiState.Success -> {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    ChatListHeader(onLogout = onLogout, onCreateGroup = onCreateGroup)
                    if (state.chats.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada chat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn {
                            items(state.chats, key = { it.id }) { chat ->
                                ChatListItem(
                                    chat = chat,
                                    isTyping = state.typingMap[chat.id] == true,
                                    onClick = {
                                        viewModel.selectChat(chat.id)
                                        onChatClick(chat.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            is ChatListUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadChats() }) {
                        Text("Coba lagi")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListHeader(onLogout: () -> Unit, onCreateGroup: () -> Unit) {
    Surface(shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Chat", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onCreateGroup) {
                Text("Grup Baru")
            }
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    isTyping: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarWithPresence(chat = chat)

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                chat.lastMessage?.let { msg ->
                    Text(
                        text = formatTimestamp(msg.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTyping) {
                    Text(
                        text = "sedang mengetik...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    chat.lastMessage?.let { msg ->
                        Text(
                            text = messagePreview(msg.type, msg.content),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (chat.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Badge {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarWithPresence(chat: Chat) {
    Box {
        val avatarText = if (chat.type == ChatType.GROUP) {
            val words = chat.name.split(" ").filter { it.isNotBlank() }
            if (words.size >= 2) "${words[0].first().uppercase()}${words[1].first().uppercase()}"
            else words.firstOrNull()?.take(2)?.uppercase() ?: "G"
        } else {
            chat.name.firstOrNull()?.uppercase() ?: "?"
        }
        val avatarBg = if (chat.type == ChatType.GROUP) Color(0xFF25D366) else MaterialTheme.colorScheme.primaryContainer
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center,
        ) {
            val textColor = if (chat.type == ChatType.GROUP) Color.White else Color.Unspecified
            Text(avatarText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        }

        if (chat.type == ChatType.PERSONAL) {
            val online = chat.participants.any { it.presence?.status == PresenceStatus.ONLINE }
            val dotColor = if (online) Color(0xFF25D366) else Color(0xFFA0A0A0)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .align(Alignment.BottomEnd),
            )
        }
    }
}

private fun formatTimestamp(iso: String): String {
    return iso.takeLast(5).take(5)
}

private fun messagePreview(type: MessageType, content: String): String = when (type) {
    MessageType.TEXT -> content
    MessageType.IMAGE -> "Foto"
    MessageType.VIDEO -> "Video"
    MessageType.DOCUMENT -> "Dokumen"
}
