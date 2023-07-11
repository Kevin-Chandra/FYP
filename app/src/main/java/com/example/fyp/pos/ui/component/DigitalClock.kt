package com.example.fyp.pos.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fyp.ordering_system.util.formatTime
import com.example.fyp.ordering_system.util.pastTime
import kotlinx.coroutines.delay

@Composable
fun ClockText() {
    val currentTimeMillis = remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(key1 = currentTimeMillis) {
        while (true) {
            delay(500)
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    Box {
        Text(
            text = formatTime(currentTimeMillis.value),
            modifier = Modifier.padding(8.dp, 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimePassed(
    time: Long,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal
){
    val currentTimeMillis = remember {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(key1 = currentTimeMillis) {
        while (true) {
            delay(500)
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }
    Text(
        text = pastTime(time, currentTimeMillis.value),
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        modifier = modifier
    )
}