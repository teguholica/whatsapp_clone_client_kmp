package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.ParticipantDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

open class ContactApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    open suspend fun getAll(token: String): Result<List<ParticipantDto>> {
        return try {
            val response = client.get("$baseUrl/api/contacts") {
                bearerAuth(token)
            }
            if (!response.status.isSuccess()) {
                val code = response.status.value
                Result.failure(AuthApiException(code, "Gagal ambil daftar kontak ($code)"))
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
