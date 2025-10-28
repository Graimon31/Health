package com.example.healthapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.HealthRepository
import com.example.healthapp.model.Measurement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel powering the history screen.
 */
class HistoryViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    enum class Period(val days: Long, val label: String) {
        DAY(1, "24 часа"),
        WEEK(7, "7 дней"),
        MONTH(30, "30 дней")
    }

    data class UiState(
        val period: Period = Period.DAY,
        val measurements: List<Measurement> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeCurrentPeriod()
    }

    fun onPeriodSelected(period: Period) {
        if (period == _state.value.period) return
        _state.update { it.copy(period = period) }
        observeCurrentPeriod()
    }

    private fun observeCurrentPeriod() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observePeriodDays(_state.value.period.days).collectLatest { list ->
                _state.update { it.copy(measurements = list) }
            }
        }
    }

    fun refreshFromRemote() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refreshHistory(_state.value.period.days.toInt())
            } catch (t: Throwable) {
                _state.update { it.copy(errorMessage = t.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
