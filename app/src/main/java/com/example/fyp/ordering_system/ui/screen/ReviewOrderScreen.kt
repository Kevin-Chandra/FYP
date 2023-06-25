package com.example.fyp.ordering_system.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.OrderingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewOrderScreen(
    navigator: NavController,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel,
    account: Account
) {
    val cart = cartViewModel.cart.value

    val uiState = cartViewModel.orderingUiState.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    FypTheme {
        Surface(
            color = MaterialTheme.colorScheme.primary
        ) {
            val snackBarHostState by remember {
                mutableStateOf(SnackbarHostState())
            }
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            LaunchedEffect(key1 = uiState.value) {
                if (uiState.value is Response.Success) {
                    if ((uiState.value as Response.Success<String>).data == "Order Submitted!") {
                        navigator.navigate(Screen.OngoingOrderScreen.withArgs(cartViewModel.orderId))
                        cartViewModel.resetState()
                    }
                }
                if (uiState.value is Response.Error) {
                    val text = (uiState.value as Response.Error).exception.message ?: ""
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            "Error! $text",
                            null,
                            false,
                            SnackbarDuration.Long
                        )
                    }
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                bottomBar = {
                    Button(
                        enabled = cart.isNotEmpty(),
                        onClick = {
                            cartViewModel.onOrderingEvent(OrderingEvent.SubmitOrder(account.id))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Submit Order")
                    }
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Review Order")
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(modifier = Modifier.padding(it)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.Center)
                            .testTag("revOrder:list")
//                            .verticalScroll(rememberScrollState())

                    ) {
                        if (cart.isEmpty()) {
                            item {
                                Text(
                                    text = "No item in cart",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        } else {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                    Text(
                                        text = "Name",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = "Qty",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Price",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Total",
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(
                                items = cart,
                                key = { item ->
                                    item.orderItemId
                                }
                            ) { item ->
                                val currentItem by rememberUpdatedState(item)
                                val dismissState = rememberDismissState(
                                    confirmValueChange = { value ->
                                        if (value == DismissValue.DismissedToStart){
                                            cartViewModel.onOrderingEvent(
                                                OrderingEvent.FoodDeletedChanged(
                                                    currentItem.orderItemId
                                                )
                                            )
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )

                                SwipeToDismiss(
                                    state = dismissState,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(15)),
                                    directions = setOf(
                                        DismissDirection.EndToStart
                                    ),
                                    background = {
                                        val color by animateColorAsState(
                                            when (dismissState.targetValue) {
                                                DismissValue.Default -> MaterialTheme.colorScheme.background
                                                else -> Color.Red
                                            }
                                        )
                                        val alignment = Alignment.CenterEnd
                                        val icon = Icons.Default.Delete

                                        val scale by animateFloatAsState(
                                            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                        )

                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = alignment
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = "Delete Icon",
                                                modifier = Modifier.scale(scale)
                                            )
                                        }
                                    },
                                    dismissContent = {
                                        OrderItemCard(
                                            getFood = { id ->
                                                productViewModel.getFood(id)
                                            },
                                            modifier = Modifier
                                                .clickable {
                                                    navigator.navigate(
                                                        Screen.AddToCartScreen.withArgs(
                                                            item.foodId,
                                                            item.orderItemId,
                                                            item.quantity.toString()
                                                        )
                                                    )
                                                },
                                            item = item,
                                        )
                                    })
                            }
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal")
                                Text(text = cartViewModel.getSubTotalPrice().toString())
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tax (${cartViewModel.getTaxPercentage()}%)")
                                Text(text = String.format("%.2f", cartViewModel.getTaxValue()))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Service Charge (${cartViewModel.getServiceChargePercentage()}%)")
                                Text(
                                    text = String.format(
                                        "%.2f",
                                        cartViewModel.getServiceChargeValue()
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = String.format("%.2f", cartViewModel.getGrandTotal()),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    if (uiState.value is Response.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun OrderItemCard(getFood: (String) -> Food? ,item: OrderItem, modifier: Modifier){
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFood(item.foodId)?.name ?: "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = modifier.weight(1f)
            )
            Text(text = item.quantity.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
            Text(text = (item.price/item.quantity).toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
            Text(text = item.price.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderItemCartPreview() {
    OrderItemCard(getFood = {Food(name = "jdbajh", price = 83.2, description = "Desc")},
        item = OrderItem(quantity = 1, price = 10.0), modifier = Modifier)
}