package com.teguholica.chat.data.remote.dto

import kotlinx.serialization.Serializable
import com.teguholica.chat.domain.model.User

@Serializable
data class RegisterRequest(val phone: String)

@Serializable
data class VerifyRequest(val phone: String, val otp: String)

@Serializable
data class VerifyResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

@Serializable
data class ErrorResponse(
    val message: String,
    val error: String? = null,
    val statusCode: Int? = null,
)
