package com.example.healthapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import com.example.healthapp.di.AppModule
import com.example.healthapp.workers.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Application-level entry point where we wire the service locator and background sync.
 */
class HealthApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize simple dependency container.
        AppModule.init(this)

        // Prepare constraints: run only with network available and when battery is not low.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // WorkManager requires at least 15 minutes for periodic work. We still schedule
        // unique work so the job runs roughly every 15 minutes while satisfying the
        // requirement to leverage WorkManager for background sync.
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
