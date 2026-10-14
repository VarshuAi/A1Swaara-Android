package com.varshuai.a1swaara.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.cloud.SharedPlaylistData
import com.varshuai.a1swaara.cloud.SwaaraCloudAuth
import com.varshuai.a1swaara.cloud.SwaaraCloudSync
import com.varshuai.a1swaara.cloud.SwaaraListenTogether
import com.varshuai.a1swaara.cloud.SwaaraSharedPlaylists
import com.varshuai.a1swaara.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SwaaraCloudSyncDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { SwaaraCloudAuth.getInstance(context) }

    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var isEditingId by remember { mutableStateOf(false) }
    var customIdInput by remember { mutableStateOf("") }

    val lastSyncFormatted = remember(auth.lastSyncTimestamp) {
        if (auth.lastSyncTimestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(Date(auth.lastSyncTimestamp))
        } else {
            "Never synced"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SwaaraDeepSurface)
                .border(1.dp, SwaaraMagenta.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SwaaraMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.sync),
                                contentDescription = null,
                                tint = SwaaraMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cloud Sync & Backup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwaaraPrimaryText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Close",
                            tint = SwaaraSecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Account / Sync ID Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Cloud Sync ID (Share with PC or other devices)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SwaaraSecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isEditingId) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = auth.userId,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SwaaraCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable {
                                            val clipboard =
                                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText(
                                                    "Swaara ID",
                                                    auth.userId
                                                )
                                            )
                                            context.toast("Sync ID copied to clipboard")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Copy", fontSize = 11.sp, color = SwaaraPrimaryText)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SwaaraViolet.copy(alpha = 0.2f))
                                        .clickable {
                                            customIdInput = auth.userId
                                            isEditingId = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Link ID", fontSize = 11.sp, color = SwaaraViolet)
                                }
                            }
                        }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = customIdInput,
                                onValueChange = { customIdInput = it },
                                label = { Text("Paste Target Sync ID", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SwaaraCyan,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = SwaaraPrimaryText,
                                    unfocusedTextColor = SwaaraPrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .clickable { isEditingId = false }
                                        .padding(8.dp)
                                ) {
                                    Text("Cancel", fontSize = 12.sp, color = SwaaraSecondaryText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SwaaraCyan)
                                        .clickable {
                                            if (customIdInput.isNotBlank()) {
                                                auth.linkAccount(customIdInput)
                                                isEditingId = false
                                                context.toast("Linked to new Sync ID")
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last synced: $lastSyncFormatted",
                        fontSize = 11.sp,
                        color = SwaaraSecondaryText
                    )
                }

                if (statusText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = SwaaraCyan,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Backup Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SwaaraMagenta)
                        .clickable(enabled = !isBackingUp && !isRestoring) {
                            scope.launch {
                                isBackingUp = true
                                statusText = "Uploading favorites and playlists..."
                                val res = SwaaraCloudSync.backup(context)
                                isBackingUp = false
                                statusText = res.message
                                context.toast(res.message)
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Backup Library to Cloud",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Restore Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, SwaaraCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable(enabled = !isBackingUp && !isRestoring) {
                            scope.launch {
                                isRestoring = true
                                statusText = "Downloading library from cloud..."
                                val res = SwaaraCloudSync.restore(context)
                                isRestoring = false
                                statusText = res.message
                                context.toast(res.message)
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(color = SwaaraCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Restore Library from Cloud",
                            color = SwaaraCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwaaraSharePlaylistDialog(
    playlistId: Long? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPublishing by remember { mutableStateOf(false) }
    var shareCode by remember { mutableStateOf<String?>(null) }
    var shareUrl by remember { mutableStateOf<String?>(null) }

    var importCodeInput by remember { mutableStateOf("") }
    var isFetchingPreview by remember { mutableStateOf(false) }
    var previewPlaylist by remember { mutableStateOf<SharedPlaylistData?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SwaaraDeepSurface)
                .border(1.dp, SwaaraCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (playlistId != null) "Share Playlist" else "Explore / Import Playlist",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwaaraPrimaryText
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.close), null, tint = SwaaraSecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (playlistId != null) {
                    // Sharing current playlist
                    if (shareCode == null) {
                        Text(
                            text = "Generate a public 6-digit share code so friends or other devices can import this playlist instantly.",
                            fontSize = 13.sp,
                            color = SwaaraSecondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SwaaraMagenta)
                                .clickable(enabled = !isPublishing) {
                                    scope.launch {
                                        isPublishing = true
                                        val res = SwaaraSharedPlaylists.publishPlaylist(context, playlistId)
                                        isPublishing = false
                                        if (res.success) {
                                            shareCode = res.code
                                            shareUrl = res.shareUrl
                                        } else {
                                            context.toast(res.message)
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Generate Share Code", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Display Code
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Share Code", fontSize = 12.sp, color = SwaaraSecondaryText)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = shareCode ?: "",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SwaaraMagenta,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SwaaraElevatedSurface)
                                    .border(1.dp, SwaaraCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Playlist Code", shareCode))
                                        context.toast("Code copied: $shareCode")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text("Copy 6-Digit Code", color = SwaaraCyan, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    // Import Mode
                    OutlinedTextField(
                        value = importCodeInput,
                        onValueChange = { importCodeInput = it.uppercase().take(6) },
                        label = { Text("Enter 6-Digit Code (e.g. SW89A2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SwaaraCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = SwaaraPrimaryText,
                            unfocusedTextColor = SwaaraPrimaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SwaaraViolet)
                            .clickable(enabled = !isFetchingPreview && importCodeInput.length >= 4) {
                                scope.launch {
                                    isFetchingPreview = true
                                    previewPlaylist = SwaaraSharedPlaylists.fetchPlaylist(importCodeInput)
                                    isFetchingPreview = false
                                    if (previewPlaylist == null) {
                                        context.toast("No playlist found with code $importCodeInput")
                                    }
                                }
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFetchingPreview) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Find Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    previewPlaylist?.let { pl ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SwaaraElevatedSurface)
                                .padding(14.dp)
                        ) {
                            Text(pl.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SwaaraPrimaryText)
                            Text("by ${pl.author} • ${pl.songs.size} tracks", fontSize = 12.sp, color = SwaaraSecondaryText)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SwaaraCyan)
                                    .clickable(enabled = !isImporting) {
                                        scope.launch {
                                            isImporting = true
                                            SwaaraSharedPlaylists.importPlaylist(pl)
                                            isImporting = false
                                            context.toast("Imported \"${pl.name}\" to your library!")
                                            onDismiss()
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isImporting) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Import to My Library", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwaaraJamRoomDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeRoom by SwaaraListenTogether.currentRoom.collectAsState()

    var isCreating by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SwaaraDeepSurface)
                .border(1.dp, SwaaraViolet.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SwaaraViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.radio),
                                contentDescription = null,
                                tint = SwaaraViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Listen Together (Live Jam)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwaaraPrimaryText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.close), null, tint = SwaaraSecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeRoom == null) {
                    Text(
                        text = "Synchronize playback in real time with friends. One person plays, everyone hears the same song and seek position.",
                        fontSize = 12.5.sp,
                        color = SwaaraSecondaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Host Room Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SwaaraMagenta)
                            .clickable(enabled = !isCreating && !isJoining) {
                                scope.launch {
                                    isCreating = true
                                    val code = SwaaraListenTogether.createRoom(context)
                                    isCreating = false
                                    if (code != null) {
                                        context.toast("Room created: $code")
                                    } else {
                                        context.toast("Failed to create Jam Room")
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Host a Jam Room", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("— OR JOIN AN EXISTING ROOM —", fontSize = 11.sp, color = SwaaraSecondaryText, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it.uppercase().take(6) },
                        label = { Text("Room Code (e.g. JAM821)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SwaaraViolet,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = SwaaraPrimaryText,
                            unfocusedTextColor = SwaaraPrimaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SwaaraElevatedSurface)
                            .border(1.dp, SwaaraViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isCreating && !isJoining && joinCodeInput.length >= 4) {
                                scope.launch {
                                    isJoining = true
                                    val room = SwaaraListenTogether.joinRoom(context, joinCodeInput)
                                    isJoining = false
                                    if (room != null) {
                                        context.toast("Joined ${room.hostName}'s room!")
                                    } else {
                                        context.toast("Room not found or expired")
                                    }
                                }
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(color = SwaaraViolet, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Join Room", color = SwaaraViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val room = activeRoom!!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (room.isHost) "Hosting Jam Room" else "Listening with ${room.hostName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (room.isHost) SwaaraMagenta else SwaaraCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = room.roomCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SwaaraPrimaryText,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Jam Room Code", room.roomCode))
                                    context.toast("Room code copied: ${room.roomCode}")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Copy Code to Invite Friends", fontSize = 11.sp, color = SwaaraPrimaryText)
                        }

                        if (room.title.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SwaaraElevatedSurface)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Now In Sync:", fontSize = 11.sp, color = SwaaraSecondaryText)
                                Text(room.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SwaaraPrimaryText)
                                Text(room.artist, fontSize = 12.sp, color = SwaaraCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF8B0000).copy(alpha = 0.35f))
                                .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        SwaaraListenTogether.leaveRoom()
                                        context.toast("Left Jam Room")
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Leave Room", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
