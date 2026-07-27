package com.teguholica.chat

import com.teguholica.chat.data.remote.ConversationApi
import com.teguholica.chat.data.remote.dto.ConversationDto
import com.teguholica.chat.data.remote.dto.ParticipantDto
import com.teguholica.chat.data.repository.ChatRepositoryImpl
import com.teguholica.chat.domain.model.ChatType
import com.teguholica.chat.domain.model.User
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatRepositoryImplTest {

    private val fakeApi = FakeConversationApi()

    @Test
    fun createPersonalConversation_returns_chat_and_updates_cache() = runBlocking {
        val repo = ChatRepositoryImpl(fakeApi)

        val result = repo.createPersonalConversation("+62811111111")

        assertTrue(result.isSuccess)
        val chat = result.getOrThrow()
        assertEquals("conv_new_1", chat.id)
        assertEquals(ChatType.PERSONAL, chat.type)
        assertEquals("Budi", chat.name)
        assertEquals(1, chat.participants.size)
        assertEquals("+62811111111", chat.participants.first().phone)

        val cached = repo.getCached()
        assertEquals(1, cached.size)
        assertEquals("conv_new_1", cached.first().id)
    }

    @Test
    fun createPersonalConversation_propagates_api_error() = runBlocking {
        fakeApi.shouldFail = true
        val repo = ChatRepositoryImpl(fakeApi)

        val result = repo.createPersonalConversation("+62899999999")

        assertTrue(result.isFailure)
        assertEquals("Gagal buat percakapan", result.exceptionOrNull()?.message)
    }
}

private class FakeConversationApi : ConversationApi() {
    var shouldFail = false

    override suspend fun create(participantId: String) = if (shouldFail) {
        Result.failure(Exception("Gagal buat percakapan"))
    } else {
        Result.success(ConversationDto(
            id = "conv_new_1",
            type = "PERSONAL",
            name = "Budi",
            participants = listOf(ParticipantDto(
                id = "user_1",
                phone = participantId,
                displayName = "Budi",
            )),
            createdAt = "2025-01-01T00:00:00Z",
        ))
    }
}
