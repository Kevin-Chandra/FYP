package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.FypTheme
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.formatTime
import com.example.fyp.pos.ui.viewmodel.ManageOrderViewModel
import com.example.fyp.pos.util.PosManageOrderEvent

@Composable
fun ManageOrderScreen(
    manageOrderViewModel: ManageOrderViewModel,
    productViewModel: ProductViewModel
) {

    val ongoingOrders = manageOrderViewModel.ongoingOrder.collectAsStateWithLifecycle()
    val ongoingOrderItems = manageOrderViewModel.ongoingOrderItem.collectAsStateWithLifecycle()

    var showDialog by remember() {
        mutableStateOf(false)
    }

    FypTheme() {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()){
                if (showDialog){
                    ErrorDialog(
                        msg = "All Order Item must be finished before ending this order",
                        title = "Error!",
                        cancellable = true,
                        onConfirmClick = { showDialog = false },
                        showDialog = {
                            showDialog = it
                        }
                    )
                }
                LazyColumn(){
                    items(ongoingOrders.value){
                        OngoingOrderCard(order = it,
                            getOrderItem = { id ->
                                ongoingOrderItems.value[id]
                            },
                            getFood = { id ->
                                productViewModel.getFood(id)
                            },
                            onFinishOrderClicked = {
                                val unfinishedOrder = it.orderList.any{
                                        id -> ongoingOrderItems.value[id]?.orderItemStatus != OrderItemStatus.Finished
                                }
                                if (unfinishedOrder){
                                    showDialog = true
                                } else {
                                    manageOrderViewModel.onEvent(PosManageOrderEvent.OnFinishOrderItem(it))
                                }
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun ErrorDialog(
    msg: String,
    title: String,
    cancellable: Boolean,
    onConfirmClick: () -> Unit,
    showDialog: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (cancellable) showDialog(false) },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = "Ok")
            }
        },
        title = {
            Text(text = title)
        },
        dismissButton = {
            TextButton(onClick = { showDialog(false) }) {
                Text(text = "Cancel")
            }
        },
        text = {
            Text(text = msg)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OngoingOrderCard(
    order: Order,
    getOrderItem: (String) -> OrderItem?,
    getFood: (String) -> Food?,
    onFinishOrderClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = order.orderId,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(order.orderStartTime),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                AssistChip(
                    onClick = {},
                    label = {
                    Text(text = order.orderStatus.name)
                })
            }
            Divider(Modifier.padding(vertical = 8.dp))
            order.orderList.forEach {
                val item = getOrderItem(it)
                println(item)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column() {
                        Text(
                            text = item?.orderItemId ?: "[Unknown]",
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = item?.foodId?.let { it1 -> "${item.quantity}x   ${getFood(it1)?.name}" } ?: "",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall)
                    }
                    AssistChip(onClick = {},
                        label = {
                            item?.orderItemStatus?.let { it1 -> Text(text = it1.name) }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (order.orderStatus != OrderStatus.Sent && order.orderType == OrderType.Online){
                Button(
                    onClick = onFinishOrderClicked,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                ) {
                    Text(text = "Finish Order")
                }
            }
        }
    }
}