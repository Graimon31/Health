package com.example.healthapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database storing all measurements captured from BLE or demo mode.
 */
@Database(
    entities = [MeasurementEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
}
