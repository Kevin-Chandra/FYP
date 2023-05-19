package com.example.fyp.pos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.screen.MainPosScreen
import com.example.fyp.pos.ui.screen.KitchenManageOrderScreen
import com.example.fyp.pos.ui.screen.ManageOrderScreen
import com.example.fyp.pos.ui.viewmodel.IncomingOrderItemViewModel
import com.example.fyp.pos.ui.viewmodel.ManageOrderViewModel

@Composable
fun PosNavGraph(
    navController : NavHostController,
    productViewModel: ProductViewModel,
    incomingOrderItemViewModel: IncomingOrderItemViewModel,
    manageOrderViewModel: ManageOrderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = PosScreen.MainPosScreen.route){

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
    }
}