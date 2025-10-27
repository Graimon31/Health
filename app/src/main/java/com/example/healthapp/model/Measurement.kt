package com.example.healthapp.model

import java.time.Instant

/**
 * Domain model used across the app to represent a health measurement.
 */
data class Measurement(
    val id: Long = 0L,
    val userId: String,
    val timestamp: Instant,
    val heartRate: Int?,
    val systolic: Int?,
    val diastolic: Int?,
    val isSynced: Boolean = false
)
