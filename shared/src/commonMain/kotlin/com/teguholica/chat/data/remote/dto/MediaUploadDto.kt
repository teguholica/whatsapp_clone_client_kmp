package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaUploadResponse(
    val id: String,
    val url: String,
    val mimeType: String,
    val fileSize: Long,
)
