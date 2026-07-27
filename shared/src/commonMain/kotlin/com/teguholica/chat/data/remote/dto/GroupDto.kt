package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val members: List<String>,
)

@Serializable
data class UpdateGroupNameRequest(
    val name: String,
)

@Serializable
data class GroupMembersRequest(
    val members: List<String>,
)

@Serializable
data class PromoteAdminRequest(
    val userId: String,
)

@Serializable
data class GroupResponseDto(
    val id: String,
    val name: String,
    val type: String = "group",
    val avatarUrl: String? = null,
    val members: List<MemberDto> = emptyList(),
    val admins: List<String> = emptyList(),
    val createdAt: String,
)
