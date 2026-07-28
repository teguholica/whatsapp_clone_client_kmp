package com.teguholica.chat.data.repository

import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.remote.AuthApiException
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.domain.repository.AuthResult

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun register(phone: String): Result<Unit> {
        return authApi.register(phone)
    }

    override suspend fun verify(phone: String, otp: String): Result<AuthResult> {
        return authApi.verify(phone, otp).map { response ->
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            tokenStorage.saveUserId(response.user.id)
            tokenStorage.savePhone(phone)
            AuthResult(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                user = response.user,
            )
        }
    }

    override suspend fun refreshTokens(): Result<AuthResult> {
        val storedRefreshToken = tokenStorage.getRefreshToken()
        if (storedRefreshToken == null) {
            return Result.failure(AuthApiException(401, "Tidak ada refresh token"))
        }
        return authApi.refresh(storedRefreshToken).map { response ->
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            AuthResult(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                user = response.user,
            )
        }
    }

    override fun getSavedAccessToken(): String? = tokenStorage.getAccessToken()
    override fun getSavedRefreshToken(): String? = tokenStorage.getRefreshToken()
    override fun getSavedUserId(): String? = tokenStorage.getUserId()
    override fun getSavedPhone(): String? = tokenStorage.getPhone()
    override fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null
    override fun logout() = tokenStorage.clear()
}
