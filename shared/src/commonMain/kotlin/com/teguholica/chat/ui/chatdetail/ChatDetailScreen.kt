package com.teguholica.chat.ui.chatdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.teguholica.chat.domain.model.Message
import com.teguholica.chat.domain.model.MessageStatus
import com.teguholica.chat.domain.model.MessageType
import com.teguholica.chat.domain.repository.AuthRepository
import org.koin.compose.koinInject

private val greenBubble = Color(0xFFDCF8C6)
private val whiteBubble = Color(0xFFFFFFFF)
private val checkGray = Color(0xFF8696A0)
private val checkBlue = Color(0xFF53BDEB)

@Composable
fun ChatDetailScreen(
    chatId: String,
    chatName: String,
    personal: Boolean,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = koinInject(),
    authRepository: AuthRepository = koinInject(),
) {
    val currentUserId = authRepository.getSavedUserId()
    val uiState by viewModel.uiState.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val listState = rememberLazyListState()

    DisposableEffect(chatId) {
        viewModel.init(chatId, personal)
        onDispose { viewModel.leaveRoom() }
    }

    LaunchedEffect(uiState) {
        val success = uiState as? ChatDetailUiState.Success
        if (success != null && success.messages.isNotEmpty()) {
            listState.animateScrollToItem(success.messages.size - 1)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ChatDetailHeader(name = chatName, onBack = onBack)

            when (val state = uiState) {
                is ChatDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ChatDetailUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ChatDetailUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFECE5DD))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        state = listState,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (state.hasMore) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { viewModel.loadMore() }) {
                                        Text("Muat lebih banyak")
                                    }
                                }
                            }
                        }

                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                isMine = message.senderId == currentUserId,
                                showSenderName = !personal && message.senderId != currentUserId,
                            )
                        }

                        state.typingUserId?.let { userId ->
                            item {
                                if (personal) TypingBubble(senderName = null)
                                else TypingBubble(senderName = userId)
                            }
                        }
                    }

                    if (isUploading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    MessageInput(
                        text = draft,
                        onTextChange = viewModel::updateDraft,
                        onSend = viewModel::sendMessage,
                        onAttachImage = { viewModel.pickAndSendMedia("image") },
                        onAttachDocument = { viewModel.pickAndSendMedia("document") },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatDetailHeader(name: String, onBack: () -> Unit) {
    Surface(shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("←", fontSize = 18.sp)
            }
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean, showSenderName: Boolean = false) {
    val bgColor = if (isMine) greenBubble else whiteBubble
    val shape = RoundedCornerShape(
        topStart = 12.dp, topEnd = 12.dp,
        bottomStart = if (isMine) 12.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 12.dp,
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bgColor, shape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Column {
                if (showSenderName) {
                    Text(
                        text = message.senderId,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                }

                when (message.type) {
                    MessageType.IMAGE -> MediaPreview(message.content)
                    MessageType.VIDEO -> {
                        Text("[Video]", fontSize = 14.sp, color = Color(0xFF303030))
                        Spacer(Modifier.height(2.dp))
                    }
                    MessageType.DOCUMENT -> {
                        Text("[Dokumen]", fontSize = 14.sp, color = Color(0xFF303030))
                        Spacer(Modifier.height(2.dp))
                    }
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            color = Color(0xFF303030),
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = formatTime(message.createdAt),
                        fontSize = 11.sp,
                        color = checkGray,
                    )
                    if (isMine) {
                        Spacer(Modifier.width(4.dp))
                        StatusIcon(message.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaPreview(content: String) {
    val url = remember(content) { parseMediaUrl(content) }
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = "Media",
            modifier = Modifier
                .widthIn(max = 240.dp)
                .heightIn(max = 240.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    val icon = when (status) {
        MessageStatus.SENT -> "✓"
        MessageStatus.DELIVERED -> "✓✓"
        MessageStatus.READ -> "✓✓"
    }
    val color = when (status) {
        MessageStatus.READ -> checkBlue
        else -> checkGray
    }
    Text(icon, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
}

@Composable
private fun TypingBubble(senderName: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(whiteBubble, RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val text = if (senderName != null) "$senderName sedang mengetik..." else "sedang mengetik..."
            Text(text, fontSize = 14.sp, color = checkGray)
        }
    }
}

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachImage: () -> Unit,
    onAttachDocument: () -> Unit,
) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onAttachImage, modifier = Modifier.size(40.dp)) {
                Text("📷", fontSize = 18.sp)
            }
            TextButton(onClick = onAttachDocument, modifier = Modifier.size(40.dp)) {
                Text("📎", fontSize = 18.sp)
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Ketik pesan...") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onSend,
                enabled = text.isNotBlank(),
            ) {
                Text("Kirim")
            }
        }
    }
}

private fun formatTime(iso: String): String {
    val parts = iso.split("T")
    if (parts.size < 2) return iso
    return parts[1].take(5)
}
