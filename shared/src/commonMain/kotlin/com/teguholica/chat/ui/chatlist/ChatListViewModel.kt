package com.teguholica.chat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.data.remote.ws.WsClient
import com.teguholica.chat.data.remote.ws.WsEvent
import com.teguholica.chat.domain.model.Chat
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChatListUiState {
    data object Loading : ChatListUiState
    data class Success(
        val chats: List<Chat>,
        val typingMap: Map<String, Boolean> = emptyMap(),
    ) : ChatListUiState
    data class Error(val message: String) : ChatListUiState
}

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val wsClient: WsClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChats()
        observeWsEvents()
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = ChatListUiState.Loading
            val result = chatRepository.getAll()
            _uiState.value = result.fold(
                onSuccess = { chats ->
                    ChatListUiState.Success(chats = chats)
                },
                onFailure = { e ->
                    val cached = chatRepository.getCached()
                    if (cached.isNotEmpty()) {
                        ChatListUiState.Success(chats = cached)
                    } else {
                        ChatListUiState.Error(e.message ?: "Gagal memuat chat")
                    }
                },
            )
        }
    }

    fun selectChat(chatId: String) {
        chatRepository.clearUnread(chatId)
    }

    private fun observeWsEvents() {
        viewModelScope.launch {
            wsClient.events.collect { event ->
                when (event) {
                    is WsEvent.MessageNew -> {
                        val msg = event.data
                        chatRepository.updateLastMessage(
                            conversationId = msg.conversationId,
                            senderId = msg.senderId,
                            content = msg.content,
                            timestamp = msg.createdAt,
                        )
                        chatRepository.incrementUnread(msg.conversationId)

                        val chats = chatRepository.getCached()
                        _uiState.value = ChatListUiState.Success(chats = chats)
                    }
                    is WsEvent.Presence -> {
                        val p = event.data
                        chatRepository.updatePresenceCache(
                            userId = p.userId,
                            online = p.status == "online",
                            lastSeenAt = p.lastSeenAt,
                        )
                        val chats = chatRepository.getCached()
                        _uiState.value = ChatListUiState.Success(chats = chats)
                    }
                    is WsEvent.Typing -> {
                        val current = _uiState.value
                        if (current is ChatListUiState.Success) {
                            val typingMap = current.typingMap + (event.data.conversationId to true)
                            _uiState.value = current.copy(typingMap = typingMap)
                        }
                    }
                    is WsEvent.TypingStop -> {
                        val current = _uiState.value
                        if (current is ChatListUiState.Success) {
                            val typingMap = current.typingMap - event.data.conversationId
                            _uiState.value = current.copy(typingMap = typingMap)
                        }
                    }
                    is WsEvent.Connected -> {
                        // reconnect WS — refresh daftar chat
                        loadChats()
                    }
                    else -> {}
                }
            }
        }
    }

    fun logout() {
        wsClient.disconnect()
        authRepository.logout()
    }
}
