package com.teguholica.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val presence: Presence? = null,
)

@Serializable
enum class PresenceStatus { ONLINE, OFFLINE }

@Serializable
data class Presence(
    val userId: String,
    val status: PresenceStatus,
    val lastSeenAt: String? = null,
)
