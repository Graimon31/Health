package com.example.myapp  // поправьте на ваш пакет

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class MiBandAdapter(private val context: Context) {
    // 1) Получаем BluetoothManager и Scanner
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner

    companion object {
        // UUID сервиса Mi Band (0xFEE0)
        val MI_BAND_SERVICE_UUID = ParcelUuid.fromString("0000FEE0-0000-1000-8000-00805F9B34FB")
        // UUID Heart Rate Service и Characteristic
        val HEART_RATE_SERVICE_UUID: UUID =
            UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID =
            UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }

    // 2) Сканирование Mi Band
    fun scanMiBandDevices(): Flow<BluetoothDevice> = callbackFlow {
        val filter = ScanFilter.Builder()
            .setServiceUuid(MI_BAND_SERVICE_UUID)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.device?.let { trySend(it) }
            }
            override fun onScanFailed(errorCode: Int) {
                close(RuntimeException("Scan failed: $errorCode"))
            }
        }

        scanner?.startScan(listOf(filter), settings, cb)
            ?: close(RuntimeException("Could not get scanner"))
        awaitClose { scanner?.stopScan(cb) }
    }

    // 3) Подписка на Heart Rate
    fun observeHeartRate(device: BluetoothDevice): Flow<Int> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val svc = gatt.getService(HEART_RATE_SERVICE_UUID) ?: return
                val chr = svc.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID) ?: return

                gatt.setCharacteristicNotification(chr, true)
                chr.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)?.apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(this)
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                    val flags = characteristic.getIntValue(
                        BluetoothGattCharacteristic.FORMAT_UINT8, 0
                    ) ?: return
                    val format = if (flags and 0x01 == 0)
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    else
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    val hr = characteristic.getIntValue(format, 1) ?: return
                    trySend(hr)
                }
            }
        }

        val gatt = device.connectGatt(context, false, gattCallback)
        awaitClose { gatt.close() }
    }
}