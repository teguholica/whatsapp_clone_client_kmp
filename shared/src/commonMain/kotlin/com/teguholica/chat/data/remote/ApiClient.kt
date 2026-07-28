package com.teguholica.chat.data.remote

import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.platformBaseUrl
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

object ApiConfig {
    var baseUrl: String = platformBaseUrl
}

object NetworkClient {
    lateinit var tokenStorage: TokenStorage

    var performRefresh: (suspend () -> BearerTokens?)? = null

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
            install(WebSockets)
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = tokenStorage.getAccessToken() ?: return@loadTokens null
                        val refresh = tokenStorage.getRefreshToken() ?: ""
                        BearerTokens(access, refresh)
                    }
                    refreshTokens {
                        val result = performRefresh?.invoke()
                        if (result == null) {
                            _sessionExpired.tryEmit(Unit)
                        }
                        result
                    }
                }
            }
        }
    }
}
