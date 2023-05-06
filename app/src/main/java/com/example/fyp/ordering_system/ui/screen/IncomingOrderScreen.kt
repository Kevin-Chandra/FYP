package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.ManageOrderEvent

@Composable
fun ManageOrderScreen(
    navigator: NavController,
    orderType: String,
    incomingOrderViewModel: IncomingOrderViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {

    val incomingType = orderType.equals("Incoming",true)

    val orders = if (incomingType)
        incomingOrderViewModel.incomingOrder.collectAsStateWithLifecycle()
    else
        incomingOrderViewModel.ongoingOrder.collectAsStateWithLifecycle()

    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (orders.value) {
                is Response.Error -> {
                    (orders.value as Response.Error).exception.printStackTrace()
                }

                Response.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is Response.Success -> {
                    val orderList = (orders.value as Response.Success<List<Order>>).data
                    if (orderList.isEmpty()) {
                        Text(
                            text = if (incomingType) "No incoming Order at the moment" else "No ongoing order at the moment",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn() {
                            items(orderList) { item ->
                                var expanded by remember {
                                    mutableStateOf(false)
                                }
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = item.orderId)
                                        Text(text = item.grandTotal.toString())
                                        Button(
                                            onClick = {
                                                incomingOrderViewModel.onEvent(
                                                    ManageOrderEvent.OnClickOrder(
                                                        item.orderId
                                                    )
                                                )
                                                expanded = !expanded
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                contentDescription = ""
                                            )
                                        }
                                    }
                                    if (expanded) {
                                        ViewOrder(item.orderId,incomingOrderViewModel,productViewModel)
                                    }
                                    if (incomingType){
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Button(
                                                onClick = {
                                                    incomingOrderViewModel.onEvent(
                                                        ManageOrderEvent.OnAcceptOrder(
                                                            item.orderId,
                                                            item.orderList
                                                        )
                                                    )
                                                }
                                            ) {
                                                Text(text = "Accept")
                                            }
                                            Button(
                                                onClick = {
                                                    incomingOrderViewModel.onEvent(
                                                        ManageOrderEvent.OnRejectOrder(
                                                            item.orderId,
                                                            item.orderList
                                                        )
                                                    )
                                                }
                                            ) {
                                                Text(text = "Reject")
                                            }
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ViewOrder(
        orderId :String,
        incomingOrderViewModel: IncomingOrderViewModel,
        productViewModel: ProductViewModel
) {

    var state by remember {
        mutableStateOf(UiState())
    }

    LaunchedEffect(key1 = true) {
        incomingOrderViewModel.getOrderItemByOrderId(orderId) {
            when (it) {
                is Response.Error -> {
                    state = UiState(
                        loading = false,
                        error = true,
                        errorMessage = it.exception.message?.let { it1 -> error(it1) }
                    )
                }

                Response.Loading -> {
                    state = UiState(loading = true)
                }

                is Response.Success -> {
                    println("hmm")
                    state = UiState(
                        loading = false,
                        success = true,
                        data = it.data
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        if (state.success) {
            Column() {
                state.data?.forEach { orderItem ->
                    val food = productViewModel.getFood(orderItem.foodId)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = food?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    if (food?.modifiable == true) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            orderItem.modifierItems!!.forEach { i ->
                                val modifierItem = productViewModel.getModifierItem(i)
                                Text(
                                    text = modifierItem?.name ?: ""
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class UiState(
    val error: Boolean = false,
    val errorMessage : String? = null,
    val loading: Boolean = true,
    val success: Boolean = false,
    val data: List<OrderItem>? = null,
)