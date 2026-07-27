package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.ConversationDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ConversationApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun getAll(token: String): Result<List<ConversationDto>> {
        return try {
            val response = client.get("$baseUrl/api/conversations") {
                bearerAuth(token)
            }
            if (!response.status.isSuccess()) {
                val code = response.status.value
                Result.failure(AuthApiException(code, "Gagal ambil daftar chat ($code)"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun getById(token: String, id: String): Result<ConversationDto> {
        return try {
            val response = client.get("$baseUrl/api/conversations/$id") {
                bearerAuth(token)
            }
            if (!response.status.isSuccess()) {
                val code = response.status.value
                Result.failure(AuthApiException(code, "Gagal ambil chat ($code)"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung ke server: ${e.message}"))
        }
    }
}
