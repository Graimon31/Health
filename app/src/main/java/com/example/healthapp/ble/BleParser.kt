package com.example.healthapp.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/**
 * Utility responsible for parsing BLE GATT characteristic payloads.
 */
class BleParser {

    fun parseHeartRate(payload: ByteArray): Int? {
        if (payload.isEmpty()) return null
        val flags = payload[0].toInt()
        val formatUInt16 = flags and 0x01 != 0
        return if (formatUInt16) {
            ByteBuffer.wrap(payload, 1, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else {
            payload[1].toInt() and 0xFF
        }
    }

    fun parseBloodPressure(payload: ByteArray): Pair<Int?, Int?> {
        if (payload.size < 7) return null to null
        val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
        buffer.get() // flags, currently unused
        val systolic = parseSFloat(buffer.short)
        val diastolic = parseSFloat(buffer.short)
        buffer.short // mean arterial pressure (unused for this sample)
        return systolic?.toInt() to diastolic?.toInt()
    }

    private fun parseSFloat(raw: Short): Float? {
        val intVal = raw.toInt() and 0xFFFF
        val mantissa = intVal and 0x0FFF
        val exponent = intVal shr 12
        val signedMantissa = if (mantissa >= 0x0800) mantissa - 0x1000 else mantissa
        val signedExponent = if (exponent >= 0x0008) exponent - 0x0010 else exponent
        return signedMantissa * Math.pow(10.0, signedExponent.toDouble()).toFloat()
    }

    companion object {
        val SERVICE_HEART_RATE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val SERVICE_BLOOD_PRESSURE: UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_HEART_RATE: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_BLOOD_PRESSURE: UUID = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")
    }
}
