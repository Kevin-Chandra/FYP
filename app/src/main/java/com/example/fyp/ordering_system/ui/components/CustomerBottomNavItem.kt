package com.example.fyp.ordering_system.ui.components

import com.example.fyp.R
import com.example.fyp.ordering_system.ui.navigation.Screen

sealed class CustomerBottomNavItem(var title:String, var icon:Int, var screen_route:String) {

    object OrderScreen : CustomerBottomNavItem("Order",R.drawable.ic_add_cart,Screen.ProductListScreen.route)

    object OngoingScreen: CustomerBottomNavItem("Ongoing Order", R.drawable.ic_order_approve,Screen.OngoingOrderListScreen.route)

    object OrderHistoryScreen: CustomerBottomNavItem("History", R.drawable.ic_history,Screen.OrderHistoryScreen.route)
}