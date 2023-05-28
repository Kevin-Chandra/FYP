package com.example.fyp.ordering_system.util

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.progressindicator.LinearProgressIndicatorSpec
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

fun errorToast(msg: String, context: Context){
    Toast.makeText(context,msg, Toast.LENGTH_LONG).show()
}

fun successToast(msg: String, context: Context){
    Toast.makeText(context,msg, Toast.LENGTH_SHORT).show()
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

fun formatTime(date: Date): String {
    val localDt = date
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return localDt.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
}

@Composable
fun LinearProgressAnimated(duration: Int,modifier: Modifier = Modifier,finished: (Boolean) -> Unit) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }
    val progressAnimation by animateFloatAsState(
        targetValue = if (animationPlayed) 0f else 1f,
        animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing, delayMillis = 800),
        finishedListener = {
            finished.invoke(true)
        }
    )
    LinearProgressIndicator(progress = progressAnimation, modifier = modifier.clip(
        RoundedCornerShape(10.dp)))
    LaunchedEffect(true) {
        animationPlayed = true
    }
}

@Preview(showBackground = true)
@Composable
private fun LpaPreview() {
    LinearProgressAnimated(duration = 10000, finished = {})
}