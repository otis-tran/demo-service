package com.otistran.demo_service.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.otistran.demo_service.service.MusicPlaybackService

@Composable
fun MusicPlayerDemo() {
    val context = LocalContext.current
    var playerState by remember { mutableStateOf("Stopped") }
    var startCount by remember { mutableStateOf(0) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Request permission khi cần
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Foreground Service Music Player Demo")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Status: $playerState")
        Text(text = "Start count: $startCount")

        if (!hasNotificationPermission) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚠️ Notification permission required",
                color = Color.Red
            )
            NotificationSettingsButton()
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            ) {
                Text("Grant Permission")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    startCount++
                    playerState = "Playing #$startCount"
                    playMusic(context)
                },
                enabled = hasNotificationPermission
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    playerState = "Paused"
                    pauseMusic(context)
                },
                enabled = hasNotificationPermission
            ) {
                Icon(Icons.Default.Close, contentDescription = "Pause")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pause")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    playerState = "Stopped"
                    stopMusic(context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                enabled = hasNotificationPermission
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop")
            }
        }
    }
}

private fun playMusic(context: Context) {
    val intent = Intent(context, MusicPlaybackService::class.java).apply {
        action = MusicPlaybackService.ACTION_PLAY
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    } catch (e: Exception) {
        android.util.Log.e("MusicPlayerDemo", "Error starting service", e)
    }
}

private fun pauseMusic(context: Context) {
    val intent = Intent(context, MusicPlaybackService::class.java).apply {
        action = MusicPlaybackService.ACTION_PAUSE
    }
    context.startService(intent)
}

private fun stopMusic(context: Context) {
    val intent = Intent(context, MusicPlaybackService::class.java).apply {
        action = MusicPlaybackService.ACTION_STOP
    }
    context.startService(intent)
}


@Composable
fun NotificationSettingsButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    ) {
        Text("Open Notification Settings")
    }
}