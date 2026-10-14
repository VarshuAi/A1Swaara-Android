package com.varshuai.a1swaara.ui.screens.settings

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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.cloud.SwaaraCloudAuth
import com.varshuai.a1swaara.cloud.SwaaraCloudSync
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import com.varshuai.a1swaara.ui.components.SwaaraDeepSurface
import com.varshuai.a1swaara.ui.components.SwaaraElevatedSurface
import com.varshuai.a1swaara.ui.components.SwaaraJamRoomDialog
import com.varshuai.a1swaara.ui.components.SwaaraMagenta
import com.varshuai.a1swaara.ui.components.SwaaraObsidian
import com.varshuai.a1swaara.ui.components.SwaaraPrimaryText
import com.varshuai.a1swaara.ui.components.SwaaraSecondaryText
import com.varshuai.a1swaara.ui.components.SwaaraSharePlaylistDialog
import com.varshuai.a1swaara.ui.components.SwaaraViolet
import com.varshuai.a1swaara.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RaagaCloudSyncScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { SwaaraCloudAuth.getInstance(context) }

    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    var isEditingId by remember { mutableStateOf(false) }
    var targetIdInput by remember { mutableStateOf("") }

    var showJamDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val lastSyncFormatted = remember(auth.lastSyncTimestamp) {
        if (auth.lastSyncTimestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(auth.lastSyncTimestamp))
        } else {
            "Never backed up"
        }
    }

    if (showJamDialog) {
        SwaaraJamRoomDialog(onDismiss = { showJamDialog = false })
    }
    if (showImportDialog) {
        SwaaraSharePlaylistDialog(onDismiss = { showImportDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Account Identity Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SwaaraDeepSurface)
                .border(1.dp, SwaaraMagenta.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                        Column {
                            Text(
                                text = "A1 Swaara Cloud",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SwaaraPrimaryText
                            )
                            Text(
                                text = "Live Firebase backend connected",
                                fontSize = 11.5.sp,
                                color = SwaaraCyan
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Swaara ID", auth.userId))
                                context.toast("Sync ID copied")
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Copy ID", fontSize = 11.sp, color = SwaaraPrimaryText, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isEditingId) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SwaaraElevatedSurface)
                            .padding(12.dp)
                    ) {
                        Text("Cross-Device Sync ID", fontSize = 11.sp, color = SwaaraSecondaryText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = auth.userId,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SwaaraCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Link to another device: ",
                                fontSize = 11.sp,
                                color = SwaaraSecondaryText
                            )
                            Text(
                                text = "Tap here to paste ID",
                                fontSize = 11.sp,
                                color = SwaaraViolet,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    targetIdInput = auth.userId
                                    isEditingId = true
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SwaaraElevatedSurface)
                            .padding(12.dp)
                    ) {
                        OutlinedTextField(
                            value = targetIdInput,
                            onValueChange = { targetIdInput = it },
                            label = { Text("Paste Sync ID from PC or Tablet", fontSize = 11.sp) },
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
                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                color = SwaaraSecondaryText,
                                modifier = Modifier
                                    .clickable { isEditingId = false }
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SwaaraCyan)
                                    .clickable {
                                        if (targetIdInput.isNotBlank()) {
                                            auth.linkAccount(targetIdInput)
                                            isEditingId = false
                                            context.toast("Account linked to new Sync ID")
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Link", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last backup: $lastSyncFormatted",
                    fontSize = 11.5.sp,
                    color = SwaaraSecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Auto-Sync Setting Card
        var autoSyncEnabled by remember { mutableStateOf(auth.isAutoSyncEnabled) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SwaaraElevatedSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SwaaraCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.sync),
                            contentDescription = null,
                            tint = SwaaraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Sync Library",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SwaaraPrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Automatically backup favorites and playlists to cloud in background",
                            fontSize = 11.sp,
                            color = SwaaraSecondaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { checked ->
                        autoSyncEnabled = checked
                        auth.isAutoSyncEnabled = checked
                        context.toast(if (checked) "Auto-sync enabled" else "Auto-sync disabled")
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SwaaraCyan,
                        uncheckedThumbColor = SwaaraSecondaryText,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Backup & Restore Actions
        Text(
            text = "LIBRARY BACKUP & RESTORE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SwaaraSecondaryText,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Backup Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SwaaraMagenta)
                    .clickable(enabled = !isBackingUp && !isRestoring) {
                        scope.launch {
                            isBackingUp = true
                            statusMessage = "Uploading library to cloud..."
                            val res = SwaaraCloudSync.backup(context)
                            isBackingUp = false
                            statusMessage = res.message
                            context.toast(res.message)
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isBackingUp) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(R.drawable.sync), null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Backup Library", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Restore Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SwaaraElevatedSurface)
                    .border(1.dp, SwaaraCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable(enabled = !isBackingUp && !isRestoring) {
                        scope.launch {
                            isRestoring = true
                            statusMessage = "Restoring library from cloud..."
                            val res = SwaaraCloudSync.restore(context)
                            isRestoring = false
                            statusMessage = res.message
                            context.toast(res.message)
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(color = SwaaraCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(R.drawable.download), null, tint = SwaaraCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Restore Library", color = SwaaraCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                color = SwaaraCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Collaborative & Social Tools
        Text(
            text = "COLLABORATIVE FEATURES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SwaaraSecondaryText,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Listen Together Entry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SwaaraElevatedSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .clickable { showJamDialog = true }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SwaaraViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.radio), null, tint = SwaaraViolet, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Listen Together (Live Jam)", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = SwaaraPrimaryText)
                    Text("Host or join synchronized music sessions with friends", fontSize = 11.5.sp, color = SwaaraSecondaryText)
                }
                Icon(painterResource(R.drawable.chevron_forward), null, tint = SwaaraSecondaryText, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Import Shared Playlist Entry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SwaaraElevatedSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .clickable { showImportDialog = true }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SwaaraCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.share_social), null, tint = SwaaraCyan, modifier = Modifier.size(19.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Import Shared Playlist", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = SwaaraPrimaryText)
                    Text("Enter a 6-digit playlist code to import tracks to library", fontSize = 11.5.sp, color = SwaaraSecondaryText)
                }
                Icon(painterResource(R.drawable.chevron_forward), null, tint = SwaaraSecondaryText, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cloud Push Notifications Entry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SwaaraElevatedSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SwaaraMagenta.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.sparkles), null, tint = SwaaraMagenta, modifier = Modifier.size(19.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloud Push Notifications", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = SwaaraPrimaryText)
                        Text("Real-time cloud alerts, announcements, and updates", fontSize = 11.5.sp, color = SwaaraSecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SwaaraMagenta.copy(alpha = 0.15f))
                            .clickable {
                                scope.launch {
                                    val notif = com.varshuai.a1swaara.cloud.SwaaraPushNotificationEngine.checkAndPostNotifications(context)
                                    if (notif != null) {
                                        context.toast("Notification received: ${notif.title}")
                                    } else {
                                        context.toast("No new notifications pending")
                                    }
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Check Now", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = SwaaraMagenta)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SwaaraCyan.copy(alpha = 0.15f))
                            .clickable {
                                com.varshuai.a1swaara.cloud.SwaaraPushNotificationEngine.postTestNotification(context)
                                context.toast("Test notification posted to system tray")
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Test Alert", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = SwaaraCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
