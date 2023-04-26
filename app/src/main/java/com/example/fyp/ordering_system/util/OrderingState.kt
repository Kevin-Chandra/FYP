package com.example.fyp.ordering_system.util

import android.net.Uri
import java.util.*

data class OrderingState(
    val foodId: String = "",
    val modifierList : Map<String,List<String>> = emptyMap(),
    val quantity : Int  = 0
)