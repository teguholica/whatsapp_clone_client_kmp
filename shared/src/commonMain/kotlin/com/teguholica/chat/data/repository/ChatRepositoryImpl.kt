package com.teguholica.chat.data.repository

import com.teguholica.chat.data.remote.ConversationApi
import com.teguholica.chat.domain.model.*
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val conversationApi: ConversationApi,
    private val authRepository: AuthRepository,
) : ChatRepository {

    private var cache: List<Chat> = emptyList()
    private var conversationIds: Set<String> = emptySet()

    override suspend fun createPersonalConversation(participantId: String): Result<Chat> {
        val token = authRepository.getSavedAccessToken()
            ?: return Result.failure(Exception("Belum login"))
        return conversationApi.create(token, participantId).map { dto ->
            val participants = dto.participants.map { p ->
                User(id = p.id, phone = p.phone, displayName = p.displayName, avatarUrl = p.avatarUrl)
            }
            val chat = Chat(
                id = dto.id,
                type = ChatType.PERSONAL,
                name = dto.name,
                avatarUrl = dto.avatarUrl,
                unreadCount = dto.unreadCount,
                participants = participants,
                createdAt = dto.createdAt,
            )
            conversationIds = conversationIds + chat.id
            cache = listOf(chat) + cache
            chat
        }
    }

    override suspend fun getAll(): Result<List<Chat>> {
        val token = authRepository.getSavedAccessToken()
            ?: return Result.failure(Exception("Belum login"))
        return conversationApi.getAll(token).map { dtos ->
            val chats = dtos.map { dto ->
                val type = if (dto.type == "GROUP") ChatType.GROUP else ChatType.PERSONAL
                val lastMsg = dto.lastMessage?.let {
                    Message(
                        id = it.id,
                        chatId = dto.id,
                        senderId = it.senderId,
                        content = it.content,
                        type = parseMessageType(it.type),
                        createdAt = it.createdAt,
                    )
                }
                val participants = dto.participants.map { p ->
                    User(id = p.id, phone = p.phone, displayName = p.displayName, avatarUrl = p.avatarUrl)
                }
                Chat(
                    id = dto.id,
                    type = type,
                    name = dto.name,
                    avatarUrl = dto.avatarUrl,
                    lastMessage = lastMsg,
                    unreadCount = dto.unreadCount,
                    participants = participants,
                    createdAt = dto.createdAt,
                )
            }
            conversationIds = chats.map { it.id }.toSet()
            cache = chats
            chats
        }
    }

    override fun getCached(): List<Chat> = cache

    override fun updatePresenceCache(userId: String, online: Boolean, lastSeenAt: String?) {
        cache = cache.map { chat ->
            val updated = chat.participants.map { p ->
                if (p.id == userId) p.copy(
                    presence = Presence(userId, if (online) PresenceStatus.ONLINE else PresenceStatus.OFFLINE, lastSeenAt)
                ) else p
            }
            chat.copy(participants = updated)
        }
    }

    override fun updateTypingCache(conversationId: String, isTyping: Boolean) {
        // typing state stored locally; not persisted in Chat model
        // handled via UiState separately
    }

    override fun updateLastMessage(conversationId: String, senderId: String, content: String, timestamp: String) {
        cache = cache.map { chat ->
            if (chat.id == conversationId) {
                chat.copy(lastMessage = Message(
                    id = "",
                    chatId = conversationId,
                    senderId = senderId,
                    content = content,
                    type = MessageType.TEXT,
                    createdAt = timestamp,
                ))
            } else chat
        }
    }

    override fun incrementUnread(conversationId: String) {
        cache = cache.map { chat ->
            if (chat.id == conversationId) chat.copy(unreadCount = chat.unreadCount + 1) else chat
        }
    }

    override fun clearUnread(conversationId: String) {
        cache = cache.map { chat ->
            if (chat.id == conversationId) chat.copy(unreadCount = 0) else chat
        }
    }

    private fun parseMessageType(type: String): MessageType = when (type.uppercase()) {
        "IMAGE" -> MessageType.IMAGE
        "VIDEO" -> MessageType.VIDEO
        "DOCUMENT" -> MessageType.DOCUMENT
        else -> MessageType.TEXT
    }
}
