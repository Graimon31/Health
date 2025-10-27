package com.example.healthapp.data

import com.example.healthapp.data.db.MeasurementDao
import com.example.healthapp.data.db.toEntity
import com.example.healthapp.data.db.toModel
import com.example.healthapp.data.prefs.UserPrefsDataStore
import com.example.healthapp.data.prefs.UserPreferences
import com.example.healthapp.model.Measurement
import com.example.healthapp.network.RetrofitClient
import com.example.healthapp.network.model.MeasurementRequest
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository encapsulating data access logic between Room, Retrofit and DataStore.
 */
class HealthRepository(
    private val dao: MeasurementDao,
    private val retrofitClient: RetrofitClient,
    private val userPrefs: UserPrefsDataStore
) {

    val preferencesFlow: Flow<UserPreferences> = userPrefs.data

    fun observeMeasurements(from: Instant, to: Instant = Instant.now()): Flow<List<Measurement>> {
        return dao.observeByPeriod(from.toEpochMilli(), to.toEpochMilli())
            .map { list -> list.map { it.toModel() } }
    }

    fun observeRecent(limit: Int): Flow<List<Measurement>> {
        return dao.observeLatest(limit).map { list -> list.map { it.toModel() } }
    }

    fun observePeriodDays(days: Long): Flow<List<Measurement>> {
        val to = Instant.now()
        val from = to.minus(days, ChronoUnit.DAYS)
        return observeMeasurements(from, to)
    }

    suspend fun saveMeasurement(measurement: Measurement): Measurement {
        val id = dao.insert(measurement.toEntity())
        return measurement.copy(id = id)
    }

    suspend fun getPendingMeasurements(): List<Measurement> = dao.pending().map { it.toModel() }

    suspend fun markSynced(ids: List<Long>) {
        if (ids.isNotEmpty()) {
            dao.markSynced(ids, true)
        }
    }

    suspend fun syncMeasurement(measurement: Measurement, prefs: UserPreferences): Boolean {
        val api = retrofitClient.create(prefs)
        val response = api.postMeasurement(MeasurementRequest.fromMeasurement(measurement))
        val success = response.isSuccessful
        if (success && measurement.id != 0L) {
            markSynced(listOf(measurement.id))
        }
        return success
    }

    suspend fun refreshHistory(days: Int): List<Measurement> {
        val prefs = userPrefs.current()
        val api = retrofitClient.create(prefs)
        val remote = api.getHistory(prefs.userId, days).toMeasurements()
        remote.forEach { measurement ->
            // Persist remote history locally to keep graphs in sync.
            dao.insert(measurement.copy(isSynced = true).toEntity())
        }
        return remote
    }

    suspend fun preferences(): UserPreferences = preferencesFlow.first()
}
