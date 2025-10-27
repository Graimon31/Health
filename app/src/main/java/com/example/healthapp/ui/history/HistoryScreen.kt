package com.example.healthapp.ui.history

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.healthapp.model.Measurement
import com.example.healthapp.util.toDisplayText
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

/**
 * History screen shows local measurements and renders a heart rate chart.
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, snackbarHostState: SnackbarHostState) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "История измерений",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        PeriodSelector(state.period, onSelect = viewModel::onPeriodSelected)

        Button(onClick = viewModel::refreshFromRemote) {
            Text("Обновить с сервера")
        }

        HeartRateChart(measurements = state.measurements)

        MeasurementList(
            measurements = state.measurements,
            modifier = Modifier.weight(1f, fill = true)
        )
    }
}

@Composable
private fun PeriodSelector(selected: HistoryViewModel.Period, onSelect: (HistoryViewModel.Period) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryViewModel.Period.values().forEach { period ->
            AssistChip(
                onClick = { onSelect(period) },
                label = { Text(period.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (period == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (period == selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun HeartRateChart(measurements: List<Measurement>) {
    val entries = measurements
        .filter { it.heartRate != null }
        .sortedBy { it.timestamp }
        .mapIndexed { index, measurement -> Entry(index.toFloat(), measurement.heartRate!!.toFloat()) }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                setTouchEnabled(false)
                setScaleEnabled(false)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Пульс").apply {
                color = Color.RED
                valueTextColor = Color.DKGRAY
                lineWidth = 2f
                setDrawCircles(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
private fun MeasurementList(measurements: List<Measurement>, modifier: Modifier = Modifier) {
    if (measurements.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = "Данных пока нет",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(measurements) { measurement ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(measurement.timestamp.toDisplayText(), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Пульс: ${measurement.heartRate ?: "—"} уд/мин")
                    Text("Давление: ${measurement.systolic ?: "—"}/${measurement.diastolic ?: "—"}")
                }
            }
        }
    }
}
