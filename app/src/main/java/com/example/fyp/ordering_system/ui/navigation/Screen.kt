package com.example.fyp.ordering_system.ui.navigation

sealed class Screen(val route: String){
    object ProductListScreen: Screen(route = "product_list_screen")
    object AddToCartScreen: Screen(route = "add_to_cart_screen")
    object ReviewOrderScreen: Screen(route = "review_order_screen")
    object MainOnlineOrderingScreen: Screen(route = "main_order_screen")
    object ViewOrderScreen: Screen(route = "view_order_screen")
    object ManageOrderScreen: Screen(route = "incoming_order_screen")
//    object OngoingOrderScreen: Screen(route = "ongoing_order_screen")

    fun withArgs(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
