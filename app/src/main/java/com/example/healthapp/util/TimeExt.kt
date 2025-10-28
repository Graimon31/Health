package com.example.healthapp.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm")
    .withZone(ZoneId.systemDefault())

/** Formats Instant to a short, human readable string for cards. */
fun Instant.toDisplayText(): String = formatter.format(this)
