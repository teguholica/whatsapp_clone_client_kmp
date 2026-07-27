package com.teguholica.chat.domain.repository

import com.teguholica.chat.domain.model.User

data class AuthResult(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

interface AuthRepository {
    suspend fun register(phone: String): Result<Unit>
    suspend fun verify(phone: String, otp: String): Result<AuthResult>
    fun getSavedAccessToken(): String?
    fun getSavedRefreshToken(): String?
    fun getSavedUserId(): String?
    fun getSavedPhone(): String?
    fun isLoggedIn(): Boolean
    fun logout()
}
