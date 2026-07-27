package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.CreateGroupRequest
import com.teguholica.chat.data.remote.dto.GroupMembersRequest
import com.teguholica.chat.data.remote.dto.GroupResponseDto
import com.teguholica.chat.data.remote.dto.PromoteAdminRequest
import com.teguholica.chat.data.remote.dto.UpdateGroupNameRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GroupApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun create(name: String, members: List<String>): Result<GroupResponseDto> {
        return try {
            val response = client.post("$baseUrl/api/groups") {
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name, members))
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

    suspend fun updateName(id: String, name: String): Result<GroupResponseDto> {
        return try {
            val response = client.put("$baseUrl/api/groups/$id") {
                contentType(ContentType.Application.Json)
                setBody(UpdateGroupNameRequest(name))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal update grup"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun addMembers(id: String, members: List<String>): Result<GroupResponseDto> {
        return try {
            val response = client.post("$baseUrl/api/groups/$id/members") {
                contentType(ContentType.Application.Json)
                setBody(GroupMembersRequest(members))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal tambah anggota"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun removeMember(id: String, userId: String): Result<GroupResponseDto> {
        return try {
            val response = client.delete("$baseUrl/api/groups/$id/members/$userId")
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal hapus anggota"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun promoteToAdmin(id: String, userId: String): Result<GroupResponseDto> {
        return try {
            val response = client.post("$baseUrl/api/groups/$id/admins") {
                contentType(ContentType.Application.Json)
                setBody(PromoteAdminRequest(userId))
            }
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal jadikan admin"))
            } else {
                Result.success(response.body())
            }
        } catch (e: AuthApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthApiException(0, "Gagal terhubung: ${e.message}"))
        }
    }

    suspend fun demoteAdmin(id: String, userId: String): Result<GroupResponseDto> {
        return try {
            val response = client.delete("$baseUrl/api/groups/$id/admins/$userId")
            if (!response.status.isSuccess()) {
                Result.failure(AuthApiException(response.status.value, "Gagal turunkan admin"))
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
