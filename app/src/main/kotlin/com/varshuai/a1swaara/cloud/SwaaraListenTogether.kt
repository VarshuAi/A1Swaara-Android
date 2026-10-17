package com.varshuai.a1swaara.cloud

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

data class RoomState(
    val roomCode: String,
    val hostId: String,
    val hostName: String,
    val roomName: String,
    val songId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val updatedAt: Long,
    val isHost: Boolean = false
)

/**
 * Manages real-time "Listen Together" Live Jam rooms for synchronized multi-device playback.
 */
object SwaaraListenTogether {
    private const val TAG = "SwaaraListenTogether"
    private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()

    private val _currentRoom = MutableStateFlow<RoomState?>(null)
    val currentRoom: StateFlow<RoomState?> = _currentRoom.asStateFlow()

    private var syncJob: Job? = null

    fun generateRoomCode(): String {
        val sb = StringBuilder("JAM")
        for (i in 0 until 3) {
            sb.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)])
        }
        return sb.toString()
    }

    suspend fun createRoom(
        context: Context,
        roomName: String = "My Jam Room"
    ): String? = withContext(Dispatchers.IO) {
        val auth = SwaaraCloudAuth.getInstance(context)
        val roomCode = generateRoomCode()
        val timestamp = System.currentTimeMillis()

        val docData = mapOf(
            "roomCode" to roomCode,
            "hostId" to auth.userId,
            "hostName" to auth.displayName,
            "roomName" to roomName,
            "songId" to "",
            "title" to "",
            "artist" to "",
            "artworkUrl" to "",
            "isPlaying" to false,
            "positionMs" to 0L,
            "updatedAt" to timestamp
        )

        val success = SwaaraCloudConfig.setDocument("jam_rooms/$roomCode", docData)
        if (success) {
            val state = RoomState(
                roomCode = roomCode,
                hostId = auth.userId,
                hostName = auth.displayName,
                roomName = roomName,
                songId = "",
                title = "",
                artist = "",
                artworkUrl = null,
                isPlaying = false,
                positionMs = 0L,
                updatedAt = timestamp,
                isHost = true
            )
            _currentRoom.value = state
            roomCode
        } else {
            null
        }
    }

    suspend fun joinRoom(
        context: Context,
        roomCode: String
    ): RoomState? = withContext(Dispatchers.IO) {
        val cleanCode = roomCode.trim().uppercase()
        val auth = SwaaraCloudAuth.getInstance(context)

        val state = fetchRoom(cleanCode) ?: return@withContext null
        val fullState = state.copy(isHost = state.hostId == auth.userId)
        _currentRoom.value = fullState
        fullState
    }

    suspend fun fetchRoom(roomCode: String): RoomState? = withContext(Dispatchers.IO) {
        val cleanCode = roomCode.trim().uppercase()
        try {
            val doc = SwaaraCloudConfig.getDocument("jam_rooms/$cleanCode") ?: return@withContext null
            val hostId = doc["hostId"] as? String ?: ""
            val hostName = doc["hostName"] as? String ?: "Host"
            val roomName = doc["roomName"] as? String ?: "Jam Room"
            val songId = doc["songId"] as? String ?: ""
            val title = doc["title"] as? String ?: ""
            val artist = doc["artist"] as? String ?: ""
            val artworkUrl = doc["artworkUrl"] as? String
            val isPlaying = doc["isPlaying"] as? Boolean ?: false
            val positionMs = (doc["positionMs"] as? Number)?.toLong() ?: 0L
            val updatedAt = (doc["updatedAt"] as? Number)?.toLong() ?: 0L

            RoomState(
                roomCode = cleanCode,
                hostId = hostId,
                hostName = hostName,
                roomName = roomName,
                songId = songId,
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                isPlaying = isPlaying,
                positionMs = positionMs,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching room $cleanCode", e)
            null
        }
    }

    suspend fun updateHostPlayback(
        roomCode: String,
        songId: String,
        title: String,
        artist: String,
        artworkUrl: String?,
        isPlaying: Boolean,
        positionMs: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val active = _currentRoom.value ?: return@withContext false
        if (!active.isHost && active.roomCode != roomCode) return@withContext false

        val timestamp = System.currentTimeMillis()
        val docData = mapOf(
            "songId" to songId,
            "title" to title,
            "artist" to artist,
            "artworkUrl" to (artworkUrl ?: ""),
            "isPlaying" to isPlaying,
            "positionMs" to positionMs,
            "updatedAt" to timestamp
        )

        val success = SwaaraCloudConfig.setDocument("jam_rooms/$roomCode", docData)
        if (success) {
            _currentRoom.value = active.copy(
                songId = songId,
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                isPlaying = isPlaying,
                positionMs = positionMs,
                updatedAt = timestamp
            )
        }
        success
    }

    fun startListenerSync(
        scope: CoroutineScope,
        roomCode: String,
        onPlaybackSyncRequired: (songId: String, title: String, artist: String, artworkUrl: String?, isPlaying: Boolean, targetPositionMs: Long) -> Unit
    ) {
        syncJob?.cancel()
        syncJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2500)
                val state = fetchRoom(roomCode)
                if (state != null) {
                    _currentRoom.value = state.copy(isHost = false)
                    if (state.songId.isNotBlank()) {
                        // Calculate elapsed time since host reported position
                        val now = System.currentTimeMillis()
                        val elapsedSinceUpdate = if (state.isPlaying) (now - state.updatedAt).coerceAtLeast(0) else 0L
                        val estimatedPosition = state.positionMs + elapsedSinceUpdate

                        withContext(Dispatchers.Main) {
                            onPlaybackSyncRequired(
                                state.songId,
                                state.title,
                                state.artist,
                                state.artworkUrl,
                                state.isPlaying,
                                estimatedPosition
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun leaveRoom() = withContext(Dispatchers.IO) {
        syncJob?.cancel()
        syncJob = null
        val room = _currentRoom.value
        if (room != null && room.isHost) {
            SwaaraCloudConfig.deleteDocument("jam_rooms/${room.roomCode}")
        }
        _currentRoom.value = null
    }
}
