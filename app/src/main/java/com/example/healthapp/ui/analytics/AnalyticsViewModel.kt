package com.example.healthapp.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.HealthRepository
import com.example.healthapp.util.calculateTrendSlope
import com.example.healthapp.util.trendMessageForSlope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel computing trend analytics over the last heart rate samples.
 */
class AnalyticsViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    data class UiState(
        val slope: Double = 0.0,
        val message: String = trendMessageForSlope(0.0),
        val heartRates: List<Int> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRecent(10).collectLatest { measurements ->
                val heartRates = measurements.mapNotNull { it.heartRate }.reversed()
                val slope = calculateTrendSlope(heartRates)
                _state.update {
                    it.copy(
                        slope = slope,
                        message = trendMessageForSlope(slope),
                        heartRates = heartRates
                    )
                }
            }
        }
    }
}
