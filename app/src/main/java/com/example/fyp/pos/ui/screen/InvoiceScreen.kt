package com.example.fyp.pos.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.viewmodel.TableOngoingOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    navigator: NavController,
    orderId: String,
    tableOngoingOrderViewModel: TableOngoingOrderViewModel,
    productViewModel: ProductViewModel
) {

    fun navigateBack() = navigator.popBackStack(PosScreen.ManageTableScreen.route,false)

    BackHandler() {
        navigateBack()
    }

    val order = remember { mutableStateOf<Order?>(null) }
    val orderItemList = remember { mutableStateOf<List<OrderItem>>(listOf()) }

    LaunchedEffect(key1 = 1){
        order.value = tableOngoingOrderViewModel.getOrder(orderId)
    }

    LaunchedEffect(key1 = order){
        if (order.value != null){
            orderItemList.value = order.value!!.orderList.map {
                tableOngoingOrderViewModel.getOrderItem(it)?: OrderItem()
            }
        }
    }

    FypTheme() {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(text = "Invoice")
                        }
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(it)){
                    if (order.value != null){
                        OrderInvoiceDetailedView(
                            order = order.value!!,
                            orderItemList = orderItemList.value,
                            accountName = "",
                            getFood = {
                                productViewModel.getFood(it)
                            },
                            getModifier = {
                                productViewModel.getModifier(it)
                            },
                            getModifierItem = {
                                productViewModel.getModifierItem(it)
                            },
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

}