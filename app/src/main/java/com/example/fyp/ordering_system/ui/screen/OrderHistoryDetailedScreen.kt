package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.screen.OrderInvoiceDetailedView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryDetailedScreen(
    navigator : NavController,
    orderId: String,
    productViewModel : ProductViewModel,
    ongoingOrderViewModel: OngoingOrderViewModel,
    account : Account
) {

    val order = remember() {
        mutableStateOf<Order?>(null)
    }

    LaunchedEffect(key1 = 1){
        order.value = ongoingOrderViewModel.getPastOrder(orderId)
        ongoingOrderViewModel.getOrderItemList(orderId)
    }

    val orderItemList = ongoingOrderViewModel.currentOrderItem.collectAsStateWithLifecycle()

    FypTheme() {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Detailed Order")
                        },
                        navigationIcon = {
                            IconButton(onClick = {navigator.navigateUp()}) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(it)) {
                    if (order.value != null && orderItemList.value is Response.Success){
                        OrderInvoiceDetailedView(
                            order = order.value?: Order(),
                            orderItemList = (orderItemList.value as Response.Success<List<OrderItem>>).data,
                            accountName = account.first_name + " " + account.last_name,
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
                    } else {
                        CircularProgressIndicator( modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}