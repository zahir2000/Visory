package com.taruc.visory.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.Date

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

fun getCurrentFormattedDateTime(): String {
    val date = getCurrentDateTime()
    return date.toString("dd MMM yyyy HH:mm")
}

fun getCurrentDate(): String {
    val date = getCurrentDateTime()
    return date.toString("dd MMM yyyy")
}