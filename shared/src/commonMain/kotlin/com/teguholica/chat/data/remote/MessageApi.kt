package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.MessageResponseDto
import com.teguholica.chat.data.remote.dto.SendMessageRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class MessageApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun getMessages(
        conversationId: String,
        limit: Int = 50,
        before: String? = null,
    ): Result<List<MessageResponseDto>> {
        return try {
            val response = client.get("$baseUrl/api/messages/$conversationId") {
                parameter("limit", limit)
                before?.let { parameter("before", it) }
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal ambil pesan"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        type: String,
        content: String,
    ): Result<MessageResponseDto> {
        return try {
            val response = client.post("$baseUrl/api/messages/$conversationId") {
                contentType(ContentType.Application.Json)
                setBody(SendMessageRequest(type = type, content = content))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal kirim pesan"))
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
