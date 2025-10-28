package com.example.healthapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthapp.model.Measurement
import java.time.Instant

/**
 * Room entity mirroring [Measurement].
 */
@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val timestamp: Long,
    val heartRate: Int?,
    val systolic: Int?,
    val diastolic: Int?,
    val isSynced: Boolean
)

fun MeasurementEntity.toModel(): Measurement = Measurement(
    id = id,
    userId = userId,
    timestamp = Instant.ofEpochMilli(timestamp),
    heartRate = heartRate,
    systolic = systolic,
    diastolic = diastolic,
    isSynced = isSynced
)

fun Measurement.toEntity(): MeasurementEntity = MeasurementEntity(
    id = id,
    userId = userId,
    timestamp = timestamp.toEpochMilli(),
    heartRate = heartRate,
    systolic = systolic,
    diastolic = diastolic,
    isSynced = isSynced
)
