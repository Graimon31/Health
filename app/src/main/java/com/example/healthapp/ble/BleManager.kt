package com.example.healthapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.UUID

/**
 * Handles BLE scanning, connection and streaming of measurement payloads.
 */
class BleManager(
    context: Context,
    private val parser: BleParser
) {

    private val appContext = context.applicationContext

    data class Device(val name: String?, val address: String)

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Scanning : ConnectionState()
        data class Connecting(val device: Device) : ConnectionState()
        data class Connected(val device: Device) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private val connectionMutex = Mutex()

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun scanDevices(): Flow<List<Device>> = callbackFlow {
        val results = LinkedHashMap<String, Device>()
        val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("Bluetooth scanner unavailable"))
            return@callbackFlow
        }

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(BleParser.SERVICE_HEART_RATE)).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid(BleParser.SERVICE_BLOOD_PRESSURE)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val item = Device(device.name ?: "Неизвестно", device.address)
                results[device.address] = item
                trySend(results.values.toList())
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("Scan failed: $errorCode"))
            }
        }

        scanner.startScan(filters, settings, callback)
        awaitClose { scanner.stopScan(callback) }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: Device, userId: String): Flow<BleSample> = callbackFlow {
        val bluetoothDevice = adapter?.getRemoteDevice(device.address)
            ?: return@callbackFlow close(IllegalStateException("Device not found"))

        val connection = object : BluetoothGattCallback() {
            private var lastHeartRate: Int? = null
            private var lastSystolic: Int? = null
            private var lastDiastolic: Int? = null

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close(IllegalStateException("Gatt error $status"))
                    return
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close()
                    close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close(IllegalStateException("Service discovery failed: $status"))
                    return
                }
                enableNotifications(gatt, BleParser.CHARACTERISTIC_HEART_RATE)
                enableNotifications(gatt, BleParser.CHARACTERISTIC_BLOOD_PRESSURE)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                val value = characteristic.value ?: return
                when (characteristic.uuid) {
                    BleParser.CHARACTERISTIC_HEART_RATE -> {
                        lastHeartRate = parser.parseHeartRate(value)
                    }
                    BleParser.CHARACTERISTIC_BLOOD_PRESSURE -> {
                        val (sys, dia) = parser.parseBloodPressure(value)
                        if (sys != null) lastSystolic = sys
                        if (dia != null) lastDiastolic = dia
                    }
                }
                trySend(
                    BleSample(
                        userId = userId,
                        timestamp = Instant.now(),
                        heartRate = lastHeartRate,
                        systolic = lastSystolic,
                        diastolic = lastDiastolic
                    )
                )
            }

            private fun enableNotifications(gatt: BluetoothGatt, uuid: UUID) {
                val serviceUuid = when (uuid) {
                    BleParser.CHARACTERISTIC_HEART_RATE -> BleParser.SERVICE_HEART_RATE
                    BleParser.CHARACTERISTIC_BLOOD_PRESSURE -> BleParser.SERVICE_BLOOD_PRESSURE
                    else -> return
                }
                val service: BluetoothGattService = gatt.getService(serviceUuid) ?: return
                val characteristic = service.getCharacteristic(uuid) ?: return
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_UUID)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }
            }
        }

        val gatt = connectionMutex.withLock {
            bluetoothDevice.connectGatt(appContext, false, connection)
        }

        awaitClose {
            gatt?.close()
        }
    }

    data class BleSample(
        val userId: String,
        val timestamp: Instant,
        val heartRate: Int?,
        val systolic: Int?,
        val diastolic: Int?
    )

    companion object {
        private val CLIENT_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
