package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val type: String,
    val name: String,
    val avatarUrl: String? = null,
    val lastMessage: LastMessageDto? = null,
    val unreadCount: Int = 0,
    val participants: List<ParticipantDto> = emptyList(),
    val createdAt: String,
)

@Serializable
data class LastMessageDto(
    val id: String,
    val content: String,
    val type: String,
    val senderId: String,
    val createdAt: String,
)

@Serializable
data class ParticipantDto(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)
