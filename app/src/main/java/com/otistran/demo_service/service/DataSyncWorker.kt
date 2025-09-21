package com.otistran.demo_service.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DataSyncWorker"

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting data sync...")
            performDataSync()
            Log.d(TAG, "Data sync completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Data sync failed", e)
            Result.failure()
        }
    }

    private suspend fun performDataSync() {
        for (i in 1..5) {
            Log.d(TAG, "Syncing data... $i/5")
            delay(1000)
        }
    }
}