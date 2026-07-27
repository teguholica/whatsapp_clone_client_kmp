package com.teguholica.chat.data.remote.ws

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class WsFrame(
    val event: String,
    val data: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class RoomJoinData(val conversationId: String)

@Serializable
data class RoomLeaveData(val conversationId: String)

@Serializable
data class MessageReadData(val messageId: String)

@Serializable
data class TypingStartData(val conversationId: String)

@Serializable
data class TypingStopData(val conversationId: String)

@Serializable
data class PresenceOnlineData(val status: String = "online")

@Serializable
data class MessageNewData(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String,
    val createdAt: String,
)

@Serializable
data class MessageStatusData(
    val messageId: String,
    val userId: String,
    val status: String,
)

@Serializable
data class MessageDeletedData(
    val messageId: String,
    val mode: String,
)

@Serializable
data class TypingData(
    val conversationId: String,
    val userId: String? = null,
)

@Serializable
data class PresenceData(
    val userId: String,
    val status: String,
    val lastSeenAt: String? = null,
)
