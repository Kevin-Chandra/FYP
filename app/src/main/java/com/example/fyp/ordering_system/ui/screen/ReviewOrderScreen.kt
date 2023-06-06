package com.example.fyp.ordering_system.ui.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material.Snackbar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.OrderingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReviewOrderScreen(
    navigator: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
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
                snackbarHost = { SnackbarHost( hostState = snackBarHostState) },
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
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(modifier = Modifier.padding(it)) {
                    if (uiState.value is Response.Loading){
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState())

                    ) {
                        if (cart.isEmpty()){
                            Text(
                                text = "No item in cart",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Name", modifier = Modifier.weight(1.5f))
                                Text(text = "Qty", modifier = Modifier.weight(1f))
                                Text(text = "Price", modifier = Modifier.weight(1f))
                                Text(text = "Total", modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.weight(0.5f))
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Column(
                                Modifier.fillMaxWidth()
                            ){
                                cart.forEach{ item ->
                                    OrderItemCard(
                                        Modifier
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
                                        productViewModel,
                                        cartViewModel
                                    )
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Subtotal")
                            Text(text = cartViewModel.getSubTotalPrice().toString())
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Tax (${cartViewModel.getTaxPercentage()}%)")
                            Text(text = String.format("%.2f",cartViewModel.getTaxValue()))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Service Charge (${cartViewModel.getServiceChargePercentage()}%)")
                            Text(text = String.format("%.2f",cartViewModel.getServiceChargeValue()))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%.2f", cartViewModel.getGrandTotal() ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(modifier: Modifier, item: OrderItem, productViewModel: ProductViewModel, cartViewModel: CartViewModel) {
    val food = productViewModel.getFood(item.foodId)?: return
    OrderItemCart(
        food = food,
        item = item,
        onDeleteClicked = {
            cartViewModel.onOrderingEvent(OrderingEvent.FoodDeletedChanged(it))
        },
        modifier = modifier)
}

@Composable
fun OrderItemCart(food: Food,item: OrderItem, onDeleteClicked: (String) -> Unit, modifier: Modifier){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = food.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f)
            )
            Text(text = item.quantity.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
            Text(text = item.price.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
            Text(text = (item.price * item.quantity).toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.weight(1f))
            IconButton(
                onClick = { onDeleteClicked(item.orderItemId) },
                modifier = modifier.weight(0.5f)
            ) {
                Icon(imageVector = Icons.TwoTone.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderItemCartPreview() {
    OrderItemCart(food = Food(name = "jdbajh", price = 83.2, description = "Desc"),
        item = OrderItem(quantity = 1, price = 10.0),
        onDeleteClicked = {} , modifier = Modifier)
}