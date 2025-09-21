package com.otistran.demo_service.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DataSyncService : Service() {

    private val TAG = "DataSyncService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Thực hiện tác vụ đồng bộ trong coroutine (không block main thread)
        serviceScope.launch {
            try {
                performDataSync()
                // Tự dừng Service sau khi hoàn thành
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing data", e)
                stopSelf()
            }
        }

        // Nếu hệ thống kill service, KHÔNG khởi động lại
        return START_NOT_STICKY
    }

    private suspend fun performDataSync() {
        // Giả lập đồng bộ dữ liệu
        for (i in 1..3) {
            Log.d(TAG, "Syncing data... $i/3")
            delay(1000) // Giả lập độ trễ mạng
        }
        Log.d(TAG, "Data sync completed")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy tất cả coroutine khi service bị hủy
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null // Không hỗ trợ binding
}
