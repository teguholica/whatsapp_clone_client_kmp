package com.teguholica.chat.domain.repository

import com.teguholica.chat.domain.model.Chat

interface ChatRepository {
    suspend fun getAll(): Result<List<Chat>>
    suspend fun createPersonalConversation(phone: String): Result<Chat>
    fun getCached(): List<Chat>
    fun updatePresenceCache(userId: String, online: Boolean, lastSeenAt: String?)
    fun updateTypingCache(conversationId: String, isTyping: Boolean)
    fun updateLastMessage(conversationId: String, senderId: String, content: String, timestamp: String)
    fun incrementUnread(conversationId: String)
    fun clearUnread(conversationId: String)
}
