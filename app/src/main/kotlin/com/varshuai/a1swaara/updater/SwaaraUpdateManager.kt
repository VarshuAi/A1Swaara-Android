package com.varshuai.a1swaara.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.BuildConfig
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.service.ServiceNotifications
import com.varshuai.a1swaara.utils.toast
import app.a1swaara.providers.github.GitHub
import app.a1swaara.providers.github.models.Release
import app.a1swaara.providers.github.requests.releases
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

private val SwaaraDeepSurface = Color(0xFF111119)
private val SwaaraElevatedSurface = Color(0xFF191924)
private val SwaaraMagenta = Color(0xFFFF2DAA)
private val SwaaraViolet = Color(0xFF8B35FF)
private val SwaaraCyan = Color(0xFF20CFFF)
private val SwaaraPrimaryText = Color(0xFFF1F1F5)
private val SwaaraSecondaryText = Color(0xFF8E8EA0)

data class SwaaraUpdateInfo(
    val version: String,
    val versionCode: Int = 1,
    val title: String = "A1 Swaara Update",
    val changelog: String = "",
    val downloadUrl: String = "",
    val githubReleaseUrl: String = "",
    val forceUpdate: Boolean = false
)

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object SwaaraUpdateManager {
    const val REPO_OWNER = "VarshuAi"
    const val REPO_NAME = "A1Swaara_apk"
    const val DEV_USERNAME = "varshuai"
    const val DEV_GITHUB_URL = "https://github.com/varshuai"
    const val REPO_URL = "https://github.com/VarshuAi/A1Swaara_apk"
    const val RELEASES_URL = "https://github.com/VarshuAi/A1Swaara_apk/releases/latest"
    const val VERSION_JSON_URL = "https://raw.githubusercontent.com/VarshuAi/A1Swaara_apk/main/version.json"

    private const val TAG = "SwaaraUpdateManager"

    private val _latestRelease = MutableStateFlow<Release?>(null)
    val latestRelease = _latestRelease.asStateFlow()

    private val _updateInfo = MutableStateFlow<SwaaraUpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable = _isUpdateAvailable.asStateFlow()

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    private val _isMandatoryLockActive = MutableStateFlow(false)
    val isMandatoryLockActive = _isMandatoryLockActive.asStateFlow()

    fun setMandatoryLock(active: Boolean) {
        _isMandatoryLockActive.value = active
    }

    fun resetDownloadState() {
        _downloadState.value = UpdateDownloadState.Idle
    }

    /**
     * Primary OTA check function.
     * Checks raw version.json (unthrottled) and GitHub Releases API, sends high-priority
     * system status-bar notification, and updates reactive state flows.
     */
    suspend fun checkForUpdates(
        context: Context,
        forceCheck: Boolean = false,
        showToastIfUpToDate: Boolean = false
    ): SwaaraUpdateInfo? = withContext(Dispatchers.IO) {
        val currentVersion = BuildConfig.VERSION_NAME.trim().removePrefix("v").substringBeforeLast("-")
        val currentVersionCode = BuildConfig.VERSION_CODE
        var detectedUpdate: SwaaraUpdateInfo? = null

        // 1. Check raw version.json (Fast, reliable, with cache-busting timestamp)
        try {
            val url = URL("$VERSION_JSON_URL?t=${System.currentTimeMillis()}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "A1Swaara-Android")
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonText)
                val rawVersion = json.optString("version", "").trim().removePrefix("v")

                if (rawVersion.isNotBlank()) {
                    val remoteVersionCode = json.optInt("versionCode", 1)
                    val info = SwaaraUpdateInfo(
                        version = rawVersion,
                        versionCode = remoteVersionCode,
                        title = json.optString("title", "A1 Swaara v$rawVersion"),
                        changelog = json.optString("changelog", ""),
                        downloadUrl = json.optString("downloadUrl", "https://github.com/VarshuAi/A1Swaara_apk/raw/main/A1_Swaara_v$rawVersion.apk"),
                        githubReleaseUrl = json.optString("githubReleaseUrl", RELEASES_URL),
                        forceUpdate = json.optBoolean("forceUpdate", true)
                    )

                    val isSameVersion = rawVersion.equals(currentVersion, ignoreCase = true)
                    val isNewer = if (isSameVersion) {
                        false
                    } else if (remoteVersionCode > 0 && currentVersionCode > 0) {
                        remoteVersionCode > currentVersionCode
                    } else {
                        isRemoteVersionNewer(currentVersion, rawVersion)
                    }

                    if (isNewer) {
                        detectedUpdate = info
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "version.json check failed, falling back to GitHub API", e)
        }

        // 2. Fallback: Check GitHub Releases API
        if (detectedUpdate == null) {
            try {
                val releasesResult = GitHub.releases(owner = REPO_OWNER, repo = REPO_NAME)
                val releases = releasesResult?.getOrNull()
                val latest = releases?.filter { !it.draft }?.maxByOrNull { it.publishedAt?.time ?: it.createdAt.time }

                if (latest != null) {
                    _latestRelease.value = latest
                    val remoteVersion = latest.tag.trim().removePrefix("v")
                    val isSameVersion = remoteVersion.equals(currentVersion, ignoreCase = true)
                    val isNewer = !isSameVersion && isRemoteVersionNewer(currentVersion, remoteVersion)

                    if (isNewer) {
                        val apkAsset = latest.assets.firstOrNull {
                            it.name.endsWith(".apk", ignoreCase = true) || it.contentType.contains("android")
                        }
                        detectedUpdate = SwaaraUpdateInfo(
                            version = remoteVersion,
                            versionCode = currentVersionCode + 1,
                            title = latest.name ?: "A1 Swaara $remoteVersion",
                            changelog = latest.markdown ?: "",
                            downloadUrl = apkAsset?.downloadUrl?.toString() ?: "https://github.com/VarshuAi/A1Swaara_apk/raw/main/A1_Swaara_v$remoteVersion.apk",
                            githubReleaseUrl = latest.frontendUrl.toString(),
                            forceUpdate = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "GitHub releases check failed", e)
            }
        }

        // 3. Process Result & Post OTA Notification
        if (detectedUpdate != null) {
            _updateInfo.value = detectedUpdate
            _isUpdateAvailable.value = true

            Log.i(TAG, "OTA Update Found: v${detectedUpdate.version} (Current: $currentVersion)")
            postOtaNotification(context, detectedUpdate)
            return@withContext detectedUpdate
        } else {
            _isUpdateAvailable.value = false
            _isMandatoryLockActive.value = false
            _updateInfo.value = null
            Log.i(TAG, "A1 Swaara is up to date (v$currentVersion)")
            if (showToastIfUpToDate) {
                withContext(Dispatchers.Main) {
                    context.toast("A1 Swaara is already on the latest version (v$currentVersion)")
                }
            }
            return@withContext null
        }
    }

    /**
     * Downloads the APK directly inside the app using OkHttp with streaming progress,
     * saving it to app-specific storage and prompting the Android package installer.
     */
    suspend fun downloadAndInstallUpdate(context: Context, update: SwaaraUpdateInfo) = withContext(Dispatchers.IO) {
        val targetUrl = update.downloadUrl.takeIf { it.isNotBlank() }
            ?: "https://github.com/VarshuAi/A1Swaara_apk/raw/main/A1_Swaara_v${update.version}.apk"

        try {
            _downloadState.value = UpdateDownloadState.Downloading(0, 0, 0)

            val updateDir = File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "A1_Swaara_v${update.version}.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "A1Swaara-Android")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP error ${response.code}: ${response.message}")
                }
                val body = response.body ?: throw IOException("Empty response body from update server")
                val totalLength = body.contentLength()

                body.byteStream().use { inputStream ->
                    FileOutputStream(apkFile).use { outputStream ->
                        val buffer = ByteArray(32 * 1024)
                        var bytesRead: Int
                        var downloaded = 0L
                        var lastReportedPercent = -1

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            val percent = if (totalLength > 0) ((downloaded * 100) / totalLength).toInt() else 0
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent
                                _downloadState.value = UpdateDownloadState.Downloading(percent, downloaded, totalLength)
                            }
                        }
                        outputStream.flush()
                    }
                }
            }

            _downloadState.value = UpdateDownloadState.ReadyToInstall(apkFile)
            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "In-app download failed", e)
            _downloadState.value = UpdateDownloadState.Error(e.message ?: "Failed to download update inside app")
        }
    }

    /**
     * Prompts the Android OS package installer to install the downloaded APK
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            context.toast("Install prompt failed: ${e.message}")
        }
    }

    /** Posts an Android System Status Bar Notification with sound, vibration, and direct action */
    fun postOtaNotification(context: Context, update: SwaaraUpdateInfo) {
        try {
            val targetUrl = update.downloadUrl.ifBlank { RELEASES_URL }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            ServiceNotifications.version.sendNotification(context) {
                setSmallIcon(R.drawable.download)
                setContentTitle("A1 Swaara Update: v${update.version}")
                setContentText("New version ${update.version} is available. Tap to update.")
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle("A1 Swaara v${update.version} Available")
                        .setSummaryText("Compulsory Update")
                        .bigText("${update.title}\n\nUpdate required to stream online songs.\n${update.changelog}")
                )
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setPriority(NotificationCompat.PRIORITY_HIGH)
                setDefaults(NotificationCompat.DEFAULT_ALL)
                addAction(R.drawable.download, "Download Update", pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTA notification", e)
        }
    }

    fun isRemoteVersionNewer(current: String, remote: String): Boolean {
        try {
            val cleanCurrent = current.trim().removePrefix("v").substringBeforeLast("-")
            val cleanRemote = remote.trim().removePrefix("v").substringBeforeLast("-")
            if (cleanCurrent.equals(cleanRemote, ignoreCase = true)) return false

            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val c = currentParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun openDownloadPage(context: Context, downloadUrl: String? = null) {
        val targetUrl = downloadUrl?.takeIf { it.isNotBlank() } ?: RELEASES_URL
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open update URL: $targetUrl", e)
        }
    }
}

/** Floating In-App OTA Notification Banner for Top of Screen */
@Composable
fun SwaaraOtaBanner(
    updateInfo: SwaaraUpdateInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = SwaaraCyan)
            .clip(RoundedCornerShape(16.dp))
            .background(SwaaraElevatedSurface)
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SwaaraMagenta.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.download),
                        contentDescription = null,
                        tint = SwaaraMagenta,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = "Update Available: v${updateInfo.version}",
                        color = SwaaraPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to download inside app",
                        color = SwaaraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.chevron_forward),
                contentDescription = null,
                tint = SwaaraSecondaryText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** Comprehensive In-App OTA Update Modal Dialog */
