package com.example.fyp.pos.ui.navigation

sealed class PosScreen(val route: String) {

    object KitchenManageOrderItemScreen: PosScreen(route = "kitchen_manage_order_item_screen")
    object MainPosScreen: PosScreen(route = "main_pos_screen")
    object ManageOrderScreen: PosScreen(route = "pos_manage_order_screen")
    object ManageTableScreen: PosScreen(route = "manage_table_screen")

    fun withArgs(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}