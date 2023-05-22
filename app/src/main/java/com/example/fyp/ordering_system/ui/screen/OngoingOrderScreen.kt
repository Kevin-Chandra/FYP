package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus.*
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingOrderScreen(
    navigator: NavController,
    id : String,
    viewModel: OngoingOrderViewModel,
    productViewModel: ProductViewModel
) {
    LaunchedEffect(key1 = true){
        viewModel.getOrderStatus(id)
        viewModel.getOrderItemList(id)
    }
    val statusState = viewModel.orderingStatusState.collectAsStateWithLifecycle()
    val currentOrder = viewModel.currentOrder.collectAsStateWithLifecycle()
    val currentOrderItem = viewModel.currentOrderItem.collectAsStateWithLifecycle()

    FypTheme() {
        Scaffold() {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (statusState.value.loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                if (statusState.value.errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { },
                        confirmButton = {
                            TextButton(onClick = { navigator.navigateUp() }) {
                                Text(text = "Ok")
                            }
                        },
                        title = {
                            Text(text = "Error!")
                        },
                        text = {
                            Text(text = statusState.value.errorMessage!!)
                        }
                    )
                }
                var message by remember { mutableStateOf("") }
                if (statusState.value.success) {
                    if (currentOrder.value is Response.Success) {
                        message =
                            when ((currentOrder.value as Response.Success<Order>).data.orderStatus) {
                                Sent -> "Order is being processed!"
                                Rejected -> "Unfortunately, the restaurant is busy"
                                Confirmed, Ongoing -> "Restaurant is preparing your order!"
                                Finished -> "Order is finished!"
                            }
                        if ((currentOrder.value as Response.Success<Order>).data.orderStatus == Rejected) {
                            AlertDialog(
                                onDismissRequest = { },
                                title = {
                                    Text(text = "Sorry :(")
                                },
                                text = {
                                    Text(text = "Unfortunately, restaurant is busy! Please try again next time...")
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            navigator.navigateUp()
                                        }
                                    ) {
                                        Text(text = "Ok")
                                    }
                                }
                            )
                        }
                    }
                    if (currentOrderItem.value is Response.Success)
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = message,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            var expandOrderItem by rememberSaveable {
                                mutableStateOf(false)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)

                            ) {
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "View Order",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(onClick = {
                                            expandOrderItem = !expandOrderItem
                                        }) {
                                            Icon(
                                                imageVector = if (expandOrderItem) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    if (expandOrderItem) {
                                        LazyColumn() {
                                            item {
                                                Divider()
                                            }
                                            items((currentOrderItem.value as Response.Success<List<OrderItem>>).data) { item ->
                                                ViewOrderItem(orderItem = item,
                                                    getFood = { id ->
                                                        productViewModel.getFood(id)
                                                    },
                                                    getModifierItem = { id ->
                                                        productViewModel.getModifierItem(id)
                                                    },
                                                    getModifier = { id ->
                                                        productViewModel.getModifier(id)
                                                    }
                                                )
                                            }

                                        }
                                    }


                                }
                            }


                        }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOrderItem(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem:(String) -> ModifierItem?
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${orderItem.quantity}x",
                modifier = Modifier.padding(end = 16.dp),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.headlineSmall
            )
            val food = getFood(orderItem.foodId)?:return
            Column() {
                Text(text = food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                orderItem.modifierItems?.forEach {
                    val modifier = getModifier(it.key)
                    if (modifier != null) {
                        Text(text = modifier.name)
                    }
                    it.value.forEach { it1 ->
                        val modifierItem = getModifierItem(it1)
                        modifierItem?.name?.let { it2 -> Text(text = it2, modifier = Modifier.padding(horizontal = 8.dp)) }
                    }
                }
            }
        }
        AssistChip(
            onClick = {  },
            label = {
                Text(text = orderItem.orderItemStatus.name)
            }
        )
    }
}

data class OngoingOrderScreenState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val successMessage : String? = null,
    val status: String? = null,
    val errorMessage : String? = null
)
