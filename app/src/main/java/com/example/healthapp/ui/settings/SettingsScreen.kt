package com.example.healthapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Settings screen allows configuring backend and demo mode.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, snackbarHostState: SnackbarHostState) {
    val state by viewModel.state.collectAsState()

    var userId by remember { mutableStateOf(state.preferences.userId) }
    var baseUrl by remember { mutableStateOf(state.preferences.baseUrl) }
    var token by remember { mutableStateOf(state.preferences.token.orEmpty()) }

    LaunchedEffect(state.preferences) {
        userId = state.preferences.userId
        baseUrl = state.preferences.baseUrl
        token = state.preferences.token.orEmpty()
    }

    LaunchedEffect(state.preferences.allowHttp) {
        if (state.preferences.allowHttp) {
            snackbarHostState.showSnackbar("Внимание: HTTP соединения небезопасны")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = userId,
            onValueChange = {
                userId = it
                viewModel.updateUserId(it)
            },
            label = { Text("User ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                viewModel.updateBaseUrl(it)
            },
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Например, https://api.example.com") }
        )

        OutlinedTextField(
            value = token,
            onValueChange = {
                token = it
                viewModel.updateToken(it)
            },
            label = { Text("Bearer Token") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Разрешить HTTP (только отладка)")
                Text(
                    text = "Отключите для защиты трафика. При включении HTTP лучше использовать только тестовый сервер.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = state.preferences.allowHttp,
                onCheckedChange = { viewModel.updateAllowHttp(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Demo Mode")
                Text(
                    text = "Генерировать синтетические данные каждые 5 секунд",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = state.preferences.demoMode,
                onCheckedChange = { viewModel.updateDemoMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Токен добавляется автоматически как заголовок Authorization",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
