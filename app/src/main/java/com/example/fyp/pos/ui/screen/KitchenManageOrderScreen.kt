package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.formatDate
import com.example.fyp.ordering_system.util.formatTime
import com.example.fyp.pos.ui.theme.FYPTheme
import com.example.fyp.pos.ui.viewmodel.IncomingOrderItemViewModel
import com.example.fyp.pos.util.KitchenManageOrderItemEvent
import java.util.Date

@Composable
fun KitchenManageOrderScreen(
    navigator : NavController,
    viewModel: IncomingOrderItemViewModel,
    productViewModel: ProductViewModel
) {
    val ongoingItems = viewModel.ongoingOrderItems.collectAsStateWithLifecycle()
    val confirmedOrders = remember {
        mutableStateListOf<OrderItem>()
    }

    LaunchedEffect(key1 = ongoingItems){
        confirmedOrders.clear()
        confirmedOrders.addAll(ongoingItems.value.filter { it.orderItemStatus == OrderItemStatus.Confirmed })
    }

    FYPTheme() {
        Surface() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Confirmed Order", style = MaterialTheme.typography.headlineMedium)
                if (ongoingItems.value.any { it.orderItemStatus == OrderItemStatus.Confirmed }){
                    LazyRow(){
                        items(ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Confirmed }
                            .sortedByDescending { it.timeAdded }){ item ->
                            var showDialog by remember { mutableStateOf(false) }
                            OrderItemCard(
                                orderItem = item,
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                onNextButtonClick = {
                                    viewModel.onEvent(KitchenManageOrderItemEvent.OnPrepareOrderItem(item.orderItemId))
                                },
                                modifier = Modifier.clickable {
                                    showDialog = true
                                }
                            )
                            if (showDialog){
                                OrderItemDialog(
                                    item = item,
                                    getFood = { id ->
                                        productViewModel.getFood(id)
                                    },
                                    getModifierItem = { id ->
                                        productViewModel.getModifierItem(id)
                                    },
                                    onNextButtonClick = {
                                        viewModel.onEvent(KitchenManageOrderItemEvent.OnFinishOrderItem(item.orderItemId))
                                    },
                                    showDialog = {
                                        showDialog = it
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No orders coming in right now...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Divider()

                Text(text = "Preparing Order", style = MaterialTheme.typography.headlineMedium)
                if (ongoingItems.value.any { it.orderItemStatus == OrderItemStatus.Preparing }){
                    LazyRow(){
                        items(ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Preparing }
                            .sortedByDescending { it.timeAdded }){ item ->
                            var showDialog by remember { mutableStateOf(false) }
                            OrderItemCard(
                                orderItem = item,
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                onNextButtonClick = {
                                    viewModel.onEvent(KitchenManageOrderItemEvent.OnFinishOrderItem(item.orderItemId))
                                },
                                modifier = Modifier.clickable {
                                    showDialog = true
                                }
                            )
                            if (showDialog){
                                OrderItemDialog(
                                    item = item,
                                    getFood = { id ->
                                        productViewModel.getFood(id)
                                    },
                                    getModifierItem = { id ->
                                        productViewModel.getModifierItem(id)
                                    },
                                    onNextButtonClick = {
                                        viewModel.onEvent(KitchenManageOrderItemEvent.OnFinishOrderItem(item.orderItemId))
                                    },
                                    showDialog = {
                                        showDialog = it
                                    }
                                )
                            }
                        }
                    }
                }else {
                    Text(
                        text = "No order to prepare right now...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Divider()

                Text(text = "Finished Order", style = MaterialTheme.typography.headlineMedium)
                if (ongoingItems.value.any { it.orderItemStatus == OrderItemStatus.Finished }){
                    LazyRow{
                        items(ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Finished }
                            .sortedByDescending { it.timeFinished }){ item ->

                            OrderItemCard(
                                orderItem = item,
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                onNextButtonClick = {},
                            )
                        }
                    }
                }else {
                    Text(
                        text = "No order finished...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifierItem: (String) -> ModifierItem?,
    onNextButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min= 300.dp,max= 340.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (orderItem.orderItemStatus == OrderItemStatus.Finished){
                    Text(text = "Time finished: ${formatTime(orderItem.timeFinished)}")
                } else {
                    Text(text = formatTime(orderItem.timeAdded))
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(text = orderItem.orderItemStatus.name)
                    }
                )
            }

            Divider()
            Text(text = orderItem.orderItemId)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "[${orderItem.foodId}] ${getFood(orderItem.foodId)?.name}")
                Text(text = orderItem.quantity.toString())
            }
            Column(
                Modifier
                    .height(100.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                orderItem.modifierItems?.forEach{
                    Text(text = "[${it}] ${getModifierItem(it)?.name}")
                }
            }
            if (orderItem.orderItemStatus != OrderItemStatus.Finished){
                Row() {
                    Button(onClick = onNextButtonClick) {
                        Text(text = "Next")
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemDialog(
    item: OrderItem,
    getFood: (String) -> Food?,
    getModifierItem: (String) -> ModifierItem?,
    onNextButtonClick: () -> Unit,
    showDialog: (Boolean) -> Unit
) {

    Dialog(
        onDismissRequest = { showDialog(false) },
        properties = DialogProperties()
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "View Item Details",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 0.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(item.timeAdded))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(text = item.orderItemStatus.name)
                        }
                    )
                }
                Text(text = "Order Item Id")
                Text(text = item.orderItemId)
                Divider()
                Text("Food & Add-ons")
                Text(text = "${item.foodId} ${getFood(item.foodId)?.name}")
                if (!item.modifierItems.isNullOrEmpty()){
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item.modifierItems.forEach {
                            val modifierItem = getModifierItem(it)
                            Text(text = "[$it] ${modifierItem?.name}")
                        }
                    }
                }
                if (item.orderItemStatus != OrderItemStatus.Finished){
                    Button(onClick = {
                        onNextButtonClick()
                        showDialog(false)
                    }) {
                        Text(text = if (item.orderItemStatus == OrderItemStatus.Confirmed) "Prepare order" else "Finish Order")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = false)
@Composable
fun OrderItemCardPreview() {
    OrderItemCard(
        orderItem = OrderItem(Date(), orderItemId = "udwihbh", quantity = 12),
        getFood = { Food(name = "ABC", productId = "F-1") },
        getModifierItem = { ModifierItem(name = "!@#") },
        onNextButtonClick = {}
    )
}