package com.otistran.demo_service.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otistran.demo_service.service.CalculatorService

@Composable
fun CalculatorServiceDemo() {
    val context = LocalContext.current
    var isBound by remember { mutableStateOf(false) }
    var calculatorService by remember { mutableStateOf<CalculatorService?>(null) }
    var result by remember { mutableStateOf("No calculation yet") }
    var firstNumber by remember { mutableIntStateOf(10) }
    var secondNumber by remember { mutableIntStateOf(5) }

    // ServiceConnection sẽ được sử dụng để liên kết với service
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as CalculatorService.CalculatorBinder
                calculatorService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                calculatorService = null
                isBound = false
            }
        }
    }

    // Tự động bind khi composable được tạo và unbind khi hủy
    DisposableEffect(context) {
        val intent = Intent(context, CalculatorService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Cleanup khi composable bị hủy
        onDispose {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bound Service Calculator Demo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Service status: ${if (isBound) "Connected" else "Disconnected"}")

        Spacer(modifier = Modifier.height(32.dp))

        // Trường nhập liệu cho các số
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextField(
                value = firstNumber.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { firstNumber = it }
                },
                label = { Text("First Number") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            TextField(
                value = secondNumber.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { secondNumber = it }
                },
                label = { Text("Second Number") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Các nút tính toán
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    calculatorService?.let {
                        result = "${firstNumber} + ${secondNumber} = ${
                            it.add(
                                firstNumber,
                                secondNumber
                            )
                        }"
                    } ?: run {
                        result = "Service not connected"
                    }
                },
                enabled = isBound
            ) {
                Text("+")
            }

            Button(
                onClick = {
                    calculatorService?.let {
                        result = "${firstNumber} - ${secondNumber} = ${
                            it.subtract(
                                firstNumber,
                                secondNumber
                            )
                        }"
                    } ?: run {
                        result = "Service not connected"
                    }
                },
                enabled = isBound
            ) {
                Text("-")
            }

            Button(
                onClick = {
                    calculatorService?.let {
                        result = "${firstNumber} × ${secondNumber} = ${
                            it.multiply(
                                firstNumber,
                                secondNumber
                            )
                        }"
                    } ?: run {
                        result = "Service not connected"
                    }
                },
                enabled = isBound
            ) {
                Text("×")
            }

            Button(
                onClick = {
                    calculatorService?.let {
                        try {
                            result = "${firstNumber} ÷ ${secondNumber} = ${
                                it.divide(
                                    firstNumber,
                                    secondNumber
                                )
                            }"
                        } catch (e: IllegalArgumentException) {
                            result = "Error: ${e.message}"
                        }
                    } ?: run {
                        result = "Service not connected"
                    }
                },
                enabled = isBound && secondNumber != 0
            ) {
                Text("÷")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Result: $result",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}