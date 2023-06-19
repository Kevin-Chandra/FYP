package com.example.fyp.menucreator.util

data class SetAvailabilityState(
    val foodAvailability: Boolean = true,
    val modifierItemAvailabilityMap : MutableMap<String,Boolean>? = null,
)