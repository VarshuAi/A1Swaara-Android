package com.varshuai.a1swaara.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.varshuai.a1swaara.cloud.SwaaraPushNotificationEngine
import com.varshuai.a1swaara.cloud.SwaaraRemoteConfig
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that periodically checks for cloud push notifications and remote config.
 */
class SwaaraPushWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SwaaraPushWorker"
        private const val WORK_NAME = "swaara_push_notification_worker"

        fun upsert(context: Context) = runCatching {
            val workManager = WorkManager.getInstance(context)

            val request = PeriodicWorkRequestBuilder<SwaaraPushWorker>(
                1,
                TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }.onFailure {
            Log.e(TAG, "Failed to schedule SwaaraPushWorker", it)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            SwaaraRemoteConfig.fetchAndApply(applicationContext)
            SwaaraPushNotificationEngine.checkAndPostNotifications(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SwaaraPushWorker execution error", e)
            Result.retry()
        }
    }
}
