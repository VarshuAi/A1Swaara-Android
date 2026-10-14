package com.varshuai.a1swaara.cloud

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.UUID

/**
 * Manages anonymous and cross-device identity for A1 Swaara Cloud.
 * Enables zero-friction immediate usage with optional multi-device account linking.
 */
class SwaaraCloudAuth private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var userId: String
        get() {
            var id = prefs.getString(KEY_USER_ID, null)
            if (id.isNullOrBlank()) {
                id = generateNewUserId()
                prefs.edit().putString(KEY_USER_ID, id).apply()
            }
            return id
        }
        set(value) {
            prefs.edit().putString(KEY_USER_ID, value.trim()).apply()
        }

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, defaultDeviceName()) ?: defaultDeviceName()
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value.trim()).apply()

    var isAutoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TS, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TS, value).apply()

    fun linkAccount(targetUserId: String) {
        if (targetUserId.isNotBlank()) {
            userId = targetUserId.trim()
        }
    }

    fun resetToNewAccount(): String {
        val newId = generateNewUserId()
        userId = newId
        lastSyncTimestamp = 0L
        return newId
    }

    private fun generateNewUserId(): String {
        val shortUuid = UUID.randomUUID().toString().replace("-", "").take(12)
        return "swaara_usr_$shortUuid"
    }

    private fun defaultDeviceName(): String {
        val model = Build.MODEL ?: "Android"
        return "Swaara ($model)"
    }

    companion object {
        private const val PREFS_NAME = "a1swaara_cloud_auth_prefs"
        private const val KEY_USER_ID = "cloud_user_id"
        private const val KEY_DISPLAY_NAME = "cloud_display_name"
        private const val KEY_AUTO_SYNC = "cloud_auto_sync"
        private const val KEY_LAST_SYNC_TS = "cloud_last_sync_ts"

        @Volatile
        private var INSTANCE: SwaaraCloudAuth? = null

        fun getInstance(context: Context): SwaaraCloudAuth {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SwaaraCloudAuth(context).also { INSTANCE = it }
            }
        }
    }
}
