package com.example.fyp.pos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.screen.CheckoutScreen
import com.example.fyp.pos.ui.screen.FinishedOrderDetailedScreen
import com.example.fyp.pos.ui.screen.FinishedOrderScreen
import com.example.fyp.pos.ui.screen.InvoiceScreen
import com.example.fyp.pos.ui.screen.MainPosScreen
import com.example.fyp.pos.ui.screen.KitchenManageOrderScreen
import com.example.fyp.pos.ui.screen.ManageOrderScreen
import com.example.fyp.pos.ui.screen.ManageTableScreen
import com.example.fyp.pos.ui.screen.PosAddToCartScreen
import com.example.fyp.pos.ui.screen.PosOrderScreen
import com.example.fyp.pos.ui.screen.PosOrderSummary
import com.example.fyp.pos.ui.screen.TableSettingScreen
import com.example.fyp.pos.ui.viewmodel.CheckoutViewModel
import com.example.fyp.pos.ui.viewmodel.IncomingOrderItemViewModel
import com.example.fyp.pos.ui.viewmodel.ManageOrderViewModel
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import com.example.fyp.pos.ui.viewmodel.PastOrderViewModel
import com.example.fyp.pos.ui.viewmodel.TableOngoingOrderViewModel
import com.example.fyp.pos.ui.viewmodel.TableOrderCartViewModel

@Composable
fun PosNavGraph(
    navController : NavHostController,
    productViewModel: ProductViewModel,
    incomingOrderItemViewModel: IncomingOrderItemViewModel,
    manageOrderViewModel: ManageOrderViewModel,
    manageTableViewModel: ManageTableViewModel,
    tableOngoingOrderViewModel: TableOngoingOrderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = PosScreen.MainPosScreen.route,
        route = PosScreen.PosRootGraph.route
    ){
        composable(
            route = PosScreen.MainPosScreen.route
        ){
            MainPosScreen(navigator = navController)
        }
        composable(
            route = PosScreen.KitchenManageOrderItemScreen.route
        ){
            KitchenManageOrderScreen(
                navigator = navController,
                viewModel = incomingOrderItemViewModel,
                productViewModel = productViewModel
            )
        }
        composable(
            route = PosScreen.ManageOrderScreen.route
        ){
            ManageOrderScreen(productViewModel = productViewModel, manageOrderViewModel = manageOrderViewModel)
        }
        composable(
            route = PosScreen.ManageTableScreen.route
        ){
            ManageTableScreen(
                navigator = navController,
                viewModel = manageTableViewModel,
                tableOngoingOrderViewModel = tableOngoingOrderViewModel,
                productViewModel = productViewModel
            )
        }
        composable(
            route = PosScreen.TableSettingScreen.route
        ){
            TableSettingScreen(navController,manageTableViewModel)
        }

        composable(
            route = PosScreen.PosInvoiceScreen.route +  "/{orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                    nullable = false
                },
            )
        ){
            val orderId = it.arguments?.getString("orderId") ?: ""
            InvoiceScreen(
                navigator = navController,
                orderId = orderId,
                productViewModel = productViewModel,
                tableOngoingOrderViewModel =  tableOngoingOrderViewModel
            )
        }

        composable(
            route = PosScreen.PosCheckoutScreen.route + "/{orderId}/{tableId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("tableId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
            )
        ){
            val orderId = it.arguments?.getString("orderId")
            val tableId = it.arguments?.getString("tableId")
            CheckoutScreen(
                navigator = navController,
                productViewModel = productViewModel,
                tableOngoingOrderViewModel = tableOngoingOrderViewModel,
                tableViewModel  = manageTableViewModel,
                orderId = orderId ?: "",
                tableId = tableId ?: "",
            )
        }

        // order history nav graph
        navigation(
            startDestination = PosScreen.OrderHistoryScreen.route,
            route = PosScreen.OrderHistoryRootGraph.route,
        ){
            composable(
                route = PosScreen.OrderHistoryScreen.route
            ){
                val orderHistoryViewModel = it.sharedViewModel<PastOrderViewModel>(navController)
                FinishedOrderScreen(navigator = navController, pastOrderViewModel = orderHistoryViewModel)
            }

            composable(
                route = PosScreen.OrderHistoryDetailedScreen.route + "/{orderId}"
            ){
                val orderHistoryViewModel = it.sharedViewModel<PastOrderViewModel>(navController)
                val orderId = it.arguments?.getString("orderId")
                FinishedOrderDetailedScreen(
                    navigator = navController,
                    pastOrderViewModel = orderHistoryViewModel,
                    productViewModel = productViewModel,
                    orderId = orderId
                )
            }
        }

        //Add/view order nested nav graph
        navigation(
            startDestination = PosScreen.PosOrderScreen.route,
            route = PosScreen.PosTableOrderGraph.route + "?tableId={tableId}",
            arguments = listOf(
                navArgument("tableId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            composable(
                route = PosScreen.PosOrderScreen.route
            ) { entry ->
                val viewModel = entry.sharedViewModel<TableOrderCartViewModel>(navController)
                val parentEntry = remember(entry) { navController.getBackStackEntry(PosScreen.PosTableOrderGraph.route + "?tableId={tableId}") }
                val tableId = parentEntry.arguments?.getString("tableId") ?: ""
                viewModel.initTable(manageTableViewModel.getTable(tableId)!!)
                PosOrderScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = viewModel,
                    tableId = tableId,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )

            }
            composable(
                route = PosScreen.PosOrderSummaryScreen.route
            ){
                val viewModel = it.sharedViewModel<TableOrderCartViewModel>(navController)
                PosOrderSummary(navigator = navController, productViewModel = productViewModel, tableOrderViewModel = viewModel)
            }

            composable(
                route = PosScreen.PosAddToCartScreen.route + "/{foodId}/{quantity}?orderItemId={orderItemId}",
                arguments = listOf(
                    navArgument("foodId"){
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument("orderItemId"){
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("quantity"){
                        type = NavType.IntType
                        defaultValue = 1
                    },
                )
            ) { entry ->
                val foodId = entry.arguments?.getString("foodId") ?: ""
                val quantity = entry.arguments?.getInt("quantity",1) ?: 1
                val orderItemId = entry.arguments?.getString("orderItemId")
                PosAddToCartScreen(
                    navigator = navController,
                    productViewModel = productViewModel,
                    foodId = foodId,
                    orderItemId = orderItemId,
                    quantity = quantity
                )
            }
        }

    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}