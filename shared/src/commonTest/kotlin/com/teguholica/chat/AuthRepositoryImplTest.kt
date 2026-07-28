package com.teguholica.chat

import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.remote.AuthApiException
import com.teguholica.chat.data.remote.dto.RefreshResponse
import com.teguholica.chat.data.repository.AuthRepositoryImpl
import com.teguholica.chat.domain.model.User
import com.teguholica.chat.domain.repository.AuthResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private val fakeApi = FakeAuthApi()
    private val fakeStorage = FakeTokenStorage()

    @Test
    fun refreshTokens_success_saves_new_tokens_and_returns_AuthResult() = runBlocking {
        fakeStorage.refreshToken = "old-refresh-token"
        val repo = AuthRepositoryImpl(fakeApi, fakeStorage)

        val result = repo.refreshTokens()

        assertTrue(result.isSuccess)
        val authResult = result.getOrThrow()
        assertEquals("new-access-token", authResult.accessToken)
        assertEquals("new-refresh-token", authResult.refreshToken)
        assertEquals("user-1", authResult.user.id)
        assertEquals("new-access-token", fakeStorage.accessToken)
        assertEquals("new-refresh-token", fakeStorage.refreshToken)
    }

    @Test
    fun refreshTokens_api_failure_returns_failure_and_tokens_unchanged() = runBlocking {
        fakeStorage.accessToken = "old-access-token"
        fakeStorage.refreshToken = "old-refresh-token"
        fakeApi.shouldFail = true
        val repo = AuthRepositoryImpl(fakeApi, fakeStorage)

        val result = repo.refreshTokens()

        assertTrue(result.isFailure)
        assertEquals("Refresh gagal", result.exceptionOrNull()?.message)
        assertEquals("old-access-token", fakeStorage.accessToken)
        assertEquals("old-refresh-token", fakeStorage.refreshToken)
    }

    @Test
    fun refreshTokens_no_stored_refresh_token_returns_failure() = runBlocking {
        fakeStorage.refreshToken = null
        val repo = AuthRepositoryImpl(fakeApi, fakeStorage)

        val result = repo.refreshTokens()

        assertTrue(result.isFailure)
    }

}

private class FakeAuthApi : AuthApi() {
    var shouldFail = false

    override suspend fun refresh(refreshToken: String): Result<RefreshResponse> {
        return if (shouldFail) {
            Result.failure(AuthApiException(401, "Refresh gagal"))
        } else {
            Result.success(RefreshResponse(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                user = User(
                    id = "user-1",
                    phone = "+62811111111",
                    displayName = "Test User",
                ),
            ))
        }
    }
}

private class FakeTokenStorage : TokenStorage {
    var accessToken: String? = null
    var refreshToken: String? = null
    var userId: String? = null
    var phone: String? = null

    override fun saveAccessToken(token: String) { accessToken = token }
    override fun getAccessToken(): String? = accessToken
    override fun saveRefreshToken(token: String) { refreshToken = token }
    override fun getRefreshToken(): String? = refreshToken
    override fun saveUserId(id: String) { userId = id }
    override fun getUserId(): String? = userId
    override fun savePhone(phone: String) { this.phone = phone }
    override fun getPhone(): String? = phone
    override fun clear() {
        accessToken = null
        refreshToken = null
        userId = null
        phone = null
    }
}
