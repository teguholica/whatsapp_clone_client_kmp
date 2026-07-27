package com.teguholica.chat.ui.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.data.remote.ContactApi
import com.teguholica.chat.domain.model.ChatType
import com.teguholica.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface NewChatUiState {
    data object Loading : NewChatUiState
    data class Success(
        val contacts: List<ContactDisplay>,
        val searchQuery: String = "",
    ) : NewChatUiState {
        val filtered: List<ContactDisplay>
            get() = if (searchQuery.isBlank()) contacts
            else contacts.filter { it.displayName.contains(searchQuery, ignoreCase = true) }
    }
    data class Error(val message: String) : NewChatUiState
    data class Created(val chatId: String, val chatName: String) : NewChatUiState
}

data class ContactDisplay(
    val id: String,
    val displayName: String,
    val phone: String,
)

class NewChatViewModel(
    private val contactApi: ContactApi,
    private val chatRepository: ChatRepository,
    private val scope: CoroutineScope? = null,
) : ViewModel() {

    private val coroutineScope: CoroutineScope get() = scope ?: viewModelScope

    private val _uiState = MutableStateFlow<NewChatUiState>(NewChatUiState.Loading)
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var contacts: List<ContactDisplay> = emptyList()

    fun loadContacts() {
        coroutineScope.launch {
            _uiState.value = NewChatUiState.Loading
            contactApi.getAll().fold(
                onSuccess = { dtos ->
                    contacts = dtos.map { dto ->
                        ContactDisplay(
                            id = dto.id,
                            displayName = dto.displayName ?: dto.phone,
                            phone = dto.phone,
                        )
                    }
                    _uiState.value = NewChatUiState.Success(
                        contacts = contacts,
                        searchQuery = _searchQuery.value,
                    )
                },
                onFailure = { e ->
                    _uiState.value = NewChatUiState.Error(e.message ?: "Gagal memuat kontak")
                },
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val current = _uiState.value
        if (current is NewChatUiState.Success) {
            _uiState.value = current.copy(searchQuery = query)
        }
    }

    fun createOrNavigate(contactId: String) {
        val existing = chatRepository.getCached().find { chat ->
            chat.type == ChatType.PERSONAL && chat.participants.any { it.id == contactId }
        }
        if (existing != null) {
            _uiState.value = NewChatUiState.Created(existing.id, existing.name)
            return
        }
        coroutineScope.launch {
            _uiState.value = NewChatUiState.Loading
            chatRepository.createPersonalConversation(contactId).fold(
                onSuccess = { chat ->
                    _uiState.value = NewChatUiState.Created(chat.id, chat.name)
                },
                onFailure = { e ->
                    _uiState.value = NewChatUiState.Error(e.message ?: "Gagal membuat percakapan")
                },
            )
        }
    }
}
