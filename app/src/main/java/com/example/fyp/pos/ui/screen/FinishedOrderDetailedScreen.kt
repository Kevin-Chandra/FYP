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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.formatDate
import com.example.fyp.pos.ui.viewmodel.PastOrderViewModel
import com.example.fyp.theme.FypTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedOrderDetailedScreen(
    navigator: NavController,
    pastOrderViewModel: PastOrderViewModel,
    productViewModel: ProductViewModel,
    orderId: String?
) {

    val accountViewModel = hiltViewModel<AccountViewModel>()

    val order = remember { mutableStateOf<Order?>(null) }
    var orderBy by remember { mutableStateOf<String?>(null) }
    val orderItemResponse = pastOrderViewModel.orderItemListResponse.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true){
        pastOrderViewModel.getOrderItemDetails(orderId ?: "")
        order.value = pastOrderViewModel.getOrder(orderId ?: "")
    }

    LaunchedEffect(key1 = order){
        if (order.value?.orderType == OrderType.Online && orderBy.isNullOrEmpty()){
            val res = withContext(Dispatchers.Default) {
                accountViewModel.getAccount(order.value!!.orderBy)
            }
            orderBy = "${res?.first_name} ${res?.last_name}"
        }
    }

    FypTheme {
        Surface {
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
                    if (orderItemResponse.value is Response.Success){
                        OrderInvoiceDetailedView(
                            order = order.value?: Order(),
                            orderItemList = (orderItemResponse.value as Response.Success<List<OrderItem>>).data,
                            accountName = orderBy,
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


@Composable
fun OrderInvoiceDetailedView(
    order: Order,
    orderItemList: List<OrderItem>,
    accountName: String?,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem: (String) -> ModifierItem?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
    ) {
        item{
            if (order.orderStatus == OrderStatus.Rejected) CancelledText()
            OrderDetailedViewHeader(order,accountName)
            Spacer(modifier = Modifier.height(16.dp))
            OrderItemListHeader()
        }
        items(orderItemList){
            OrderItemRow(
                orderItem = it,
                getFood = getFood,
                getModifier = getModifier,
                getModifierItem = getModifierItem
            )
        }
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            PosOrderSummary(order = order)
        }
        if (order.orderStatus == OrderStatus.Rejected){
            item {
                CancelledText()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderDetailedViewHeader(
    order:Order,
    accountName:String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (order.orderType == OrderType.Online){
            AssistChip(onClick = { }, label =  {
                Text(text = "Online Order")
            })
            Text(
                text = accountName ?: "",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            Text(text = "Table #",style = MaterialTheme.typography.headlineMedium)
            Text(
                text = order.tableNumber,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.align(Alignment.Start)) {
            Text(
                text = "Order Id:",
                style = MaterialTheme.typography.titleMedium,)
            Text(
                text = order.orderId,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .basicMarquee()
            )
        }
        Text(
            text = "Order created: ${formatDate(order.orderStartTime)}",
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        if (order.orderStatus != OrderStatus.Rejected){
            Text(
                text = "Order finish: ${formatDate(order.orderFinishTime)}",
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

    }
}

@Composable
fun CancelledText() {
    Text(
        text = "CANCELLED ORDER",
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemListHeader() {
    Column {
        val textStyle = MaterialTheme.typography.titleLarge
        val fontWeight = FontWeight.Bold
        val textAlign = TextAlign.Center
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Item",
                fontWeight = fontWeight,
                modifier = Modifier.weight(3f),
                style = textStyle,
            )
            Text(
                text = "Price",
                fontWeight = fontWeight,
                modifier = Modifier.weight(1f),
                style = textStyle,
                textAlign = textAlign
            )
            Text(
                text = "Qty",
                fontWeight = fontWeight,
                modifier = Modifier.weight(1f),
                style = textStyle,
                textAlign = textAlign
            )
            Text(
                text = "Amount",
                fontWeight = fontWeight,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(),
                style = textStyle,
                maxLines = 1
            )
        }
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemRow(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem: (String) -> ModifierItem?,
) {
    val food = getFood(orderItem.foodId) ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var price by remember { mutableDoubleStateOf(food.price) }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .weight(3f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = food.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.basicMarquee()
            )
            if (orderItem.modifierItems?.isNotEmpty() == true) {
                orderItem.modifierItems.forEach {
                    Text(
                        text = getModifier(it.key)?.name ?: "",
                        fontWeight = FontWeight.SemiBold
                    )
                    it.value.forEach { item ->
                        val modifierItem = getModifierItem(item)
                        LaunchedEffect(key1 = true){
                            price += modifierItem?.price ?: 0.0
                        }
                        Text(
                            text = modifierItem?.name ?: "",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = price.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${orderItem.quantity}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${orderItem.price}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PosOrderSummary(
    order: Order
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subtotal",
                style = MaterialTheme.typography.titleMedium,)
            Text(
                text = String.format("%.2f",order.subTotal),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tax (${order.taxPercentage * 100}%)",
                style = MaterialTheme.typography.titleMedium,)
            Text(
                text = String.format("%.2f",order.subTotal * order.taxPercentage),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Service Charge (${order.serviceChargePercentage * 100}%)",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = String.format("%.2f",order.subTotal * order.serviceChargePercentage),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = String.format("%.2f",order.grandTotal),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
            )
        }
    }
}

@Preview(device = "id:pixel_4_xl", showBackground = true)
@Composable
fun PastOrderCardPreview1() {
    OrderInvoiceDetailedView(
        order = Order(
            orderList = listOf("dsj", "dss", "fsv", "fsj", "sjd", "dss", "fsv", "fsj", "sjd"),
            orderId = "ukbdz saduhbsiu anvuifsh uvhduivd fuvhi",
            orderType = OrderType.Online,
            tableNumber = "23",
            orderStatus = OrderStatus.Rejected
        ),
//        getOrderItem ={ OrderItem(orderItemId = "shdbcubsi qhfcds hcsbvdjsb dvuhsi", quantity = 10,modifierItems = mapOf("sj" to listOf("djsd"), "ssj" to listOf("djscd"))) },
        getFood = { Food(name = "ABCD Agsvy") },
        accountName = "Acsdcs, fsjvnz fszjfvzsn svjc",
        getModifierItem = {
            ModifierItem(name = "skj")
        },
        getModifier = {
            com.example.fyp.menucreator.data.model.Modifier(name = "dsjk")
        },
        orderItemList = listOf(OrderItem(),OrderItem(),OrderItem(modifierItems = mapOf("sj" to listOf("djsd"), "ssj" to listOf("djscd"))))
    )
}

@Preview(showBackground = true)
@Composable
fun OrderHeaderPreview() {
    OrderDetailedViewHeader(
        order = Order(orderId = "kjds dscjnkfjnzsv dfivhawinvf asiuhvasknjz fuhvidkzdvd"),
        accountName = "dswjk awuijd"
    )
}

@Preview
@Composable
fun OrderItemRowPreview() {
    val pair = Pair("sdjn" , listOf("askj","kdf","dsk"))
    OrderItemRow(
        orderItem = OrderItem(modifierItems = mapOf(pair, "sj" to listOf("djsd"), "ssj" to listOf("djscd"))),
        getFood = { Food(name = "ABCD Agsvy") },
        getModifier ={ com.example.fyp.menucreator.data.model.Modifier(name = "MOd")},
        getModifierItem = {
            ModifierItem(name = "Abc mod 1")
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun PosOrderSummaryPreview() {
    PosOrderSummary(order = Order(
        serviceChargePercentage = 0.04,
        taxPercentage = 0.06,
        subTotal = 200.0,
    ))
}