package com.example.healthapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthapp.di.AppModule

/**
 * Periodically uploads unsynchronised measurements to the backend.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = AppModule.repository()
        return try {
            val pending = repository.getPendingMeasurements()
            if (pending.isEmpty()) {
                return Result.success()
            }
            val prefs = repository.preferences()
            pending.forEach { measurement ->
                try {
                    val success = repository.syncMeasurement(measurement, prefs)
                    if (!success) {
                        return Result.retry()
                    }
                } catch (t: Throwable) {
                    return Result.retry()
                }
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "measurement_sync"
    }
}
