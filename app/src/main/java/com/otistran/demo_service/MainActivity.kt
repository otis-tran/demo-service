package com.otistran.demo_service

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.otistran.demo_service.screen.BackgroundServiceDemo
import com.otistran.demo_service.ui.theme.DemoserviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate() called with: savedInstanceState = $savedInstanceState")
        enableEdgeToEdge()
        setContent {
            DemoserviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Log.d("MainActivity", "onCreate() called with: innerPadding = $innerPadding")
                    BackgroundServiceDemo()
                }
            }
        }
    }
}
