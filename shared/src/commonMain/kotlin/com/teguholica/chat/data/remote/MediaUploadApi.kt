package com.teguholica.chat.data.remote

import com.teguholica.chat.data.remote.dto.MediaUploadResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

data class MediaFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)

class MediaUploadApi {
    private val client get() = NetworkClient.httpClient
    private val baseUrl get() = ApiConfig.baseUrl

    suspend fun upload(file: MediaFile): Result<MediaUploadResponse> {
        return try {
            val response = client.post("$baseUrl/api/media/upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("file", file.bytes, Headers.build {
                        append(HttpHeaders.ContentType, file.mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                    })
                }))
            }
            if (!response.status.isSuccess()) {
                val code = response.status.value
                val msg = when (code) {
                    413 -> "File terlalu besar"
                    else -> "Gagal upload ($code)"
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
}
