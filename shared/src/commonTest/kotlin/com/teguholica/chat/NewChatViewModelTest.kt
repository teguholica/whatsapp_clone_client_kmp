package com.teguholica.chat

import com.teguholica.chat.data.remote.ContactApi
import com.teguholica.chat.data.remote.dto.ParticipantDto
import com.teguholica.chat.domain.model.Chat
import com.teguholica.chat.domain.model.ChatType
import com.teguholica.chat.domain.model.User
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.domain.repository.AuthResult
import com.teguholica.chat.domain.repository.ChatRepository
import com.teguholica.chat.ui.newchat.NewChatUiState
import com.teguholica.chat.ui.newchat.NewChatViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewChatViewModelTest {

    private val fakeAuth = FakeAuth3()
    private val fakeApi = FakeContactApi3()
    private val fakeRepo = FakeChatRepository3()

    @Test
    fun loadContacts_populates_ui_state_with_contacts() = runBlocking {
        val vm = NewChatViewModel(fakeApi, fakeRepo, fakeAuth, scope = this)
        vm.loadContacts()
        yield()

        val state = vm.uiState.value
        assertTrue(state is NewChatUiState.Success)
        assertEquals(2, state.contacts.size)
        assertEquals("Budi", state.contacts[0].displayName)
        assertEquals("Siti", state.contacts[1].displayName)
    }

    @Test
    fun searchQuery_filters_contacts() = runBlocking {
        val vm = NewChatViewModel(fakeApi, fakeRepo, fakeAuth, scope = this)
        vm.loadContacts()
        yield()
        vm.updateSearchQuery("Budi")

        val state = vm.uiState.value as NewChatUiState.Success
        assertEquals(1, state.filtered.size)
        assertEquals("Budi", state.filtered.first().displayName)
    }

    @Test
    fun createOrNavigate_existing_contact_returns_existing_chat() = runBlocking {
        fakeRepo.existingChat = Chat(
            id = "existing_chat_1",
            type = ChatType.PERSONAL,
            name = "Budi",
            participants = listOf(User(id = "contact_budi", phone = "+62811111111", displayName = "Budi")),
            createdAt = "2025-01-01T00:00:00Z",
        )
        val vm = NewChatViewModel(fakeApi, fakeRepo, fakeAuth, scope = this)
        vm.createOrNavigate("contact_budi")

        val state = vm.uiState.value
        assertTrue(state is NewChatUiState.Created)
        assertEquals("existing_chat_1", state.chatId)
    }

    @Test
    fun createOrNavigate_new_contact_creates_conversation() = runBlocking {
        val vm = NewChatViewModel(fakeApi, fakeRepo, fakeAuth, scope = this)
        vm.createOrNavigate("contact_siti")
        yield()

        val state = vm.uiState.value
        assertTrue(state is NewChatUiState.Created)
        assertEquals("conv_siti", state.chatId)
    }
}

private class FakeAuth3 : AuthRepository {
    override suspend fun register(phone: String) = Result.success(Unit)
    override suspend fun verify(phone: String, otp: String) =
        Result.success(AuthResult("fake_access", "fake_refresh", User("fake_id", "+628123456789")))
    override fun getSavedAccessToken() = "fake_token"
    override fun getSavedRefreshToken() = "fake_refresh"
    override fun getSavedUserId() = "fake_user"
    override fun getSavedPhone() = "+628123456789"
    override fun isLoggedIn() = true
    override fun logout() {}
}

private class FakeContactApi3 : ContactApi() {
    override suspend fun getAll(token: String) = Result.success(listOf(
        ParticipantDto(id = "contact_budi", phone = "+62811111111", displayName = "Budi"),
        ParticipantDto(id = "contact_siti", phone = "+62822222222", displayName = "Siti"),
    ))
}

private class FakeChatRepository3 : ChatRepository {
    var existingChat: Chat? = null
    private val chats = mutableListOf<Chat>()

    override suspend fun getAll() = Result.success(chats.toList())
    override suspend fun createPersonalConversation(participantId: String): Result<Chat> {
        val chat = Chat(
            id = "conv_siti",
            type = ChatType.PERSONAL,
            name = "Siti",
            participants = listOf(User(id = participantId, phone = "+62822222222", displayName = "Siti")),
            createdAt = "2025-01-01T00:00:00Z",
        )
        chats.add(chat)
        return Result.success(chat)
    }
    override fun getCached(): List<Chat> {
        val existing = existingChat
        return if (existing != null) listOf(existing) + chats else chats.toList()
    }
    override fun updatePresenceCache(userId: String, online: Boolean, lastSeenAt: String?) {}
    override fun updateTypingCache(conversationId: String, isTyping: Boolean) {}
    override fun updateLastMessage(conversationId: String, senderId: String, content: String, timestamp: String) {}
    override fun incrementUnread(conversationId: String) {}
    override fun clearUnread(conversationId: String) {}
}
