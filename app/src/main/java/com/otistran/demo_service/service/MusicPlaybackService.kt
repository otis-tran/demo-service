package com.otistran.demo_service.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlaybackService : Service() {

    private val TAG = "MusicPlaybackService"
    private val NOTIFICATION_ID = 100
    private val CHANNEL_ID = "music_playback_channel"

    private var isPlaying = false
    private var startCount = 0 // Đếm số lần start

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "🔥 onCreate() called - Service instance created")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCount++
        val action = intent?.action

        Log.d(TAG, "📞 onStartCommand() called:")
        Log.d(TAG, "   - Start count: $startCount")
        Log.d(TAG, "   - Start ID: $startId")
        Log.d(TAG, "   - Action: $action")
        Log.d(TAG, "   - Intent: ${intent?.toString()}")

        when (action) {
            ACTION_PLAY -> {
                Log.d(TAG, "Play command received")
                isPlaying = true

                // Debug trước khi start foreground
                logNotificationStatus()

                startForegroundWithNotification("Playing music (Start #$startCount)")
                // Chạy task định kỳ để prove service vẫn sống
                startPeriodicLogging()
            }

            ACTION_PAUSE -> {
                Log.d(TAG, "Pause command received")
                isPlaying = false
                updateNotification("Paused (Start #$startCount)")
            }

            ACTION_STOP -> {
                Log.d(TAG, "🛑 Stopping service...")
                isPlaying = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            "DEBUG_NOTIFICATION" -> {
                // Action mới để debug
                Log.d(TAG, "Debug notification status requested")
                logNotificationStatus()

                // Tạo một notification test
                startForegroundWithNotification("Debug Mode - Testing Notification")
            }

            else -> {
                // Trường hợp không có action cụ thể, mặc định là play
                Log.d(TAG, "Default action - starting playback")
                isPlaying = true

                logNotificationStatus()

                startForegroundWithNotification("Music Player Ready (Start #$startCount)")
            }
        }

        return START_NOT_STICKY
    }

    private fun startPeriodicLogging() {
        serviceScope.launch {
            while (isPlaying) {
                Log.d(TAG, "🎵 Service still alive - ${System.currentTimeMillis()}")
                delay(5000) // Log mỗi 5 giây
            }
        }
    }

    override fun onTaskRemoved(intent: Intent?) {
        super.onTaskRemoved(intent)
        Log.d(TAG, "📱 App was removed from recent tasks")
        // Service vẫn chạy nếu là Foreground Service
    }

    private fun startForegroundWithNotification(text: String) {
        try {
            // Debug trước khi tạo notification
            logNotificationStatus()

            val notification = createNotification(text)
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Foreground service started with notification")

            // Debug sau khi tạo notification
            logNotificationStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(text: String) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "No notification permission")
            return
        }

        try {
            val notification = createNotification(text)
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification updated")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }

    private fun createNotification(text: String): Notification {
        // Intent khi click vào notification - cần tạo một Activity
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent để tạm dừng/phát
        val pauseIntent = Intent(this, MusicPlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent để dừng
        val stopIntent = Intent(this, MusicPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Tạo notification với style media
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play) // Sử dụng icon có sẵn của system
            .setContentIntent(pendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                pausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true) // Không cho phép user swipe để dismiss
            .setPriority(NotificationCompat.PRIORITY_LOW) // Ưu tiên thấp
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiện trên lock screen
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Playback"
            val descriptionText = "Music playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created")
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Không cần permission trước Android 13
        }
    }

    // Function debug - gọi ở nhiều nơi trong service
    private fun logNotificationStatus() {
        try {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                Log.d(TAG, "=== NOTIFICATION DEBUG ===")
                Log.d(TAG, "Channel exists: ${channel != null}")
                Log.d(TAG, "Channel importance: ${channel?.importance}")
                Log.d(TAG, "Channel name: ${channel?.name}")
            }

            val areNotificationsEnabled =
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            Log.d(TAG, "Notifications enabled: $areNotificationsEnabled")
            Log.d(TAG, "=========================")

        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification status", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "💀 onDestroy() called - Service destroyed")
        Log.d(TAG, "   - Total starts: $startCount")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PLAY = "com.example.myapp.action.PLAY"
        const val ACTION_PAUSE = "com.example.myapp.action.PAUSE"
        const val ACTION_STOP = "com.example.myapp.action.STOP"
    }
}