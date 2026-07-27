package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponseDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String,
    val status: String = "SENT",
    val createdAt: String,
)

@Serializable
data class SendMessageRequest(
    val type: String,
    val content: String,
)
