package com.teguholica.chat.ui.creategroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.data.remote.GroupApi
import com.teguholica.chat.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreateGroupUiState {
    data object Idle : CreateGroupUiState
    data object Creating : CreateGroupUiState
    data class Success(val conversationId: String) : CreateGroupUiState
    data class Error(val message: String) : CreateGroupUiState
}

class CreateGroupViewModel(
    private val groupApi: GroupApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateGroupUiState>(CreateGroupUiState.Idle)
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _contacts = MutableStateFlow(
        listOf(
            User(id = "contact_1", phone = "+628111111111", displayName = "Kontak Demo 1"),
            User(id = "contact_2", phone = "+628222222222", displayName = "Kontak Demo 2"),
        )
    )
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun toggleContact(userId: String) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(userId)) current.remove(userId) else current.add(userId)
        _selectedIds.value = current
    }

    fun createGroup() {
        val name = _groupName.value.trim()
        val ids = _selectedIds.value.toList()
        if (name.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Nama grup tidak boleh kosong")
            return
        }
        if (ids.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Pilih minimal 1 peserta")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateGroupUiState.Creating
            val result = groupApi.create(name, ids)
            _uiState.value = result.fold(
                onSuccess = { group ->
                    CreateGroupUiState.Success(conversationId = group.id)
                },
                onFailure = { e ->
                    CreateGroupUiState.Error(e.message ?: "Gagal buat grup")
                },
            )
        }
    }
}
