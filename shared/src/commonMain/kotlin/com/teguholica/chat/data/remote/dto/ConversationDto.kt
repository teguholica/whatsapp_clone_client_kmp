package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val phone: String,
)

@Serializable
data class ConversationDto(
    val id: String,
    val type: String,
    val name: String = "",
    val avatarUrl: String? = null,
    val lastMessage: LastMessageDto? = null,
    val unreadCount: Int = 0,
    val otherUser: OtherUserDto? = null,
    val members: List<MemberDto> = emptyList(),
    val createdAt: String,
)

@Serializable
data class OtherUserDto(
    val id: String,
    val displayName: String? = null,
)

@Serializable
data class MemberDto(
    val userId: String,
    val displayName: String? = null,
)

@Serializable
data class LastMessageDto(
    val id: String,
    val content: String,
    val type: String,
    val senderId: String,
    val createdAt: String,
)
