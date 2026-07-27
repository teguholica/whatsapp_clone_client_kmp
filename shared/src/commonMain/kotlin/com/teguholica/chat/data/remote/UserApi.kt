package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.UpdateProfileRequest
import com.teguholica.chat.data.remote.dto.UserProfileDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun getMe(): Result<UserProfileDto> {
        return try {
            val response = client.get("$baseUrl/api/users/me")
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal ambil profil"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun updateMe(displayName: String? = null, avatarUrl: String? = null): Result<UserProfileDto> {
        return try {
            val response = client.put("$baseUrl/api/users/me") {
                contentType(ContentType.Application.Json)
                setBody(UpdateProfileRequest(displayName, avatarUrl))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal update profil"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun searchByPhone(query: String): Result<List<UserProfileDto>> {
        return try {
            if (query.isBlank()) return Result.success(emptyList())
            val response = client.get("$baseUrl/api/users/search") {
                parameter("phone", query)
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal cari user"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }
}
