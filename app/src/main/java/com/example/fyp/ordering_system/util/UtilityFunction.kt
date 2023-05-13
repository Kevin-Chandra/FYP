package com.example.fyp.ordering_system.util

import android.content.Context
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

fun errorToast(msg: String, context: Context){
    Toast.makeText(context,msg, Toast.LENGTH_LONG).show()
}

fun formatDate(date: Date): String {
    val localDt = date
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val today = LocalDate.now().atStartOfDay()
    return if (localDt in today..today.plusDays(1))
        localDt.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
    else
        localDt.format(DateTimeFormatter.ofPattern("d MMM hh:mm:ss a"))

}