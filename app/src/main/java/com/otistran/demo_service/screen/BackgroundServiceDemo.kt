package com.otistran.demo_service.screen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.otistran.demo_service.service.DataSyncService

@Composable
fun BackgroundServiceDemo() {
    val context = LocalContext.current
    var syncStatus by remember { mutableStateOf("Idle") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Background Service Demo")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Status: $syncStatus")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Log.d("BackgroundServiceDemo", "BackgroundServiceDemo click start")
                syncStatus = "Syncing data..."
                startDataSyncService(context)
            }
        ) {
            Text("Start Data Sync")
        }
    }
}

private fun startDataSyncService(context: Context) {
    val intent = Intent(context, DataSyncService::class.java)
    context.startService(intent)
}