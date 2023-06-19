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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.R
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.LinearProgressAnimated
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.viewmodel.CheckoutViewModel
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import com.example.fyp.pos.ui.viewmodel.TableOngoingOrderViewModel
import com.example.fyp.pos.util.ManageTableEvent
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navigator: NavController,
    tableOngoingOrderViewModel: TableOngoingOrderViewModel,
    tableViewModel: ManageTableViewModel,
    productViewModel: ProductViewModel,
    orderId: String,
    tableId: String
) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()

    val order = checkoutViewModel.currentOrder.collectAsStateWithLifecycle()
    val state = checkoutViewModel.checkoutState.collectAsStateWithLifecycle()

    val table = remember {
        mutableStateOf<Table?>(null)
    }
    var finished by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    fun popUp() =  navigator.popBackStack()
    fun navigateInvoiceScreen() =  navigator.navigate(PosScreen.PosInvoiceScreen.withRequiredArgs(orderId))

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(key1 = true){
        table.value = tableViewModel.getTable(tableId)
        checkoutViewModel.initTable(table.value)
    }

    LaunchedEffect(key1 = state.value){
        if (state.value is Response.Success){
            if ((state.value as Response.Success<String>).data == "Table checkout complete!"){
                coroutineScope.launch {
                    when(snackbarHostState.showSnackbar("Table checkout success. Finish Table?","Yes",false,SnackbarDuration.Short)){
                        Dismissed -> {}
                        ActionPerformed -> {
                            tableViewModel.onEvent(ManageTableEvent.OnResetTable(tableId))
                        }
                    }
                }
                finished = true
            }
        }
        if (state.value is Response.Error){
            snackbarHostState.showSnackbar((state.value as Response.Error).exception.message ?: "Error!",null,false,SnackbarDuration.Short)
        }
    }

    FypTheme {
        Surface {
            Scaffold(
                topBar = {
                         TopAppBar(
                             scrollBehavior = scrollBehavior,
                             title = {
                                 Text(text = "Checkout • Table ${table.value?.tableNumber}")
                             },
                             navigationIcon = {
                                 IconButton(onClick = { popUp() }) {
                                     Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                                 }
                             }
                         )
                },
                bottomBar = {
                    if (finished){
                        OutlinedIconButton(
                            onClick = { navigateInvoiceScreen() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "View invoice",
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                table.value?.let {
                                    order.value?.let { it1 ->
                                        checkoutViewModel.checkoutTable(
                                            it,
                                            it1
                                        )
                                    }
                                }
                            },
                            enabled = order.value != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text(text = "Checkout")
                        }
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(it)){
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (!order.value?.orderList.isNullOrEmpty()) {
                            LazyColumn {
                                items(order.value!!.orderList) { item ->
                                    val orderItem = tableOngoingOrderViewModel.getOrderItem(item)
                                    orderItem?.let {
                                        OrderItemRowCheckout(
                                            orderItem = it,
                                            getFood = { id ->
                                                productViewModel.getFood(id)
                                            },
                                            getModifier = { id ->
                                                productViewModel.getModifier(id)
                                            },
                                            getModifierItem = { id ->
                                                productViewModel.getModifierItem(id)
                                            },
                                        )
                                    }
                                }
                                item {
                                    if (order.value != null){
                                        OrderSummary(order = order.value!!)
                                    }
                                }
                            }
                        }
                    }
                    if (finished){
                        LinearProgressAnimated(5000,
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)){ finished ->
                            if (finished) navigateInvoiceScreen()
                        }
                    }
                    if (state.value is Response.Loading){
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummary(order: Order, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Subtotal")
            Text(text = order.subTotal.toString())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Tax (${order.taxPercentage * 100}%)")
            Text(text = String.format("%.2f",order.subTotal * order.taxPercentage))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Service Charge (${order.serviceChargePercentage * 100}%)")
            Text(text = String.format("%.2f",order.subTotal * order.serviceChargePercentage))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format("%.2f", order.grandTotal ),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemRowCheckout(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem: (String) -> ModifierItem?,
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val food = getFood(orderItem.foodId)
            CoilImage(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .padding(8.dp),
                imageModel = { food?.imageUri ?: R.mipmap.ic_launcher },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    contentDescription = "Food Image",
                    colorFilter = null,
                ),
                previewPlaceholder = R.mipmap.ic_launcher,
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        baseColor = Color.Gray,
                        highlightColor = Color.White
                    )
                },
            )
            Column(Modifier.fillMaxWidth(0.6f)) {
                food?.let {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .basicMarquee(),
                            maxLines = 1
                        )
                        Text(
                            text = it.price.toString(),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1
                        )
                    }
                    if (!orderItem.modifierItems.isNullOrEmpty()){
                        Column(
                            modifier = Modifier
                                .heightIn(min = 0.dp, max = 100.dp)
                                .padding(horizontal = 8.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            orderItem.modifierItems.forEach{
                                val thisModifier = getModifier(it.key)
                                Text(text = thisModifier?.name ?: "")
                                it.value.forEach { it1 ->
                                    val modifierItem = getModifierItem(it1)
                                    modifierItem?.let {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = it.name, modifier = Modifier.padding(horizontal = 8.dp))
                                            Text(text = it.price.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Text(
                text = "${orderItem.quantity}x",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = orderItem.price.toString(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

        }

    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun OrderItemRowCheckoutPreview() {
    OrderItemRowCheckout(
        orderItem = OrderItem(
            modifierItems = mapOf(
                "sbjk" to listOf("ds", "sdb", "jdhs"),
                "dskj" to listOf("dsjn,wds", "jwk", "jd")
            ),
            quantity = 10,
            price = 433.32
        ),
        getFood = { Food(name = "ABCevsaesvervesav erfae efrv",price = 191.21) },
        getModifier = { com.example.fyp.menucreator.data.model.Modifier(name = "Sauce") },
        getModifierItem = { ModifierItem(name = "Less") },
    )

}

@Preview(device = "spec:width=411dp,height=891dp", showBackground = true)
@Composable
fun OrderSummaryPreview() {
    OrderSummary(order = Order(subTotal = 10.0, taxPercentage = 0.06))
    OutlinedIconButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "View invoice",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null)
        }
    }
}