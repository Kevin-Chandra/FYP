package com.example.fyp.ordering_system.ui.components

import com.example.fyp.R
import com.example.fyp.ordering_system.ui.navigation.Screen

sealed class CustomerBottomNavItem(var title:String, var icon:Int,var iconClicked: Int, var screen_route:String) {

    object OrderScreen : CustomerBottomNavItem("Order",R.drawable.ic_add_cart,R.drawable.ic_cart_filled, Screen.ProductListScreen.route)

    object OngoingScreen: CustomerBottomNavItem("Ongoing Order", R.drawable.ic_order_approve, R.drawable.ic_order_approve_filled,Screen.OngoingOrderListScreen.route)

    object OrderHistoryScreen: CustomerBottomNavItem("History", R.drawable.ic_history,R.drawable.ic_history,Screen.OrderHistoryScreen.route)
}