package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.CreateGroupRequest
import com.teguholica.chat.data.remote.dto.GroupResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GroupApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun create(name: String, participantIds: List<String>): Result<GroupResponseDto> {
        return try {
            val response = client.post("$baseUrl/api/groups") {
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name, participantIds))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal buat grup"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun getById(id: String): Result<GroupResponseDto> {
        return try {
            val response = client.get("$baseUrl/api/groups/$id")
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal ambil grup"))
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
