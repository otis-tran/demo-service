package com.otistran.demo_service.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class CalculatorService : Service() {

    private val TAG = "CalculatorService"

    // Binder cung cấp instance của service cho client
    private val binder = CalculatorBinder()

    inner class CalculatorBinder : Binder() {
        fun getService(): CalculatorService = this@CalculatorService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Calculator service created")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Client bound to service")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Client unbound from service")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Calculator service destroyed")
    }

    // API công khai mà các clients có thể gọi
    fun add(a: Int, b: Int): Int {
        Log.d(TAG, "Performing addition: $a + $b")
        return a + b
    }

    fun subtract(a: Int, b: Int): Int {
        Log.d(TAG, "Performing subtraction: $a - $b")
        return a - b
    }

    fun multiply(a: Int, b: Int): Int {
        Log.d(TAG, "Performing multiplication: $a * $b")
        return a * b
    }

    fun divide(a: Int, b: Int): Double {
        if (b == 0) throw IllegalArgumentException("Cannot divide by zero")
        Log.d(TAG, "Performing division: $a / $b")
        return a.toDouble() / b
    }
}
