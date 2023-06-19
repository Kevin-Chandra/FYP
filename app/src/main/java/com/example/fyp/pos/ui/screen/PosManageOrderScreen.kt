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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fyp.theme.FypTheme
import com.example.fyp.R
import com.example.fyp.account_management.ui.view_model.AccountViewModel
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ManageOrderScreen(
    manageOrderViewModel: ManageOrderViewModel,
    productViewModel: ProductViewModel,

) {
    val accountViewModel: AccountViewModel = hiltViewModel()

    val ongoingOrders = manageOrderViewModel.ongoingOrder.collectAsStateWithLifecycle()
    val ongoingOrderItems = manageOrderViewModel.ongoingOrderItem.collectAsStateWithLifecycle()

    var showDialog by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    FypTheme {
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
                if (ongoingOrders.value.isNotEmpty()){
                    LazyColumn {
                        items(ongoingOrders.value){ item ->
                            var firstName by rememberSaveable { mutableStateOf<String?>(null) }
                            var lastName by rememberSaveable { mutableStateOf<String?>(null)  }

                            if (item.orderType == OrderType.Online){
                                if (firstName.isNullOrEmpty()){
                                    LaunchedEffect(key1 = true){
                                        val res = coroutineScope.async {
                                            accountViewModel.getAccount(item.orderBy)
                                        }.await()
                                        firstName = res?.first_name
                                        lastName = res?.last_name
                                    }
                                }
                            }
                            OngoingOrderCard(order = item,
                                getOrderItem = { id ->
                                    ongoingOrderItems.value[id]
                                },
                                getFood = { id ->
                                    productViewModel.getFood(id)
                                },
                                orderBy = if (lastName.isNullOrEmpty()) firstName else "$firstName $lastName",
                                onFinishOrderClicked = {
                                    val unfinishedOrder = item.orderList.any{
                                            id -> ongoingOrderItems.value[id]?.orderItemStatus != OrderItemStatus.Finished
                                    }
                                    if (unfinishedOrder){
                                        showDialog = true
                                    } else {
                                        manageOrderViewModel.onEvent(PosManageOrderEvent.OnFinishOrderItem(item))
                                    }
                                }
                            )
                        }
                    }
                } else {
                    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.no_incoming_order))
                    val animProgress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever )

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { animProgress },
                            modifier = Modifier
                                .size(400.dp)
                        )
                        Text(
                            text = "No orders at the moment...",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
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
//        dismissButton = {
//            TextButton(onClick = { showDialog(false) }) {
//                Text(text = "Cancel")
//            }
//        },
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
    orderBy: String?,
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
            Divider(Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Type",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = order.orderType.name,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
//            Divider(Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (order.orderType == OrderType.DineIn) "Table Number" else "Order By",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineSmall
                )
                val text = if (order.orderType == OrderType.Online){
                        orderBy.toString()
                } else {
                    order.tableNumber
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .basicMarquee(),
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
            Divider(Modifier.padding(vertical = 4.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 250.dp)
            ) {
                items(order.orderList) {
                    val item = getOrderItem(it)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.7f),
                        ) {
                            Text(
                                text = item?.orderItemId ?: "[Unknown]",
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = item?.foodId?.let { it1 -> "${item.quantity}x   ${getFood(it1)?.name}" }
                                    ?: "",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall)
                        }
                        AssistChip(onClick = {},
                            label = {
                                item?.orderItemStatus?.let { it1 -> Text(text = it1.name) }
                            }
                        )
                    }
                }
            }
        }
        if (order.orderStatus != OrderStatus.Sent && order.orderType == OrderType.Online) {
            Button(
                onClick = onFinishOrderClicked,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            ) {
                Text(text = "Finish Order")
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Preview
@Composable
fun OrderCardPreview() {
    OngoingOrderCard(
        order = Order(orderList = listOf("dsj","dss","fsv","fsj","sjd","dss","fsv","fsj","sjd"), orderId = "ukbdz saduhbsiu anvuifsh uvhduivd fuvhi", orderType = OrderType.Online, tableNumber = "23"),
        getOrderItem ={ OrderItem(orderItemId = "shdbcubsi qhfcds hcsbvdjsb dvuhsi", quantity = 10) },
        getFood = { Food(name = "ABCD Agsvy") },
        orderBy = "Acsdcs, fsjvnz fszjfvzsn svjc"
    ) {

    }
}