package com.varshuai.a1swaara.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.varshuai.a1swaara.BuildConfig
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.service.ServiceNotifications
import com.varshuai.a1swaara.ui.components.themed.CircularProgressIndicator
import com.varshuai.a1swaara.ui.components.themed.DefaultDialog
import com.varshuai.a1swaara.ui.components.themed.SecondaryTextButton
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.utils.bold
import com.varshuai.a1swaara.utils.center
import com.varshuai.a1swaara.utils.hasPermission
import com.varshuai.a1swaara.utils.pendingIntent
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.core.data.utils.Version
import app.a1swaara.core.data.utils.version
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isAtLeastAndroid13
import app.a1swaara.core.ui.utils.isCompositionLaunched
import app.a1swaara.providers.github.GitHub
import app.a1swaara.providers.github.models.Release
import app.a1swaara.providers.github.requests.releases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.toJavaDuration

import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.updater.SwaaraForceUpdateDialog
import com.varshuai.a1swaara.updater.SwaaraUpdateManager
import kotlinx.coroutines.launch

private val VERSION_NAME = BuildConfig.VERSION_NAME.substringBeforeLast("-")
private const val REPO_OWNER = "VarshuAi"
private const val REPO_NAME = "A1Swaara_apk"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val permission = Manifest.permission.POST_NOTIFICATIONS

class VersionCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val WORK_TAG = "version_check_worker"

        fun upsert(context: Context, period: Duration?) = runCatching {
            val workManager = WorkManager.getInstance(context)

            if (period == null) {
                workManager.cancelAllWorkByTag(WORK_TAG)
                return@runCatching
            }

            val request = PeriodicWorkRequestBuilder<VersionCheckWorker>(period.toJavaDuration())
                .addTag(WORK_TAG)
                .setConstraints(
                    Constraints(
                        requiredNetworkType = NetworkType.CONNECTED,
                        requiresBatteryNotLow = true
                    )
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                /* uniqueWorkName = */ WORK_TAG,
                /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                /* periodicWork = */ request
            )

            Unit
        }.also { it.exceptionOrNull()?.printStackTrace() }
    }

    override suspend fun doWork(): Result = with(applicationContext) {
        if (isAtLeastAndroid13 && !hasPermission(permission)) return Result.retry()

        val update = SwaaraUpdateManager.checkForUpdates(
            context = applicationContext,
            forceCheck = false
        )

        return if (update != null) Result.success() else Result.retry()
    }
}

private suspend fun Version.getNewerVersion(
    repoOwner: String = REPO_OWNER,
    repoName: String = REPO_NAME,
    contentType: String = "application/vnd.android.package-archive"
) = GitHub.releases(
    owner = repoOwner,
    repo = repoName
)?.mapCatching { releases ->
    releases
        .sortedByDescending { it.publishedAt }
        .firstOrNull { release ->
            !release.draft &&
                    !release.preRelease &&
                    release.tag.version > this &&
                    release.assets.any {
                        it.contentType == contentType && it.state == Release.Asset.State.Uploaded
                    }
        }
}

@Route
@Composable
fun About() = SettingsCategoryScreen(
    title = stringResource(R.string.about),
    description = "v$VERSION_NAME • Official Production Release"
) {
    val (_, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(
            if (isAtLeastAndroid13) context.applicationContext.hasPermission(permission)
            else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    SettingsGroup(title = "Developer") {
        SettingsEntry(
            title = "Developer & Creator",
            text = "VarshuAi (@varshuai)",
            onClick = {
                uriHandler.openUri("https://github.com/varshuai")
            }
        )

        SettingsEntry(
            title = "GitHub Profile",
            text = "https://github.com/varshuai",
            onClick = {
                uriHandler.openUri("https://github.com/varshuai")
            }
        )

        SettingsEntry(
            title = "App Repository & APK Releases",
            text = "github.com/VarshuAi/A1Swaara_apk",
            onClick = {
                uriHandler.openUri("https://github.com/VarshuAi/A1Swaara_apk")
            }
        )
    }

    SettingsGroup(title = "Support & Feedback") {
        SettingsEntry(
            title = "Report an Issue",
            text = "Report bugs or playback problems directly to developer",
            onClick = {
                uriHandler.openUri("https://github.com/VarshuAi/A1Swaara_apk/issues/new")
            }
        )

        SettingsEntry(
            title = "Suggest a Feature",
            text = "Share ideas or feature requests for upcoming releases",
            onClick = {
                uriHandler.openUri("https://github.com/VarshuAi/A1Swaara_apk/issues/new")
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var otaDialogOpen by rememberSaveable { mutableStateOf(false) }
    val updateInfo by SwaaraUpdateManager.updateInfo.collectAsStateWithLifecycle()
    val latestRelease by SwaaraUpdateManager.latestRelease.collectAsStateWithLifecycle()

    SettingsGroup(title = stringResource(R.string.version)) {
        SettingsEntry(
            title = stringResource(R.string.check_new_version),
            text = if (isCheckingUpdates) "Checking for OTA updates..." else stringResource(R.string.current_version, VERSION_NAME),
            onClick = {
                if (isAtLeastAndroid13 && !hasPermission) {
                    launcher.launch(permission)
                }
                coroutineScope.launch {
                    isCheckingUpdates = true
                    val update = SwaaraUpdateManager.checkForUpdates(
                        context = context,
                        forceCheck = true,
                        showToastIfUpToDate = true
                    )
                    isCheckingUpdates = false
                    if (update != null) {
                        otaDialogOpen = true
                    }
                }
            }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(R.string.version_check),
            selectedValue = DataPreferences.versionCheckPeriod,
            onValueSelect = onSelect@{
                DataPreferences.versionCheckPeriod = it
                if (isAtLeastAndroid13 && it.period != null && !hasPermission)
                    launcher.launch(permission)

                VersionCheckWorker.upsert(context.applicationContext, it.period)
            },
            valueText = { it.displayName() }
        )
    }

    if (otaDialogOpen && (updateInfo != null || latestRelease != null)) {
        SwaaraForceUpdateDialog(
            release = latestRelease,
            updateInfo = updateInfo,
            onDismiss = { otaDialogOpen = false }
        )
    }
}
