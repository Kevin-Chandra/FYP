package com.example.fyp.ordering_system.ui.navigation

sealed class Screen(val route: String){
    object OngoingOrderListScreen: Screen(route = "ongoing_order_list_screen")
    object OngoingOrderScreen: Screen(route = "ongoing_order_screen")
    object ProductListScreen: Screen(route = "product_list_screen")
    object AddToCartScreen: Screen(route = "add_to_cart_screen")
    object ReviewOrderScreen: Screen(route = "review_order_screen")
    object MainOnlineOrderingScreen: Screen(route = "main_order_screen")
    object ManageOrderScreen: Screen(route = "incoming_order_screen")
    object OrderHistoryScreen: Screen(route = "order_history_screen")
    object OrderHistoryDetailedScreen: Screen(route = "order_history_detailed_screen")
    fun withArgs(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    fun withOptionalArg(vararg args: Pair<String,String>): String{
        return buildString {
            append(route)
            args.forEach { pair->
                append("?${pair.first}=${pair.second}")
            }
        }
    }
}
