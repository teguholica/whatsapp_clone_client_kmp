package com.teguholica.chat.ui.chatdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.MediaPicker
import com.teguholica.chat.data.remote.MediaUploadApi
import com.teguholica.chat.data.remote.MessageApi
import com.teguholica.chat.data.remote.dto.MessageResponseDto
import com.teguholica.chat.data.remote.ws.MessageNewData
import com.teguholica.chat.data.remote.ws.WsClient
import com.teguholica.chat.data.remote.ws.WsEvent
import com.teguholica.chat.domain.model.*
import com.teguholica.chat.domain.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChatDetailUiState {
    data object Loading : ChatDetailUiState
    data class Success(
        val messages: List<Message>,
        val hasMore: Boolean = true,
        val typingUserId: String? = null,
    ) : ChatDetailUiState
    data class Error(val message: String) : ChatDetailUiState
}

class ChatDetailViewModel(
    private val messageApi: MessageApi,
    private val authRepository: AuthRepository,
    private val wsClient: WsClient,
    private val mediaUploadApi: MediaUploadApi,
    private val mediaPicker: MediaPicker,
) : ViewModel() {

    private var conversationId: String = ""
    private var isPersonal = false

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private var isLoadingMore = false
    private var typingJob: Job? = null

    fun init(chatId: String, personal: Boolean) {
        if (conversationId == chatId) return
        conversationId = chatId
        isPersonal = personal
        loadMessages()
        joinRoom()
        markAsRead()
    }

    private fun loadMessages(before: String? = null) {
        viewModelScope.launch {
            val token = authRepository.getSavedAccessToken() ?: return@launch
            val result = messageApi.getMessages(token, conversationId, before = before)
            result.fold(
                onSuccess = { dtos ->
                    val msgs = dtos.map { it.toDomain(conversationId) }
                    val current = _uiState.value
                    val existing = if (current is ChatDetailUiState.Success) current.messages else emptyList()
                    _uiState.value = ChatDetailUiState.Success(
                        messages = if (before != null) existing + msgs else msgs,
                        hasMore = msgs.size >= 50,
                    )
                },
                onFailure = { e ->
                    _uiState.value = ChatDetailUiState.Error(e.message ?: "Gagal memuat pesan")
                },
            )
        }
    }

    fun loadMore() {
        if (isLoadingMore) return
        val state = _uiState.value
        if (state !is ChatDetailUiState.Success || !state.hasMore) return
        isLoadingMore = true
        val oldestId = state.messages.minOfOrNull { it.id }
        loadMessages(before = oldestId)
        isLoadingMore = false
    }

    fun sendMessage() {
        val text = _draft.value.trim()
        if (text.isEmpty()) return
        _draft.value = ""

        viewModelScope.launch {
            val token = authRepository.getSavedAccessToken() ?: return@launch
            messageApi.sendMessage(token, conversationId, "text", text)
        }
    }

    fun updateDraft(text: String) {
        _draft.value = text
        if (text.isNotEmpty()) {
            startTyping()
        } else {
            stopTyping()
        }
    }

    private fun startTyping() {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            wsClient.sendTypingStart(conversationId)
            delay(4000)
            wsClient.sendTypingStop(conversationId)
        }
    }

    private fun stopTyping() {
        typingJob?.cancel()
        viewModelScope.launch { wsClient.sendTypingStop(conversationId) }
    }

    fun pickAndSendMedia(type: String) {
        viewModelScope.launch {
            val file = when (type) {
                "image" -> mediaPicker.pickImage()
                "video" -> mediaPicker.pickVideo()
                else -> mediaPicker.pickDocument()
            } ?: return@launch

            _isUploading.value = true
            val token = authRepository.getSavedAccessToken() ?: return@launch
            val result = mediaUploadApi.upload(token, file)
            result.fold(
                onSuccess = { media ->
                    val content = """{"mediaId":"${media.id}","url":"${media.url}"}"""
                    val msgType = when {
                        file.mimeType.startsWith("image") -> "image"
                        file.mimeType.startsWith("video") -> "video"
                        else -> "document"
                    }
                    messageApi.sendMessage(token, conversationId, msgType, content)
                },
                onFailure = {
                    _uiState.value = ChatDetailUiState.Error(it.message ?: "Gagal upload")
                },
            )
            _isUploading.value = false
        }
    }

    private fun joinRoom() {
        viewModelScope.launch {
            wsClient.joinRoom(conversationId)
            observeRoomEvents()
        }
    }

    fun leaveRoom() {
        viewModelScope.launch { wsClient.leaveRoom(conversationId) }
    }

    private suspend fun observeRoomEvents() {
        wsClient.events.collect { event ->
            when (event) {
                is WsEvent.MessageNew -> {
                    val d = event.data
                    if (d.conversationId == conversationId) {
                        val msg = d.toDomain()
                        addMessage(msg)
                    }
                }
                is WsEvent.MessageStatus -> {
                    val d = event.data
                    updateMessageStatus(d.messageId, d.status)
                }
                is WsEvent.MessageDeleted -> {
                    removeMessage(event.data.messageId)
                }
                is WsEvent.Typing -> {
                    if (event.data.conversationId == conversationId && isPersonal) {
                        val current = _uiState.value
                        if (current is ChatDetailUiState.Success) {
                            _uiState.value = current.copy(typingUserId = event.data.userId)
                        }
                    }
                }
                is WsEvent.TypingStop -> {
                    if (event.data.conversationId == conversationId) {
                        val current = _uiState.value
                        if (current is ChatDetailUiState.Success) {
                            _uiState.value = current.copy(typingUserId = null)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun addMessage(msg: Message) {
        val current = _uiState.value
        if (current is ChatDetailUiState.Success) {
            val exists = current.messages.any { it.id == msg.id }
            if (!exists) {
                _uiState.value = current.copy(messages = current.messages + msg)
            }
        }
    }

    private fun updateMessageStatus(messageId: String, status: String) {
        val current = _uiState.value
        if (current is ChatDetailUiState.Success) {
            val msgs = current.messages.map { msg ->
                if (msg.id == messageId) {
                    val s = when (status.uppercase()) {
                        "DELIVERED" -> MessageStatus.DELIVERED
                        "READ" -> MessageStatus.READ
                        else -> MessageStatus.SENT
                    }
                    msg.copy(status = s)
                } else msg
            }
            _uiState.value = current.copy(messages = msgs)
        }
    }

    private fun removeMessage(messageId: String) {
        val current = _uiState.value
        if (current is ChatDetailUiState.Success) {
            _uiState.value = current.copy(messages = current.messages.filter { it.id != messageId })
        }
    }

    private fun markAsRead() {
        if (!isPersonal) return
        viewModelScope.launch {
            val current = _uiState.value
            if (current is ChatDetailUiState.Success) {
                val unreadIds = current.messages
                    .filter { it.status != MessageStatus.READ && it.senderId != authRepository.getSavedUserId() }
                    .map { it.id }
                unreadIds.forEach { wsClient.sendMessageRead(it) }
            }
        }
    }
}

private fun MessageResponseDto.toDomain(cId: String? = null): Message {
    val t = when (type.uppercase()) {
        "IMAGE" -> MessageType.IMAGE
        "VIDEO" -> MessageType.VIDEO
        "DOCUMENT" -> MessageType.DOCUMENT
        else -> MessageType.TEXT
    }
    val s = when (status.uppercase()) {
        "DELIVERED" -> MessageStatus.DELIVERED
        "READ" -> MessageStatus.READ
        else -> MessageStatus.SENT
    }
    return Message(
        id = id,
        chatId = cId ?: conversationId,
        senderId = senderId,
        type = t,
        content = content,
        status = s,
        createdAt = createdAt,
    )
}

private fun MessageNewData.toDomain(): Message {
    val t = when (type.uppercase()) {
        "IMAGE" -> MessageType.IMAGE
        "VIDEO" -> MessageType.VIDEO
        "DOCUMENT" -> MessageType.DOCUMENT
        else -> MessageType.TEXT
    }
    return Message(
        id = id,
        chatId = conversationId,
        senderId = senderId,
        type = t,
        content = content,
        status = MessageStatus.SENT,
        createdAt = createdAt,
    )
}
