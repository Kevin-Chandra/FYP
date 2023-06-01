package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
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

    val scroll = rememberScrollState()

    FYPTheme() {
        Surface() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {

                    Text(
                        text = "Confirmed Order",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Confirmed}
                            .size
                            .toString() + " Item",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (ongoingItems.value.any { it.orderItemStatus == OrderItemStatus.Confirmed }){
                    LazyRow(){
                        items(ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Confirmed }
                            .sortedBy { it.timeAdded }){ item ->
                            var showDialog by remember { mutableStateOf(false) }
                            OrderItemCard(
                                orderItem = item,
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                getModifier = { id ->
                                    productViewModel.getModifier(id)
                                },
                                onNextButtonClick = {
                                    viewModel.onEvent(KitchenManageOrderItemEvent.OnPrepareOrderItem(item.orderItemId))
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        showDialog = true
                                    }
                            )
                            if (showDialog){
                                OrderItemDialog(
                                    item = item,
                                    getFood = { id ->
                                        productViewModel.getFood(id)
                                    },
                                    getModifier = { id ->
                                        productViewModel.getModifier(id)
                                    },
                                    getModifierItem = { id ->
                                        productViewModel.getModifierItem(id)
                                    },
                                    onNextButtonClick = {
                                        viewModel.onEvent(KitchenManageOrderItemEvent.OnPrepareOrderItem(item.orderItemId))
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "Preparing Order",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Preparing}
                            .size
                            .toString() + " Item",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                if (ongoingItems.value.any { it.orderItemStatus == OrderItemStatus.Preparing }){
                    LazyRow(){
                        items(ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Preparing }
                            .sortedBy { it.timeAdded }){ item ->
                            var showDialog by remember { mutableStateOf(false) }
                            OrderItemCard(
                                orderItem = item,
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                getModifier = { id ->
                                    productViewModel.getModifier(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                onNextButtonClick = {
                                    viewModel.onEvent(KitchenManageOrderItemEvent.OnFinishOrderItem(item.orderItemId))
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        showDialog = true
                                    }
                            )
                            if (showDialog){
                                OrderItemDialog(
                                    item = item,
                                    getFood = { id ->
                                        productViewModel.getFood(id)
                                    },
                                    getModifier = { id ->
                                        productViewModel.getModifier(id)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {

                    Text(
                        text = "Finished Order",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = ongoingItems.value
                            .filter { it.orderItemStatus == OrderItemStatus.Finished}
                            .size
                            .toString() + " Item",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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
                                getModifier = { id ->
                                    productViewModel.getModifier(id)
                                },
                                getModifierItem = { id ->
                                    productViewModel.getModifierItem(id)
                                },
                                onNextButtonClick = {},
                                modifier = Modifier.padding(8.dp)
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
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem: (String) -> ModifierItem?,
    onNextButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min = 300.dp, max = 340.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = orderItem.orderItemId)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (orderItem.orderItemStatus == OrderItemStatus.Finished){
                    Text(text = "Time finished: ${formatTime(orderItem.timeFinished)}")
                } else {
                    Text(text = formatTime(orderItem.timeAdded),
                        style = MaterialTheme.typography.headlineSmall)
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(text = orderItem.orderItemStatus.name)
                    }
                )
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${orderItem.quantity}x ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(
                    Modifier
                        .height(100.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "[${orderItem.foodId}] ${getFood(orderItem.foodId)?.name}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        //                        modifier = Modifier.padding(8.dp)
                    )
                    if (!orderItem.modifierItems.isNullOrEmpty()) {
                        orderItem.modifierItems.forEach {
                            Text(
                                text = "[${it.key}] ${getModifier(it.key)?.name}",
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            it.value.forEach { id ->
                                Text(
                                    text = "[$id] ${getModifierItem(id)?.name}",
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            //Note Row
            Row{
                Icon(imageVector = Icons.Filled.Description, contentDescription = null, modifier = Modifier.padding(horizontal = 8.dp))
                Column() {
                    Text(text = "Note", fontWeight = FontWeight.Bold)
                    if (orderItem.note.isNullOrEmpty()){
                        Text(text = "No note", fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                    else
                        Text(text = orderItem.note, modifier = Modifier
                            .height(50.dp)
                            .verticalScroll(
                                rememberScrollState()
                            ))
                }
            }
            if (orderItem.orderItemStatus != OrderItemStatus.Finished){
                Row() {
                    Button(onClick = onNextButtonClick) {
                        Text(text = if (orderItem.orderItemStatus == OrderItemStatus.Preparing) "Finish order" else "Prepare order")
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemDialog(
    item: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
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
                    .width(350.dp)
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
                Divider(Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(item.timeAdded),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(text = item.orderItemStatus.name)
                        }
                    )
                }
                Text(
                    text = "Order Item Id",
                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = item.orderItemId,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Divider(Modifier.padding(vertical = 8.dp))
//                Text("Food & Add-ons")
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = "[${item.foodId}] ${getFood(item.foodId)?.name}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (!item.modifierItems.isNullOrEmpty()){
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(start = 16.dp, bottom = 8.dp)
                            ) {
                                item.modifierItems.forEach {
                                    Text(
                                        text = "[${it.key}] ${getModifier(it.key)?.name}",
//                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    it.value.forEach { id ->
                                        Text(text = "[$id] ${getModifierItem(id)?.name}", modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Row {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Column() {
                        Text(text = "Note", fontWeight = FontWeight.Bold)
                        if (item.note.isNullOrEmpty()) {
                            Text(
                                text = "No note",
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Light
                            )
                        } else
                            Text(
                                text = item.note, modifier = Modifier
                                    .height(50.dp)
                                    .verticalScroll(
                                        rememberScrollState()
                                    )
                            )
                    }
                }
                if (item.orderItemStatus != OrderItemStatus.Finished){
                    Divider(Modifier.padding(vertical = 8.dp))
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
        orderItem = OrderItem(Date(), orderItemId = "udwihbh", quantity = 12, orderItemStatus = OrderItemStatus.Confirmed),
        getFood = { Food(name = "ABC", productId = "F-1") },
        getModifierItem = { ModifierItem(name = "!@#") },
        getModifier = {com.example.fyp.menucreator.data.model.Modifier()},
        onNextButtonClick = {}
    )
}