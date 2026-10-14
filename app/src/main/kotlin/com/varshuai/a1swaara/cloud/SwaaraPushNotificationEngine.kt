package com.varshuai.a1swaara.cloud

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.varshuai.a1swaara.MainActivity
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.service.ServiceNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SwaaraBroadcastNotification(
    val id: String,
    val title: String,
    val message: String,
    val actionUrl: String? = null,
    val enabled: Boolean = true,
    val timestamp: Long = 0L
)

/**
 * Cloud Push Notification Engine for A1 Swaara.
 * Listens for remote broadcast notifications and displays high-priority Android system status bar alerts.
 */
object SwaaraPushNotificationEngine {
    private const val TAG = "SwaaraPushEngine"
    private const val PREFS_NAME = "a1swaara_push_notifications"
    private const val KEY_LAST_SHOWN_ID = "last_shown_notification_id"

    private val _activeNotification = MutableStateFlow<SwaaraBroadcastNotification?>(null)
    val activeNotification: StateFlow<SwaaraBroadcastNotification?> = _activeNotification.asStateFlow()

    /**
     * Checks Firestore for active broadcast push notifications.
     * If a new notification is found and has not been displayed yet on this device,
     * it fires an Android system status bar notification with sound and vibration.
     */
    suspend fun checkAndPostNotifications(context: Context): SwaaraBroadcastNotification? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val doc = SwaaraCloudConfig.getDocument("notifications/broadcast")
            if (doc != null) {
                val enabled = doc["enabled"] as? Boolean ?: true
                val id = doc["id"] as? String ?: ""
                val title = doc["title"] as? String ?: ""
                val message = doc["message"] as? String ?: ""
                val actionUrl = doc["actionUrl"] as? String
                val timestamp = when (val t = doc["timestamp"]) {
                    is Number -> t.toLong()
                    is String -> t.toLongOrNull() ?: 0L
                    else -> 0L
                }

                if (!enabled || id.isBlank() || title.isBlank()) {
                    Log.d(TAG, "No active broadcast notification found in cloud.")
                    return@withContext null
                }

                val notification = SwaaraBroadcastNotification(
                    id = id,
                    title = title,
                    message = message,
                    actionUrl = actionUrl,
                    enabled = enabled,
                    timestamp = timestamp
                )
                _activeNotification.value = notification

                val lastShownId = prefs.getString(KEY_LAST_SHOWN_ID, "")
                if (id != lastShownId) {
                    Log.i(TAG, "New cloud push notification received: [$id] $title")
                    showSystemNotification(context, notification)
                    prefs.edit().putString(KEY_LAST_SHOWN_ID, id).apply()
                } else {
                    Log.d(TAG, "Notification $id already displayed previously.")
                }

                return@withContext notification
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check cloud push notifications", e)
        }
        null
    }

    /**
     * Displays a system status bar notification with high priority, sound, vibration, and tap intent.
     */
    fun showSystemNotification(context: Context, notification: SwaaraBroadcastNotification) {
        try {
            val contentIntent = if (!notification.actionUrl.isNullOrBlank() && notification.actionUrl.startsWith("http")) {
                Intent(Intent.ACTION_VIEW, Uri.parse(notification.actionUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("from_push_notification", true)
                    putExtra("notification_id", notification.id)
                    putExtra("notification_title", notification.title)
                    putExtra("notification_message", notification.message)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notification.id.hashCode(),
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            ServiceNotifications.announcement.sendNotification(context) {
                setSmallIcon(R.drawable.app_icon)
                setContentTitle(notification.title)
                setContentText(notification.message)
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(notification.title)
                        .bigText(notification.message)
                )
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setPriority(NotificationCompat.PRIORITY_HIGH)
                if (!notification.actionUrl.isNullOrBlank()) {
                    addAction(R.drawable.radio, "Open", pendingIntent)
                }
                this
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting system notification", e)
        }
    }

    /**
     * Sends an immediate test notification to verify system notification delivery.
     */
    fun postTestNotification(context: Context) {
        val testNotif = SwaaraBroadcastNotification(
            id = "test_${System.currentTimeMillis()}",
            title = "A1 Swaara Push Notifications",
            message = "Push notifications are active and working on this device.",
            actionUrl = "https://github.com/VarshuAi/A1Swaara_apk"
        )
        showSystemNotification(context, testNotif)
    }

    /**
     * Clears local record of last shown notification, forcing the next fetch to display.
     */
    fun resetLastShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LAST_SHOWN_ID).apply()
    }
}
