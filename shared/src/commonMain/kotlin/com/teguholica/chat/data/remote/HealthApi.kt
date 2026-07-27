package com.teguholica.chat.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val db: String,
    val redis: String,
)

class HealthApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun check(): Result<HealthResponse> {
        return try {
            val response = client.get("$baseUrl/api/health")
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Health check gagal"))
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
