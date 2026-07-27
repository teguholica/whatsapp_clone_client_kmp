package com.teguholica.chat.ui.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.data.remote.UserApi
import com.teguholica.chat.data.remote.dto.UserProfileDto
import com.teguholica.chat.domain.model.ChatType
import com.teguholica.chat.domain.model.Contact
import com.teguholica.chat.domain.repository.ChatRepository
import com.teguholica.chat.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface NewChatUiState {
    data object Loading : NewChatUiState
    data class Success(
        val contacts: List<ContactDisplay>,
        val searchQuery: String = "",
    ) : NewChatUiState {
        val filtered: List<ContactDisplay>
            get() = if (searchQuery.isBlank()) contacts
            else contacts.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.userId?.contains(searchQuery, ignoreCase = true) == true
            }
    }
    data class Error(val message: String) : NewChatUiState
    data class Created(val chatId: String, val chatName: String) : NewChatUiState
}

data class ContactDisplay(
    val id: String,
    val displayName: String,
    val phone: String,
    val userId: String? = null,
)

class NewChatViewModel(
    private val contactRepository: ContactRepository,
    private val chatRepository: ChatRepository,
    private val userApi: UserApi,
    private val scope: CoroutineScope? = null,
) : ViewModel() {

    private val coroutineScope: CoroutineScope get() = scope ?: viewModelScope

    private val _uiState = MutableStateFlow<NewChatUiState>(NewChatUiState.Loading)
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private var contacts: List<ContactDisplay> = emptyList()
    private var serverUsers: Map<String, UserProfileDto> = emptyMap()

    fun loadContacts() {
        coroutineScope.launch {
            _uiState.value = NewChatUiState.Loading
            contacts = contactRepository.getAll().map { contact ->
                ContactDisplay(
                    id = contact.id,
                    displayName = contact.displayName,
                    phone = contact.phone,
                )
            }
            _uiState.value = NewChatUiState.Success(
                contacts = contacts,
                searchQuery = _searchQuery.value,
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            val current = _uiState.value
            if (current is NewChatUiState.Success) {
                _uiState.value = current.copy(searchQuery = query)
            }
            return
        }
        coroutineScope.launch {
            val serverResult = userApi.searchByPhone(query)
            val serverContacts = serverResult.getOrDefault(emptyList()).map { user ->
                ContactDisplay(
                    id = user.id,
                    displayName = user.displayName ?: user.phone,
                    phone = user.phone,
                    userId = user.id,
                )
            }
            serverUsers = serverUsers + serverResult.getOrDefault(emptyList()).associateBy { it.id }
            val merged = (contacts.filter { it.phone.contains(query, ignoreCase = true) || it.displayName.contains(query, ignoreCase = true) } + serverContacts).distinctBy { it.phone }
            val current = _uiState.value
            if (current is NewChatUiState.Success) {
                _uiState.value = current.copy(searchQuery = query)
            }
            contacts = merged
            _uiState.value = NewChatUiState.Success(
                contacts = contacts,
                searchQuery = _searchQuery.value,
            )
        }
    }

    fun createOrNavigate(phone: String) {
        coroutineScope.launch {
            val byPhone = chatRepository.getCached().find { chat ->
                chat.type == ChatType.PERSONAL && chat.participants.any { it.phone == phone }
            }
            if (byPhone != null) {
                _uiState.value = NewChatUiState.Created(byPhone.id, byPhone.name)
                return@launch
            }
            val contact = contacts.find { it.phone == phone }
            if (contact != null && contact.userId == null) {
                val lookup = userApi.searchByPhone(phone).getOrDefault(emptyList())
                if (lookup.isEmpty()) {
                    _uiState.value = NewChatUiState.Error("Nomor telepon tidak terdaftar")
                    return@launch
                }
            }
            _uiState.value = NewChatUiState.Loading
            chatRepository.createPersonalConversation(phone).fold(
                onSuccess = { chat ->
                    _uiState.value = NewChatUiState.Created(chat.id, chat.name)
                },
                onFailure = { e ->
                    _uiState.value = NewChatUiState.Error(e.message ?: "Gagal membuat percakapan")
                },
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addContact(displayName: String, phone: String) {
        if (displayName.isBlank() || phone.isBlank()) return
        val contact = Contact(
            id = Uuid.random().toString(),
            phone = phone,
            displayName = displayName,
        )
        coroutineScope.launch {
            contactRepository.save(contact)
            loadContacts()
        }
        _showAddDialog.value = false
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }
}
