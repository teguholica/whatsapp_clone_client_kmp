package com.teguholica.chat.domain.model

import kotlinx.serialization.Serializable

enum class ChatType { PERSONAL, GROUP }

@Serializable
data class Chat(
    val id: String,
    val type: ChatType,
    val name: String,
    val avatarUrl: String? = null,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val participants: List<User> = emptyList(),
    val createdAt: String,
)
