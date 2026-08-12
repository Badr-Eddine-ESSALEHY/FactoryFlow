package com.factoryflow.app.core.util

private val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

fun String.isValidEmail(): Boolean = emailPattern.matches(trim())
