package com.teguholica.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val id: String,
    val url: String,
    val mimeType: String,
    val fileSize: Long,
)
