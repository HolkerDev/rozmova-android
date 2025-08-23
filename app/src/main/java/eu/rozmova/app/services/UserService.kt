package eu.rozmova.app.services

import android.util.Log
import eu.rozmova.app.BuildConfig
import eu.rozmova.app.domain.Level
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UserInitState {
    data object Idle : UserInitState

    data object SavingData : UserInitState

    data object CreatingBucket : UserInitState

    data object Finished : UserInitState

    data object Error : UserInitState
}

@Serializable
data class WsCommand<T>(
    val type: String,
    val data: T,
)

@Serializable
data class UserInitData(
    val job: String?,
    val hobbies: List<String>,
    val pronoun: String,
    val level: Level,
)

@Serializable
data class WsResponse(
    val type: String,
    val data: JsonElement,
)

@Singleton
class UserService
    @Inject
    constructor(
        private val client: HttpClient,
        private val json: Json,
    ) {
        private val _userInitProgress = MutableStateFlow<UserInitState>(UserInitState.Idle)
        val userInitProgress = _userInitProgress.asStateFlow()

        suspend fun startUserInit(userInitData: UserInitData) {
            Log.d("UserService", "Connecting to WebSocket: ${BuildConfig.API_MEGA_BASE_WS_URL}")

            try {
                client.webSocket(urlString = BuildConfig.API_MEGA_BASE_WS_URL) {
                    sendSerialized(WsCommand(type = "initUser", data = userInitData))
                    _userInitProgress.value = UserInitState.SavingData

                    incoming
                        .consumeAsFlow()
                        .catch { e ->
                        }.collect { frame ->
                            when (frame) {
                                is Frame.Text -> {
                                    Log.i("UserService", "Received: ${frame.readText()}")
                                    val response = json.decodeFromString<WsResponse>(frame.readText())
                                    when (response.type) {
                                        "error" -> {
                                            Log.e("UserService", "Error response: ${response.data}")
                                            _userInitProgress.value = UserInitState.Error
                                        }
                                        "initUser" -> {
                                            Log.i("UserService", "User init response: ${response.data}")
                                            val status = response.data.jsonPrimitive.content
                                            when (status) {
                                                "FINISHED" -> {
                                                    _userInitProgress.value = UserInitState.Finished
                                                }
                                                else -> {
                                                    Log.w("UserService", "Unknown userInit status: $status")
                                                }
                                            }
                                        }

                                        else -> {
                                            Log.w("UserService", "Unknown response type: ${response.type}")
                                        }
                                    }
                                }

                                is Frame.Close -> {
                                    Log.i("UserService", "Connection closed")
                                }

                                else -> { // ignore
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("UserService", "WebSocket connection failed: ${e.message}")
            }
        }
    }
