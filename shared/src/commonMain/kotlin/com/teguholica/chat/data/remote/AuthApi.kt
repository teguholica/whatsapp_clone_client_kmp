package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthApiException(val statusCode: Int, override val message: String) : Exception(message)

class AuthApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun register(phone: String): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(phone))
            }
            if (!response.status.isSuccess()) {
                val statusCode = response.status.value
                val errorBody = tryParseError(response)
                Result.failure(AuthApiException(statusCode, errorBody ?: "Gagal register ($statusCode)"))
            } else {
                Result.success(Unit)
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun verify(phone: String, otp: String): Result<VerifyResponse> {
        return try {
            val response = client.post("$baseUrl/api/auth/verify") {
                contentType(ContentType.Application.Json)
                setBody(VerifyRequest(phone, otp))
            }
            if (!response.status.isSuccess()) {
                val statusCode = response.status.value
                val errorBody = tryParseError(response)
                Result.failure(AuthApiException(statusCode, errorBody ?: "Gagal verifikasi ($statusCode)"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung ke server: ${e.message}"))
        }
    }

    private suspend fun tryParseError(response: io.ktor.client.statement.HttpResponse): String? {
        return try {
            val error = response.body<ErrorResponse>()
            error.message
        } catch (_: Exception) {
            null
        }
    }
}
