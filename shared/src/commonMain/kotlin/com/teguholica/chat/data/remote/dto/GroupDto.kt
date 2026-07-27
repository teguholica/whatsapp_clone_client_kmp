package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val participantIds: List<String>,
)

@Serializable
data class GroupResponseDto(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val participants: List<ParticipantDto> = emptyList(),
    val createdAt: String,
)
