package com.example.fyp.ordering_system.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.ui.screen.AddToCartScreen
import com.example.fyp.ordering_system.ui.screen.OngoingOrderListScreen
import com.example.fyp.ordering_system.ui.screen.OngoingOrderScreen
import com.example.fyp.ordering_system.ui.screen.OrderHistoryDetailedScreen
import com.example.fyp.ordering_system.ui.screen.OrderHistoryScreen
import com.example.fyp.ordering_system.ui.screen.ReviewOrderScreen
import com.example.fyp.ordering_system.ui.screen.ViewProductListScreen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalComposeUiApi::class)
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
        startDestination = Screen.ProductListScreen.route,
        modifier = Modifier.semantics {
            testTagsAsResourceId = true
        }
    ){
        composable(
            route = Screen.ProductListScreen.route
        ){
            ViewProductListScreen(navController,productViewModel,cartViewModel)
        }
        composable(
            route = Screen.AddToCartScreen.route + "/{foodId}/{quantity}?orderItemId={orderItemId}",
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
                }
            )
        ) { entry ->
            val foodId = entry.arguments?.getString("foodId") ?: ""
            val quantity = entry.arguments?.getInt("quantity",1) ?: 1
            val orderItemId = entry.arguments?.getString("orderItemId")
            AddToCartScreen(
                navigator = navController,
                productViewModel = productViewModel,
                foodId = foodId,
                orderItemId = orderItemId,
                quantity = quantity
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
            OngoingOrderScreen(navigator = navController, id = id, viewModel = ongoingOrderViewModel,productViewModel= productViewModel)
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
        composable(
            route = Screen.OrderHistoryDetailedScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id"){
                    type = NavType.StringType
                    nullable = false
                }
            )
        ){ entry ->
            val id = entry.arguments?.getString("id") ?: ""
            OrderHistoryDetailedScreen(
                navigator = navController,
                orderId = id,
                ongoingOrderViewModel = ongoingOrderViewModel,
                productViewModel = productViewModel,
                account = account
            )
        }
    }
}