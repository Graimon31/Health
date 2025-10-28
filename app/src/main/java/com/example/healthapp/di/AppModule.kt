package com.example.healthapp.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.healthapp.ble.BleManager
import com.example.healthapp.ble.BleParser
import com.example.healthapp.data.HealthRepository
import com.example.healthapp.data.db.AppDatabase
import com.example.healthapp.data.prefs.UserPrefsDataStore
import com.example.healthapp.network.RetrofitClient
import com.example.healthapp.ui.analytics.AnalyticsViewModel
import com.example.healthapp.ui.history.HistoryViewModel
import com.example.healthapp.ui.scan.ScanViewModel
import com.example.healthapp.ui.settings.SettingsViewModel

/**
 * A very small service locator used to keep the sample simple and free of DI frameworks.
 */
object AppModule {

    private lateinit var appContext: Context

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "health.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val parser: BleParser by lazy { BleParser() }

    private val bleManager: BleManager by lazy { BleManager(appContext, parser) }

    private val prefs: UserPrefsDataStore by lazy { UserPrefsDataStore(appContext) }

    private val retrofitClient: RetrofitClient by lazy { RetrofitClient() }

    private val repository: HealthRepository by lazy {
        HealthRepository(
            dao = database.measurementDao(),
            retrofitClient = retrofitClient,
            userPrefs = prefs
        )
    }

    fun init(application: Application) {
        appContext = application.applicationContext
    }

    fun repository(): HealthRepository = repository

    fun scanViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        ScanViewModel(bleManager = bleManager, repository = repository, userPrefs = prefs)
    }

    fun historyViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        HistoryViewModel(repository = repository)
    }

    fun analyticsViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        AnalyticsViewModel(repository = repository)
    }

    fun settingsViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        SettingsViewModel(userPrefs = prefs)
    }

    private fun <T : ViewModel> simpleFactory(creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T1 : ViewModel> create(modelClass: Class<T1>): T1 {
                @Suppress("UNCHECKED_CAST")
                return creator.invoke() as T1
            }
        }
    }
}