@Composable
fun SwaaraForceUpdateDialog(
    release: Release? = null,
    updateInfo: SwaaraUpdateInfo? = null,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadState by SwaaraUpdateManager.downloadState.collectAsStateWithLifecycle()

    val targetVersion = updateInfo?.version ?: release?.tag?.removePrefix("v") ?: "Latest"
    val targetTitle = updateInfo?.title ?: release?.name ?: "A1 Swaara v$targetVersion"
    val targetChangelog = updateInfo?.changelog ?: release?.markdown ?: ""
    val targetDownloadUrl = updateInfo?.downloadUrl
        ?: release?.assets?.firstOrNull { it.name.endsWith(".apk") }?.downloadUrl?.toString()
        ?: "https://github.com/VarshuAi/A1Swaara_apk/raw/main/A1_Swaara_v$targetVersion.apk"

    val resolvedUpdateInfo = updateInfo ?: SwaaraUpdateInfo(
        version = targetVersion,
        title = targetTitle,
        changelog = targetChangelog,
        downloadUrl = targetDownloadUrl,
        forceUpdate = true
    )

    Dialog(
        onDismissRequest = {
            SwaaraUpdateManager.setMandatoryLock(true)
            onDismiss?.invoke()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 30.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black,
                    spotColor = SwaaraMagenta
                )
                .clip(RoundedCornerShape(24.dp))
                .background(SwaaraDeepSurface)
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)),
                    RoundedCornerShape(24.dp)
                )
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, SwaaraMagenta.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = R.mipmap.ic_launcher_round,
                        contentDescription = "A1 Swaara",
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OTA Mandatory Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SwaaraMagenta.copy(alpha = 0.20f))
                        .border(1.dp, SwaaraMagenta.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MANDATORY UPDATE REQUIRED",
                        color = SwaaraMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = targetTitle,
                    color = SwaaraPrimaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "To stream online songs, search music, or browse live charts, you must update A1 Swaara to v$targetVersion. If not updated, only offline downloaded songs remain playable.",
                    color = SwaaraSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Developer Credentials Card
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SwaaraUpdateManager.DEV_GITHUB_URL)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = "Developer",
                        tint = SwaaraCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Developed by VarshuAi (@varshuai)",
                        color = SwaaraPrimaryText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Changelog Box
                if (targetChangelog.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SwaaraElevatedSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = targetChangelog,
                            color = SwaaraPrimaryText.copy(alpha = 0.9f),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // DOWNLOAD & INSTALL AREA
                when (val state = downloadState) {
                    is UpdateDownloadState.Idle -> {
                        // In-App Download Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black,
                                    spotColor = SwaaraMagenta.copy(alpha = 0.5f)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                                .clickable {
                                    coroutineScope.launch {
                                        SwaaraUpdateManager.downloadAndInstallUpdate(context, resolvedUpdateInfo)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.download),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Update Now (Download Inside App)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Offline Only Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(SwaaraElevatedSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(21.dp))
                                .clickable {
                                    SwaaraUpdateManager.setMandatoryLock(true)
                                    onDismiss?.invoke()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Play Offline Downloads Only",
                                color = SwaaraSecondaryText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SwaaraElevatedSurface)
                                .border(1.dp, SwaaraCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading update inside app...",
                                    color = SwaaraPrimaryText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${state.percent}%",
                                    color = SwaaraCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SwaaraCyan,
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val downloadedMb = String.format("%.1f", state.bytesDownloaded / (1024.0 * 1024.0))
                            val totalMb = if (state.totalBytes > 0) String.format("%.1f", state.totalBytes / (1024.0 * 1024.0)) else "--"
                            Text(
                                text = "$downloadedMb MB / $totalMb MB • Installing automatically when done",
                                color = SwaaraSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black,
                                    spotColor = SwaaraCyan
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.horizontalGradient(listOf(SwaaraCyan, SwaaraViolet)))
                                .clickable {
                                    SwaaraUpdateManager.installApk(context, state.apkFile)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Install Update Now",
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Download failed: ${state.message}",
                                color = SwaaraMagenta,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                        .background(SwaaraElevatedSurface)
                                        .border(1.dp, SwaaraCyan.copy(alpha = 0.5f), RoundedCornerShape(21.dp))
                                        .clickable {
                                            coroutineScope.launch {
                                                SwaaraUpdateManager.downloadAndInstallUpdate(context, resolvedUpdateInfo)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Retry Inside App",
                                        color = SwaaraCyan,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                        .background(SwaaraElevatedSurface)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(21.dp))
                                        .clickable {
                                            SwaaraUpdateManager.openDownloadPage(context, targetDownloadUrl)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Browser Fallback",
                                        color = SwaaraSecondaryText,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // GitHub Source Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SwaaraUpdateManager.REPO_URL)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.globe),
                        contentDescription = null,
                        tint = SwaaraCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "VarshuAi / A1Swaara_apk on GitHub",
                        color = SwaaraCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
