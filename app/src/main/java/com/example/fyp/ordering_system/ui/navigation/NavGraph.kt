package com.example.fyp.ordering_system.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.ui.screen.AddToCartScreen
import com.example.fyp.ordering_system.ui.screen.OngoingOrderListScreen
import com.example.fyp.ordering_system.ui.screen.OngoingOrderScreen
import com.example.fyp.ordering_system.ui.screen.OrderHistoryScreen
import com.example.fyp.ordering_system.ui.screen.ReviewOrderScreen
import com.example.fyp.ordering_system.ui.screen.ViewProductListScreen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel

@Composable
fun SetupOnlineOrderingNavGraph(
    navController : NavHostController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    ongoingOrderViewModel: OngoingOrderViewModel,
    account: Account
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProductListScreen.route){
        composable(
            route = Screen.ProductListScreen.route
        ){
            ViewProductListScreen(navController,productViewModel,cartViewModel)
        }
        composable(
            route = Screen.AddToCartScreen.route + "/{id}/{orderItemId}",
            arguments = listOf(
                navArgument("id"){
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("orderItemId"){
                    type = NavType.StringType
                    defaultValue = "null"
                },
            )
        ) { entry ->
            val foodId = entry.arguments?.getString("id") ?: ""
            val orderItemId = if (entry.arguments?.getString("orderItemId") == "null") null else entry.arguments?.getString("orderItemId")
            AddToCartScreen(
                navigator = navController,
                productViewModel = productViewModel,
                foodId = foodId,
                orderItemId = orderItemId
            )
        }
        composable(
            route = Screen.ReviewOrderScreen.route
        ){
            ReviewOrderScreen(navigator = navController, cartViewModel = cartViewModel, productViewModel = productViewModel,account = account)
        }
        composable(
            route = Screen.OngoingOrderScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id"){
                    type = NavType.StringType
                    nullable = false
                }
            )
        ){ entry ->
            val id = entry.arguments?.getString("id") ?: ""
            OngoingOrderScreen(navigator = navController, id = id, viewModel = ongoingOrderViewModel)
        }
        composable(
            route = Screen.OngoingOrderListScreen.route
        ){
            OngoingOrderListScreen(navController,ongoingOrderViewModel, account)
        }
        composable(
            route = Screen.OrderHistoryScreen.route,
        ){
            OrderHistoryScreen(navigator = navController, accountId = account.id, viewModel = ongoingOrderViewModel)
        }
    }
}