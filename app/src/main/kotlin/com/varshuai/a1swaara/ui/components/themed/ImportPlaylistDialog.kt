package com.varshuai.a1swaara.ui.components.themed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.utils.PlaylistImporter
import com.varshuai.a1swaara.utils.PlaylistImporter.ImportProgress
import com.varshuai.a1swaara.utils.PlaylistImporter.ImportResult
import com.varshuai.a1swaara.utils.bold
import com.varshuai.a1swaara.utils.medium
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.raagaGradientEnd
import app.a1swaara.core.ui.raagaGradientStart
import app.a1swaara.core.ui.utils.roundedShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface ImportState {
    object Idle : ImportState
    data class Loading(val progress: ImportProgress) : ImportState
    data class Success(val result: ImportResult) : ImportState
    data class Error(val message: String) : ImportState
}

@Composable
fun ImportPlaylistDialog(
    onDismiss: () -> Unit,
    onImportSuccess: (ImportResult) -> Unit = {},
    modifier: Modifier = Modifier
) = DefaultDialog(
    onDismiss = onDismiss,
    modifier = modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    var urlInput by remember { mutableStateOf("") }
    var importState by remember { mutableStateOf<ImportState>(ImportState.Idle) }
    var currentJob by remember { mutableStateOf<Job?>(null) }

    val detectedPlatform = remember(urlInput) {
        PlaylistImporter.detectPlatform(urlInput)
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        Brush.linearGradient(listOf(raagaGradientStart, raagaGradientEnd)),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicText(
                text = stringResource(R.string.import_playlist),
                style = typography.m.bold.copy(color = colorPalette.text)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        BasicText(
            text = stringResource(R.string.import_playlist_hint),
            style = typography.xxs.medium.copy(
                color = colorPalette.textSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Platform badges
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val isSpotify = detectedPlatform == PlaylistImporter.Platform.SPOTIFY
            val isYouTube = detectedPlatform == PlaylistImporter.Platform.YOUTUBE

            // Spotify badge
            Box(
                modifier = Modifier
                    .clip(12.dp.roundedShape)
                    .background(
                        if (isSpotify) Color(0xFF1DB954).copy(alpha = 0.22f)
                        else colorPalette.background2
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                BasicText(
                    text = "Spotify",
                    style = typography.xxs.semiBold.copy(
                        color = if (isSpotify) Color(0xFF1DB954) else colorPalette.textDisabled
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // YouTube badge
            Box(
                modifier = Modifier
                    .clip(12.dp.roundedShape)
                    .background(
                        if (isYouTube) Color(0xFFFF0000).copy(alpha = 0.22f)
                        else colorPalette.background2
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                BasicText(
                    text = "YouTube Music",
                    style = typography.xxs.semiBold.copy(
                        color = if (isYouTube) Color(0xFFFF4E4E) else colorPalette.textDisabled
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Body content based on state
        when (val state = importState) {
            is ImportState.Idle -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPalette.background2, shape = 12.dp.roundedShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        hintText = "https://...",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (urlInput.isNotBlank()) {
                                    currentJob?.cancel()
                                    currentJob = coroutineScope.launch {
                                        importState = ImportState.Loading(
                                            ImportProgress(stage = "Starting import…")
                                        )
                                        val result = PlaylistImporter.importPlaylist(urlInput) { p ->
                                            importState = ImportState.Loading(p)
                                        }
                                        result.onSuccess { r ->
                                            importState = ImportState.Success(r)
                                            onImportSuccess(r)
                                        }.onFailure { e ->
                                            importState = ImportState.Error(
                                                e.message ?: "Failed to import playlist"
                                            )
                                        }
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    // Paste button
                    Box(
                        modifier = Modifier
                            .clip(8.dp.roundedShape)
                            .background(colorPalette.accent.copy(alpha = 0.15f))
                            .clickable {
                                clipboardManager.getText()?.text?.let { clip ->
                                    if (clip.isNotBlank()) urlInput = clip.trim()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        BasicText(
                            text = stringResource(R.string.import_playlist_paste),
                            style = typography.xxs.semiBold.copy(color = colorPalette.accent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DialogTextButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss
                    )

                    DialogTextButton(
                        primary = true,
                        enabled = urlInput.isNotBlank(),
                        text = stringResource(R.string.import_playlist),
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                currentJob?.cancel()
                                currentJob = coroutineScope.launch {
                                    importState = ImportState.Loading(
                                        ImportProgress(stage = "Starting import…")
                                    )
                                    val result = PlaylistImporter.importPlaylist(urlInput) { p ->
                                        importState = ImportState.Loading(p)
                                    }
                                    result.onSuccess { r ->
                                        importState = ImportState.Success(r)
                                        onImportSuccess(r)
                                    }.onFailure { e ->
                                        importState = ImportState.Error(
                                            e.message ?: "Failed to import playlist"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            is ImportState.Loading -> {
                val progress = state.progress
                val progressFraction by animateFloatAsState(
                    targetValue = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f,
                    label = "importProgress"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    BasicText(
                        text = progress.stage,
                        style = typography.xs.semiBold.copy(
                            color = colorPalette.text,
                            textAlign = TextAlign.Center
                        )
                    )

                    if (progress.total > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText(
                            text = "${progress.current} / ${progress.total}",
                            style = typography.xxs.medium.copy(color = colorPalette.accent)
                        )
                    }

                    if (progress.currentTrack.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BasicText(
                            text = progress.currentTrack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typography.xxs.medium.copy(
                                color = colorPalette.textSecondary,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(colorPalette.background2, shape = 3.dp.roundedShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction.coerceIn(0.04f, 1f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(raagaGradientStart, raagaGradientEnd)
                                    ),
                                    shape = 3.dp.roundedShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = {
                        currentJob?.cancel()
                        importState = ImportState.Idle
                    }
                )
            }

            is ImportState.Success -> {
                val res = state.result
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1DB954).copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.checkmark),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color(0xFF1DB954)),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    BasicText(
                        text = stringResource(
                            R.string.import_playlist_success,
                            res.importedTracksCount,
                            res.playlistName
                        ),
                        style = typography.xs.semiBold.copy(
                            color = colorPalette.text,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DialogTextButton(
                    primary = true,
                    text = stringResource(R.string.done),
                    onClick = onDismiss
                )
            }

            is ImportState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    BasicText(
                        text = state.message,
                        style = typography.xs.medium.copy(
                            color = colorPalette.red,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DialogTextButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss
                    )
                    DialogTextButton(
                        primary = true,
                        text = "Try Again",
                        onClick = { importState = ImportState.Idle }
                    )
                }
            }
        }
    }
}
