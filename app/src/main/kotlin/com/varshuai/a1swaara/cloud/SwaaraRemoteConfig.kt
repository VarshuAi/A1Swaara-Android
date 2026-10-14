package com.varshuai.a1swaara.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SwaaraAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val actionUrl: String? = null,
    val isDismissible: Boolean = true
)

data class RemoteConfigState(
    val pipedInstances: List<String> = listOf("https://pipedapi.kavin.rocks"),
    val streamProxyMode: String = "direct",
    val announcement: SwaaraAnnouncement? = null,
    val minRequiredVersion: String = "1.0.0",
    val recommendedBitrate: String = "320kbps"
)

/**
 * Manages cloud-driven remote configuration, dynamic streaming proxies, and broadcast announcements.
 */
object SwaaraRemoteConfig {
    private const val TAG = "SwaaraRemoteConfig"
    private const val PREFS_NAME = "a1swaara_remote_config_prefs"
    private const val KEY_DISMISSED_ANNOUNCEMENT_ID = "dismissed_announcement_id"

    private val _configState = MutableStateFlow(RemoteConfigState())
    val configState: StateFlow<RemoteConfigState> = _configState.asStateFlow()

    suspend fun fetchAndApply(context: Context): RemoteConfigState = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val doc = SwaaraCloudConfig.getDocument("remote_config/app")
            if (doc != null) {
                @Suppress("UNCHECKED_CAST")
                val instances = (doc["activePipedInstances"] as? List<String>)?.filter { it.isNotBlank() }
                    ?: listOf("https://pipedapi.kavin.rocks")
                val streamMode = doc["streamProxyMode"] as? String ?: "direct"
                val minVer = doc["minRequiredVersion"] as? String ?: "1.0.0"
                val bitrate = doc["recommendedBitrate"] as? String ?: "320kbps"

                val annEnabled = doc["announcementEnabled"] as? Boolean ?: false
                val annId = doc["announcementId"] as? String ?: ""
                val annTitle = doc["announcementTitle"] as? String ?: ""
                val annMsg = doc["announcementMessage"] as? String ?: ""
                val annUrl = doc["announcementActionUrl"] as? String

                val dismissedId = prefs.getString(KEY_DISMISSED_ANNOUNCEMENT_ID, "")
                val announcement = if (annEnabled && annId.isNotBlank() && annId != dismissedId) {
                    SwaaraAnnouncement(
                        id = annId,
                        title = annTitle,
                        message = annMsg,
                        actionUrl = annUrl
                    )
                } else null

                val newState = RemoteConfigState(
                    pipedInstances = instances,
                    streamProxyMode = streamMode,
                    announcement = announcement,
                    minRequiredVersion = minVer,
                    recommendedBitrate = bitrate
                )
                _configState.value = newState
                return@withContext newState
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching remote config", e)
        }
        _configState.value
    }

    fun dismissAnnouncement(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DISMISSED_ANNOUNCEMENT_ID, id).apply()
        _configState.value = _configState.value.copy(announcement = null)
    }
}
