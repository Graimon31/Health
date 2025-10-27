package com.example.healthapp.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.ble.BleManager
import com.example.healthapp.data.HealthRepository
import com.example.healthapp.data.prefs.UserPrefsDataStore
import com.example.healthapp.data.prefs.UserPreferences
import com.example.healthapp.model.Measurement
import java.time.Instant
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that orchestrates BLE discovery, connection and optional demo mode.
 */
class ScanViewModel(
    private val bleManager: BleManager,
    private val repository: HealthRepository,
    private val userPrefs: UserPrefsDataStore
) : ViewModel() {

    data class UiState(
        val devices: List<BleManager.Device> = emptyList(),
        val selectedDevice: BleManager.Device? = null,
        val connectionState: BleManager.ConnectionState = BleManager.ConnectionState.Disconnected,
        val latestMeasurement: Measurement? = null,
        val isDemoMode: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var scanJob: Job? = null
    private var measurementJob: Job? = null
    private var demoJob: Job? = null

    private val prefsFlow = MutableStateFlow<UserPreferences?>(null)

    init {
        viewModelScope.launch {
            userPrefs.data.collect { prefs ->
                prefsFlow.value = prefs
                _state.update { it.copy(isDemoMode = prefs.demoMode) }
            }
        }
    }

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update { it.copy(connectionState = BleManager.ConnectionState.Scanning, errorMessage = null) }
            try {
                val collected = kotlinx.coroutines.withTimeoutOrNull(15_000) {
                    bleManager.scanDevices().collect { devices ->
                        _state.update { it.copy(devices = devices) }
                    }
                }
                if (collected == null) {
                    _state.update { it.copy(connectionState = BleManager.ConnectionState.Disconnected) }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(connectionState = BleManager.ConnectionState.Error(t.message ?: "Ошибка"), errorMessage = t.message) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _state.update { current ->
            current.copy(connectionState = BleManager.ConnectionState.Disconnected)
        }
    }

    fun selectDevice(device: BleManager.Device) {
        _state.update { it.copy(selectedDevice = device, errorMessage = null) }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun startMeasurements() {
        val prefs = prefsFlow.value ?: return
        if (prefs.demoMode) {
            launchDemoMode(prefs)
        } else {
            val device = _state.value.selectedDevice
            if (device == null) {
                _state.update { it.copy(errorMessage = "Выберите устройство для подключения") }
            } else {
                connectToDevice(device, prefs)
            }
        }
    }

    private fun connectToDevice(device: BleManager.Device, prefs: UserPreferences) {
        measurementJob?.cancel()
        demoJob?.cancel()
        scanJob?.cancel()
        measurementJob = viewModelScope.launch {
            _state.update { it.copy(connectionState = BleManager.ConnectionState.Connecting(device), errorMessage = null) }
            try {
                bleManager.connect(device, prefs.userId).collect { sample ->
                    val measurement = Measurement(
                        userId = sample.userId,
                        timestamp = sample.timestamp,
                        heartRate = sample.heartRate,
                        systolic = sample.systolic,
                        diastolic = sample.diastolic,
                        isSynced = false
                    )
                    _state.update {
                        it.copy(
                            connectionState = BleManager.ConnectionState.Connected(device),
                            latestMeasurement = measurement
                        )
                    }
                    repository.saveMeasurement(measurement)
                }
            } catch (t: Throwable) {
                _state.update { it.copy(connectionState = BleManager.ConnectionState.Error(t.message ?: "Ошибка"), errorMessage = t.message) }
            }
        }
    }

    private fun launchDemoMode(prefs: UserPreferences) {
        demoJob?.cancel()
        measurementJob?.cancel()
        demoJob = viewModelScope.launch {
            while (true) {
                val measurement = Measurement(
                    userId = prefs.userId,
                    timestamp = Instant.now(),
                    heartRate = Random.nextInt(60, 95),
                    systolic = Random.nextInt(110, 130),
                    diastolic = Random.nextInt(70, 85),
                    isSynced = false
                )
                _state.update {
                    it.copy(
                        connectionState = BleManager.ConnectionState.Connected(
                            it.selectedDevice ?: BleManager.Device("Demo", "demo")
                        ),
                        latestMeasurement = measurement
                    )
                }
                repository.saveMeasurement(measurement)
                delay(5_000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        measurementJob?.cancel()
        demoJob?.cancel()
    }
}
