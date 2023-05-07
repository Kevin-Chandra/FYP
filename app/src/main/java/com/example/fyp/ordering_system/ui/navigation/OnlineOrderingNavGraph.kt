package com.example.fyp.ordering_system.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.ordering_system.ui.screen.ManageOrderScreen
import com.example.fyp.ordering_system.ui.screen.MainOnlineOrderingScreen
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel

@Composable
fun OnlineOrderingNavGraph(
    navController : NavHostController,
    incomingOrderViewModel: IncomingOrderViewModel,
    productViewModel: ProductViewModel,
    accountViewModel: AccountViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainOnlineOrderingScreen.route){

        composable(
            route = Screen.ViewOrderScreen.route
        ){
            MainOnlineOrderingScreen(navigator = navController)
        }
        composable(
            route = Screen.MainOnlineOrderingScreen.route
        ){
            MainOnlineOrderingScreen(navigator = navController)
        }
        composable(
            route = Screen.ManageOrderScreen.route + "/{type}",
            arguments = listOf(
                navArgument("type"){
                    type = NavType.StringType
                    nullable = false
                }
            )
        ){ entry ->
            val type = entry.arguments?.getString("type") ?: "Incoming"
            ManageOrderScreen(navigator = navController,type,incomingOrderViewModel,productViewModel,accountViewModel)
        }
    }
}