package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

open class ConversationApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    open suspend fun create(phone: String): Result<ConversationDto> {
        return try {
            val response = client.post("$baseUrl/api/conversations") {
                contentType(ContentType.Application.Json)
                setBody(CreateConversationRequest(phone))
            }
            if (!response.status.isSuccess()) {
                val code = response.status.value
                val msg = when (code) {
                    400 -> "Tidak bisa chat dengan diri sendiri"
                    404 -> "Nomor telepon tidak terdaftar"
                    401 -> "Sesi berakhir. Silakan login ulang"
                    else -> "Gagal buat percakapan ($code)"
                }
                Result.failure(AuthApiException(code, msg))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun getAll(): Result<List<ConversationDto>> {
        return try {
            val response = client.get("$baseUrl/api/conversations")
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

    suspend fun getById(id: String): Result<ConversationDto> {
        return try {
            val response = client.get("$baseUrl/api/conversations/$id")
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

    suspend fun leave(id: String): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/api/conversations/$id")
            if (!response.status.isSuccess()) {
                val code = response.status.value
                Result.failure(AuthApiException(code, "Gagal tinggalkan chat ($code)"))
            } else {
                Result.success(Unit)
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }
}
