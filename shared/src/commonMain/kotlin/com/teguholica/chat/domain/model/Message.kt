package com.teguholica.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType { TEXT, IMAGE, VIDEO, DOCUMENT }

@Serializable
enum class MessageStatus { SENT, DELIVERED, READ }

@Serializable
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val type: MessageType,
    val content: String,
    val media: Media? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val createdAt: String,
)
