package com.factoryflow.app.core.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val french = Locale.FRANCE
private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(french)
private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(french)

fun String.toFrenchDate(): String = runCatching { LocalDate.parse(this).format(dateFormatter) }.getOrDefault(this)
fun String.toFrenchInstant(): String = runCatching { Instant.parse(this).atZone(java.time.ZoneId.of("Africa/Casablanca")).format(dateTimeFormatter) }
    .recoverCatching { ZonedDateTime.parse(this).format(dateTimeFormatter) }.getOrDefault(this)

fun BigDecimal?.displayValue(): String = this?.let {
    DecimalFormat("#,##0.###", DecimalFormatSymbols(french)).format(it)
} ?: "—"

fun String.asEditableDecimal(): BigDecimal? = trim().replace(',', '.').takeIf(String::isNotBlank)?.toBigDecimalOrNull()
