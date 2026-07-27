package com.teguholica.chat.domain.model

data class Contact(
    val id: String,
    val phone: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String = "",
)
