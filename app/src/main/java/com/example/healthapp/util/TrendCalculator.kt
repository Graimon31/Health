package com.example.healthapp.util

/**
 * Calculates slope of a linear regression line for the provided values.
 */
fun calculateTrendSlope(values: List<Int>): Double {
    if (values.size < 2) return 0.0
    val n = values.size.toDouble()
    val sumX = values.indices.sum().toDouble()
    val sumY = values.sum().toDouble()
    val sumXY = values.indices.zip(values).sumOf { (x, y) -> x * y }.toDouble()
    val sumX2 = values.indices.sumOf { (it * it) }.toDouble()

    val numerator = n * sumXY - sumX * sumY
    val denominator = n * sumX2 - sumX * sumX
    if (denominator == 0.0) return 0.0
    return numerator / denominator
}

fun trendMessageForSlope(slope: Double): String {
    return if (slope > 0.5) {
        "Пульс растёт — возможная нагрузка или стресс"
    } else {
        "Тренд в норме"
    }
}
