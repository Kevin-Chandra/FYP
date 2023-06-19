package com.example.fyp.pos.ui.navigation

sealed class PosScreen(val route: String) {

    object KitchenManageOrderItemScreen: PosScreen(route = "kitchen_manage_order_item_screen")
    object MainPosScreen: PosScreen(route = "main_pos_screen")
    object ManageOrderScreen: PosScreen(route = "pos_manage_order_screen")
    object ManageTableScreen: PosScreen(route = "manage_table_screen")
    object TableSettingScreen: PosScreen(route = "table_setting_screen")
    object PosOrderScreen: PosScreen(route = "pos_order_screen")
    object OrderHistoryRootGraph: PosScreen(route = "order_history_root")
    object OrderHistoryScreen: PosScreen(route = "order_history_screen")
    object OrderHistoryDetailedScreen: PosScreen(route = "order_history_detailed_screen")
    object PosCheckoutScreen: PosScreen(route = "pos_checkout_screen")
    object PosInvoiceScreen: PosScreen(route = "pos_invoice_screen")
    object PosRootGraph: PosScreen(route = "pos_root_graph")
    object PosTableOrderGraph: PosScreen(route = "pos_table_order_screen")

    object PosOrderSummaryScreen : PosScreen(route = "pos_order_summary_screen")
    object PosAddToCartScreen : PosScreen(route = "pos_add_to_cart_screen")

    fun withRequiredArgs(vararg args: String): String{
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