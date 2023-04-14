package com.example.fyp.menucreator.util

import android.net.Uri
import java.util.*

data class SetAvailabilityState(
    val foodAvailability: Boolean = true,
    val modifierItemAvailabilityMap : MutableMap<String,Boolean>? = null,
)