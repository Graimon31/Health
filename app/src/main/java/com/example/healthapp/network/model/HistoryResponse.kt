package com.example.healthapp.network.model

import com.example.healthapp.model.Measurement
import java.time.Instant

/**
 * DTO returned by history endpoint.
 */
data class HistoryResponse(
    val measurements: List<HistoryItem>
) {
    data class HistoryItem(
        val userId: String,
        val timestamp: String,
        val heartRate: Int?,
        val bloodPressure: BloodPressure?
    ) {
        data class BloodPressure(
            val systolic: Int?,
            val diastolic: Int?
        )
    }

    fun toMeasurements(): List<Measurement> = measurements.map { item ->
        Measurement(
            userId = item.userId,
            timestamp = Instant.parse(item.timestamp),
            heartRate = item.heartRate,
            systolic = item.bloodPressure?.systolic,
            diastolic = item.bloodPressure?.diastolic,
            isSynced = true
        )
    }
}
