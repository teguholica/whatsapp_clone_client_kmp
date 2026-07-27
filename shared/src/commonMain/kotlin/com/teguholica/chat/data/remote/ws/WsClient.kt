package com.teguholica.chat.data.remote.ws

import com.teguholica.chat.data.remote.ApiConfig
import com.teguholica.chat.data.remote.NetworkClient
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

sealed interface WsEvent {
    data class MessageNew(val data: MessageNewData) : WsEvent
    data class MessageStatus(val data: MessageStatusData) : WsEvent
    data class MessageDeleted(val data: MessageDeletedData) : WsEvent
    data class Typing(val data: TypingData) : WsEvent
    data class TypingStop(val data: TypingData) : WsEvent
    data class Presence(val data: PresenceData) : WsEvent
    data object Connected : WsEvent
    data object Disconnected : WsEvent
    data object SessionExpired : WsEvent
    data class Error(val message: String) : WsEvent
}

sealed interface WsConnectionState {
    data object Disconnected : WsConnectionState
    data object Connecting : WsConnectionState
    data class Connected(val session: DefaultClientWebSocketSession) : WsConnectionState
}

class WsClient {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val _events = MutableSharedFlow<WsEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    private val _connectionState = MutableStateFlow<WsConnectionState>(WsConnectionState.Disconnected)
    val connectionState: StateFlow<WsConnectionState> = _connectionState.asStateFlow()

    private var job: Job? = null
    private var tokenProvider: (() -> String?)? = null
    private val joinedRooms = mutableListOf<String>()

    private val client: HttpClient by lazy {
        NetworkClient.httpClient
    }

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    fun connect(scope: CoroutineScope) {
        disconnect()
        job = scope.launch {
            var retryDelay = 1000L
            val maxDelay = 30_000L

            while (isActive) {
                val token = tokenProvider?.invoke()
                if (token == null) {
                    _events.emit(WsEvent.Error("Token tidak tersedia"))
                    break
                }

                _connectionState.value = WsConnectionState.Connecting
                val wsUrl = "${ApiConfig.baseUrl.replace("http", "ws")}?token=$token"

                var shouldReauth = false

                try {
                    client.webSocket(wsUrl) {
                        _connectionState.value = WsConnectionState.Connected(this)
                        _events.emit(WsEvent.Connected)
                        retryDelay = 1000L

                        rejoinRooms()
                        sendPresenceOnline()

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                handleFrame(frame.readText())
                            }
                        }

                        try {
                            val reason = closeReason.await()
                            shouldReauth = reason?.code == 4001.toShort()
                        } catch (_: Exception) { }
                    }
                } catch (_: CancellationException) {
                    break
                } catch (e: Exception) {
                    _events.emit(WsEvent.Error("Koneksi WebSocket gagal: ${e.message}"))
                }

                _connectionState.value = WsConnectionState.Disconnected
                _events.emit(WsEvent.Disconnected)

                if (shouldReauth) {
                    _events.emit(WsEvent.SessionExpired)
                    break
                }

                delay(retryDelay)
                retryDelay = (retryDelay * 2).coerceAtMost(maxDelay)
            }

            _connectionState.value = WsConnectionState.Disconnected
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        _connectionState.value = WsConnectionState.Disconnected
    }

    suspend fun send(event: String, data: JsonObject = JsonObject(emptyMap())) {
        val state = _connectionState.value
        if (state is WsConnectionState.Connected) {
            try {
                val frame = json.encodeToString(WsFrame.serializer(), WsFrame(event, data))
                state.session.send(Frame.Text(frame))
            } catch (_: Exception) { }
        }
    }

    suspend fun joinRoom(conversationId: String) {
        if (conversationId !in joinedRooms) joinedRooms.add(conversationId)
        val data = json.encodeToJsonElement(RoomJoinData.serializer(), RoomJoinData(conversationId)).jsonObject
        send("room:join", data)
    }

    suspend fun leaveRoom(conversationId: String) {
        joinedRooms.remove(conversationId)
        val data = json.encodeToJsonElement(RoomLeaveData.serializer(), RoomLeaveData(conversationId)).jsonObject
        send("room:leave", data)
    }

    suspend fun sendMessageRead(messageId: String) {
        val data = json.encodeToJsonElement(MessageReadData.serializer(), MessageReadData(messageId)).jsonObject
        send("message:read", data)
    }

    suspend fun sendTypingStart(conversationId: String) {
        val data = json.encodeToJsonElement(TypingStartData.serializer(), TypingStartData(conversationId)).jsonObject
        send("typing:start", data)
    }

    suspend fun sendTypingStop(conversationId: String) {
        val data = json.encodeToJsonElement(TypingStopData.serializer(), TypingStopData(conversationId)).jsonObject
        send("typing:stop", data)
    }

    private suspend fun sendPresenceOnline() {
        val data = json.encodeToJsonElement(PresenceOnlineData.serializer(), PresenceOnlineData()).jsonObject
        send("presence:online", data)
    }

    private suspend fun rejoinRooms() {
        for (roomId in joinedRooms.toList()) {
            joinRoom(roomId)
        }
    }

    private suspend fun handleFrame(text: String) {
        try {
            val frame = json.decodeFromString(WsFrame.serializer(), text)
            val event = frame.event
            val data = frame.data

            when (event) {
                "message:new" -> {
                    val msg = json.decodeFromJsonElement(MessageNewData.serializer(), data)
                    _events.emit(WsEvent.MessageNew(msg))
                }
                "message:status" -> {
                    val msg = json.decodeFromJsonElement(MessageStatusData.serializer(), data)
                    _events.emit(WsEvent.MessageStatus(msg))
                }
                "message:deleted" -> {
                    val msg = json.decodeFromJsonElement(MessageDeletedData.serializer(), data)
                    _events.emit(WsEvent.MessageDeleted(msg))
                }
                "typing" -> {
                    val msg = json.decodeFromJsonElement(TypingData.serializer(), data)
                    _events.emit(WsEvent.Typing(msg))
                }
                "typing:stop" -> {
                    val msg = json.decodeFromJsonElement(TypingData.serializer(), data)
                    _events.emit(WsEvent.TypingStop(msg))
                }
                "presence" -> {
                    val msg = json.decodeFromJsonElement(PresenceData.serializer(), data)
                    _events.emit(WsEvent.Presence(msg))
                }
            }
        } catch (_: Exception) { }
    }
}
