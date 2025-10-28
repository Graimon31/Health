package com.example.healthapp.network.model

import com.example.healthapp.model.Measurement
import java.time.format.DateTimeFormatter

/**
 * Payload used to upload local measurements to the backend API.
 */
data class MeasurementRequest(
    val userId: String,
    val timestamp: String,
    val heartRate: Int?,
    val bloodPressure: BloodPressure?
) {
    data class BloodPressure(
        val systolic: Int?,
        val diastolic: Int?
    )

    companion object {
        private val formatter = DateTimeFormatter.ISO_INSTANT

        fun fromMeasurement(measurement: Measurement): MeasurementRequest {
            return MeasurementRequest(
                userId = measurement.userId,
                timestamp = formatter.format(measurement.timestamp),
                heartRate = measurement.heartRate,
                bloodPressure = if (measurement.systolic != null || measurement.diastolic != null) {
                    BloodPressure(
                        systolic = measurement.systolic,
                        diastolic = measurement.diastolic
                    )
                } else {
                    null
                }
            )
        }
    }
}
