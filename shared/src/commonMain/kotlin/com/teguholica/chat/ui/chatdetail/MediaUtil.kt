package com.teguholica.chat.ui.chatdetail

import com.teguholica.chat.data.remote.ApiConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val json = Json { ignoreUnknownKeys = true }

fun parseMediaUrl(content: String): String? {
    return try {
        val obj = json.decodeFromString<kotlinx.serialization.json.JsonElement>(content).jsonObject
        val path = obj["url"]?.jsonPrimitive?.content ?: return null
        ApiConfig.baseUrl + path
    } catch (_: Exception) {
        null
    }
}
