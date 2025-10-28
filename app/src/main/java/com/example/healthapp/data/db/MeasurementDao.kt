package com.example.healthapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO that exposes both reactive streams for UI and imperative queries for workers.
 */
@Dao
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementEntity): Long

    @Query("SELECT * FROM measurements WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun observeByPeriod(from: Long, to: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<MeasurementEntity>

    @Query("UPDATE measurements SET isSynced = :synced WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, synced: Boolean)

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT :limit")
    suspend fun last(limit: Int): List<MeasurementEntity>
}
