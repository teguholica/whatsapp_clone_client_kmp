package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val lastSeenAt: String? = null,
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
)
