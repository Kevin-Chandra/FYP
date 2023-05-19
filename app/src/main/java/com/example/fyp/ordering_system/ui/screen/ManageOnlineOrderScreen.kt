package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.ManageOrderEvent
import com.example.fyp.ordering_system.util.formatDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManageOrderScreen(
    navigator: NavController,
    orderType: String,
    incomingOrderViewModel: IncomingOrderViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel()
) {

    val incomingType = orderType.equals("Incoming", true)

    val orders = if (incomingType)
        incomingOrderViewModel.incomingOrders.collectAsStateWithLifecycle()
    else
        incomingOrderViewModel.ongoingOrders.collectAsStateWithLifecycle()

    val uiState = incomingOrderViewModel.manageOrderUiState.collectAsStateWithLifecycle()

    val snackBarHostState by remember {
        mutableStateOf(SnackbarHostState())
    }
    val coroutineScope = rememberCoroutineScope()

    FypTheme() {
        Surface {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = if (incomingType) "Incoming Order" else "Ongoing Order")
                        },
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier.statusBarsPadding(),
                    )
                },
                snackbarHost = { SnackbarHost( hostState = snackBarHostState) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    var showDialog by remember {
                        mutableStateOf<Order?>(null)
                    }
                    DeleteOrderDialog(
                        order = showDialog,
                        onDismiss = {
                            showDialog = null
                        },
                        onDeleteClick = { order ->
                            incomingOrderViewModel.onEvent(ManageOrderEvent.OnDeleteOrder(order))
                            showDialog = null
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                    if (uiState.value.success) {
                        val orderList = orders.value
                        if (orderList.isEmpty()) {
                            Text(
                                text = if (incomingType) "No incoming Order at the moment" else "No ongoing order at the moment",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = 16.dp),
                            ) {
                                items(
                                    count = orderList.size,
                                    key = { index ->
                                        orderList[index].orderId
                                    }
                                ) { i ->
                                    OrderCard(
                                        order = orderList[i],
                                        onCLickAcceptOrder = { id,list ->
                                            incomingOrderViewModel.onEvent(ManageOrderEvent.OnAcceptOrder(id,list))
                                        },
                                        onCLickRejectOrder = { id,list ->
                                            incomingOrderViewModel.onEvent(ManageOrderEvent.OnRejectOrder(id,list))
                                            showDialog = orderList[i]
                                        },
                                        getFood = { id ->
                                            productViewModel.getFood(id)
                                        },
                                        getModifierItem = { id ->
                                            productViewModel.getModifierItem(id)
                                        },
                                        showAcceptRejectButtons = incomingType,
                                        accountViewModel = accountViewModel,
                                        incomingOrderViewModel = incomingOrderViewModel
                                    )
                                }
                            }
                        }
                    }
                    if (uiState.value.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

            }
            LaunchedEffect(key1 = uiState.value) {
                uiState.value.errorMessage?.let { msg ->
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            msg,
                            null,
                            true,
                            SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteOrderDialog(
    order: Order?,
    onDismiss: () -> Unit,
    onDeleteClick: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    if (order != null){
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onDeleteClick(order) }) {
                    Text(text = "Delete Order")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
            },
            title = {
                Text(text = "Delete rejected order?")
            },
            text = {
                Text(text = "Do you want to delete order ${order.orderId}, this action cannot be undone!")
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderCard(
    order: Order,
    onCLickAcceptOrder: (String, List<String>)-> Unit,
    onCLickRejectOrder: (String, List<String>)-> Unit,
    getFood: (String) -> Food?,
    getModifierItem: (String) -> ModifierItem?,
    showAcceptRejectButtons: Boolean,
    accountViewModel: AccountViewModel,
    incomingOrderViewModel: IncomingOrderViewModel
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = formatDate(order.orderStartTime))
                IconButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = ""
                    )
                }
            }
            OrderByView(
                orderBy = order.orderBy,
                accountViewModel = accountViewModel
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(4f)
                        .basicMarquee(),
                    maxLines = 1
                )
                Text(
                    text = order.grandTotal.toString(),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            if (expanded) {
                ViewOrderDetails(
                    order.orderId,
                    incomingOrderViewModel,
                    getFood,
                    getModifierItem
                )
            }
            if (showAcceptRejectButtons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onCLickRejectOrder(order.orderId,order.orderList) }
                    ) {
                        Text(text = "Reject")
                    }
                    Button(
                        onClick = { onCLickAcceptOrder(order.orderId,order.orderList) }
                    ) {
                        Text(text = "Accept")
                    }

                }
            }
        }
    }
}

@Composable
fun ViewOrderDetails(
    orderId :String,
    incomingOrderViewModel: IncomingOrderViewModel,
    getFood: (String) -> Food?,
    getModifierItem: (String) -> ModifierItem?
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
                        errorMessage = it.exception.message
                    )
                }

                Response.Loading -> {
                    state = UiState(loading = true)
                }

                is Response.Success -> {
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
            state.data?.let {
                ViewOrderDetails(
                    orderList = it,
                    getFood = getFood ,
                    getModifierItem = getModifierItem
                )
            }
        }
    }
}

@Composable
fun ViewOrderDetails(
    orderList: List<OrderItem>,
    getFood : (String) -> Food?,
    getModifierItem: (String) -> ModifierItem?
) {
    Column(Modifier.padding(8.dp)) {
        Divider()
        Row(
           modifier = Modifier
               .fillMaxWidth()
               .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Name",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Divider()
        orderList.forEachIndexed{ i,orderItem ->
           val food = getFood(orderItem.foodId)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${i+1}. ${food?.name ?: ""}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = orderItem.quantity.toString(),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (food?.modifiable == true) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 4.dp)
                ) {
                    orderItem.modifierItems!!.forEach { i ->
                        val modifierItem = getModifierItem(i)
                        Text(
                            text = modifierItem?.name ?: ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderByView(
    orderBy: String,
    accountViewModel: AccountViewModel
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }
    var email by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    if (email == null){
        LaunchedEffect(key1 = true) {
            accountViewModel.getAccount(orderBy) {
                if (it is Response.Success){
                    it.data?.let { data ->
                        name = data.first_name + " " + data.last_name
                        email = data.email
                    }
                }
            }
        }
    }
    email?.let {
        OrderByView(name, it)
    }

}

@Composable
fun OrderByView(
    name: String,
    email: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = email,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderByViewPreview() {
    OrderByView(name = "fnskin", email = "sfhfbj@gmail.com")
}

@Preview(showBackground = true)
@Composable
fun ViewOrderPreview() {
    ViewOrderDetails(
        orderList =  listOf(OrderItem()),
         { it ->
            Food(name = "ABC")
        },{ it ->
            ModifierItem(name = "Meow")
        })
}


data class UiState(
    val error: Boolean = false,
    val errorMessage : String? = null,
    val loading: Boolean = true,
    val success: Boolean = false,
    val data: List<OrderItem>? = null,
    val account: Account? = null
)