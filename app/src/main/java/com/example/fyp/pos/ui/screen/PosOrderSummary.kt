package com.example.fyp.pos.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarResult.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.LinearProgressAnimated
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.viewmodel.TableOrderCartViewModel
import com.example.fyp.pos.util.TableOrderEvent
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosOrderSummary(
    navigator: NavController,
    productViewModel: ProductViewModel,
    tableOrderViewModel: TableOrderCartViewModel
) {

    val cart = tableOrderViewModel.cart

    val response = tableOrderViewModel.posOrderUiState.collectAsStateWithLifecycle()

    var loading by remember {
        mutableStateOf(false)
    }
    var finished by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    fun popUp() =  navigator.popBackStack(PosScreen.ManageTableScreen.route,false)

    LaunchedEffect(key1 = response.value){
        println(response.value)
        when(response.value){
            is Response.Error -> {
                loading = false
            }
            Response.Loading -> {
                loading = true
            }
            is Response.Success -> {
                loading = false
                if ((response.value as Response.Success<String>).data.isNotEmpty()){
                    finished = true
                    when(snackbarHostState.showSnackbar((response.value as Response.Success<String>).data,"Back to table",false,SnackbarDuration.Short)){
                        Dismissed -> {}
                        ActionPerformed -> {
                            popUp()
                        }
                    }
                }
            }
        }
    }


    BackHandler {
        tableOrderViewModel.resetState()
        if (!loading){
            navigator.popBackStack()
        }
    }


    FypTheme() {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text(text = "Order Summary")
                    })
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total item",style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = cart.value.sumOf { it.quantity }.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { tableOrderViewModel.onEvent(TableOrderEvent.SubmitOrder) },
                            enabled = cart.value.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(text = "Submit Order")
                        }
                    }
                }
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(it)){
                    if (cart.value.isNotEmpty()){
                            LazyColumn(){
                                items(cart.value){
                                    OrderItemRow(
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
                                        onDelete = {
                                            tableOrderViewModel.onEvent(TableOrderEvent.OnDeleteOrderItem(it.orderItemId))
                                        },
                                        onDecrement = { qty ->
                                            tableOrderViewModel.onEvent(TableOrderEvent.OnIncrementQuantity(it,qty))
                                        },
                                        onIncrement = { qty ->
                                            tableOrderViewModel.onEvent(TableOrderEvent.OnIncrementQuantity(it,qty))
                                        }
                                    )
                                }
                            }
                    } else {
                        Text(text = "Empty order item...", modifier = Modifier.align(Alignment.Center))
                    }
                    if (loading){
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    if (finished){
                        LinearProgressAnimated(5000,
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)){ finished ->
                            if (finished) popUp()
                        }
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemRow(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem: (String) -> ModifierItem?,
    onDelete: () -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit
) {
    val food = remember {
        getFood(orderItem.foodId)
    }
    food?:return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
//
//                placeholder
            Row(
                Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoilImage(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    imageModel = { food.imageUri ?: R.mipmap.ic_launcher },
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
//                Image(
//                    imageVector = Icons.Default.Panorama, contentDescription = null,
//                    modifier = Modifier
//                        .size(150.dp)
//                        .clip(RoundedCornerShape(10.dp))
//                )
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = food.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.basicMarquee()
                    )
                    if (orderItem.modifierItems?.isNotEmpty() == true) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            orderItem.modifierItems.forEach {
                                Text(
                                    text = getModifier(it.key)?.name ?: "",
                                    fontWeight = FontWeight.SemiBold
                                )
                                it.value.forEach { item ->
                                    Text(
                                        text = getModifierItem(item)?.name ?: "",
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }
//                orderItem.modifierItems?.forEach {
//                    Text(text = getModifier(it.key).name)
//                    it.value.forEach { item ->
//                        Text(
//                            text = getModifierItem(item).name,
//                            modifier = Modifier.padding(horizontal = 8.dp)
//                        )
//                    }
//                }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onIncrement(orderItem.quantity + 1) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${orderItem.quantity}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(4.dp)
                )
                IconButton(
                    onClick = { if (orderItem.quantity > 1) onDecrement(orderItem.quantity - 1)},
                    modifier = Modifier.size(32.dp),
                    enabled = orderItem.quantity > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
//            }


        }

    }
}

@Preview(device = "spec:width=411dp,height=891dp,dpi=640", showBackground = true)
@Composable
fun RowPreview() {
    OrderItemRow(
        orderItem = OrderItem(
            modifierItems = mapOf(
                "sbjk" to listOf("ds", "sdb", "jdhs"),
                "dskj" to listOf("dsjn,wds", "jwk", "jd")
            ),
            quantity = 10
        ),
        getFood = { Food(name = "ABCevsaesvervesav erfae efrv") },
        getModifier = { com.example.fyp.menucreator.data.model.Modifier(name = "Sauce") },
        getModifierItem = { ModifierItem(name = "Less") },
        onIncrement = {},
        onDecrement = {},
        onDelete = {}
    )
}

