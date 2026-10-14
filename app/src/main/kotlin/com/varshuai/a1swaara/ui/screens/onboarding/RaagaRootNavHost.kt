package com.varshuai.a1swaara.ui.screens.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.ui.screens.RaagaAppShell
import com.varshuai.a1swaara.updater.SwaaraForceUpdateDialog
import com.varshuai.a1swaara.updater.SwaaraUpdateManager

enum class RaagaNavDestination {
    Splash,
    Onboarding,
    MainShell
}

@Composable
fun RaagaRootNavHost() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var destination by remember { mutableStateOf(RaagaNavDestination.Splash) }

    val latestRelease by SwaaraUpdateManager.latestRelease.collectAsStateWithLifecycle()
    val updateInfo by SwaaraUpdateManager.updateInfo.collectAsStateWithLifecycle()
    val isUpdateAvailable by SwaaraUpdateManager.isUpdateAvailable.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Automatically check GitHub repository and raw version.json for OTA updates on startup
        SwaaraUpdateManager.checkForUpdates(context = context, forceCheck = false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = destination, label = "raagaNav") { dest ->
            when (dest) {
                RaagaNavDestination.Splash -> {
                    RaagaSplashScreen(
                        onSplashFinished = { onboardingNeeded ->
                            destination = if (onboardingNeeded) RaagaNavDestination.Onboarding else RaagaNavDestination.MainShell
                        }
                    )
                }
                RaagaNavDestination.Onboarding -> {
                    RaagaOnboardingScreen(
                        onOnboardingFinished = {
                            destination = RaagaNavDestination.MainShell
                        }
                    )
                }
                RaagaNavDestination.MainShell -> {
                    RaagaAppShell()
                }
            }
        }

        // OTA Update Modal Overlay if newer release is detected
        if (showDialog && isUpdateAvailable && (updateInfo != null || latestRelease != null)) {
            SwaaraForceUpdateDialog(
                release = latestRelease,
                updateInfo = updateInfo,
                onDismiss = {
                    showDialog = false
                    SwaaraUpdateManager.setMandatoryLock(true)
                }
            )
        }
    }
}
