package com.example.fyp.ordering_system.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable

data class TabItem (
    val title: String,
    @DrawableRes val icon:Int,
    val screen: @Composable () -> Unit
)