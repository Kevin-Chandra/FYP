package com.example.fyp.ordering_system.util

sealed class CartEvent {
    object Submit: CartEvent()
}