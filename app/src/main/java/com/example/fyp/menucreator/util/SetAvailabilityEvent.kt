package com.example.fyp.menucreator.util

import com.example.fyp.account_management.data.model.Account

sealed class SetAvailabilityEvent {

    data class FoodAvailabilityChanged(val isFoodAvailable: Boolean) : SetAvailabilityEvent()

    data class ModifierItemAvailabilityChanged(val availabilityList: Pair<String,Boolean>) : SetAvailabilityEvent()

    data class Save(val account: Account): SetAvailabilityEvent()
}