package com.example.healthapp.ui.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthapp.ble.BleManager
import com.example.healthapp.util.Permissions
import com.example.healthapp.util.toDisplayText

/**
 * UI for scanning BLE devices and displaying live measurement stream.
 */
@Composable
fun ScanScreen(viewModel: ScanViewModel, snackbarHostState: SnackbarHostState) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val allGranted = granted.values.all { it }
        if (allGranted) {
            viewModel.startScan()
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "BLE монитор",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = {
                if (Permissions.hasPermissions(context)) {
                    viewModel.startScan()
                } else {
                    permissionLauncher.launch(Permissions.requiredPermissions())
                }
            }) {
                Text("Сканировать")
            }
            Button(onClick = viewModel::startMeasurements) {
                Text("Старт измерений")
            }
        }

        Text(
            text = when (val status = state.connectionState) {
                BleManager.ConnectionState.Disconnected -> "Отключено"
                BleManager.ConnectionState.Scanning -> "Идёт сканирование..."
                is BleManager.ConnectionState.Connecting -> "Подключаемся к ${status.device.name ?: status.device.address}"
                is BleManager.ConnectionState.Connected -> "Подключено к ${status.device.name ?: status.device.address}"
                is BleManager.ConnectionState.Error -> "Ошибка: ${status.message}"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (state.isDemoMode) {
            Text(
                text = "Demo режим активен: данные генерируются локально",
                color = MaterialTheme.colorScheme.primary
            )
        }

        DeviceList(
            devices = state.devices,
            selected = state.selectedDevice,
            onSelect = viewModel::selectDevice
        )

        state.latestMeasurement?.let { measurement ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Последнее обновление: ${measurement.timestamp.toDisplayText()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Пульс: ${measurement.heartRate ?: "—"} уд/мин")
                    Text("Давление: ${measurement.systolic ?: "—"}/${measurement.diastolic ?: "—"} мм рт. ст.")
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<BleManager.Device>,
    selected: BleManager.Device?,
    onSelect: (BleManager.Device) -> Unit
) {
    if (devices.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = "Нажмите \"Сканировать\", чтобы найти доступные датчики",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        items(devices) { device ->
            val isSelected = device == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onSelect(device) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(device.name ?: "Неизвестно", fontWeight = FontWeight.Medium)
                    Text(device.address, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
