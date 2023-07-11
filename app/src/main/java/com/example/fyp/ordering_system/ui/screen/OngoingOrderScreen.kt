package com.example.fyp.ordering_system.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fyp.R
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus.*
import com.example.fyp.ordering_system.ui.components.DefaultTopBar
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.successToast
import com.example.fyp.theme.FypTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingOrderScreen(
    navigator: NavController,
    id : String,
    viewModel: OngoingOrderViewModel,
    productViewModel: ProductViewModel
) {
    LaunchedEffect(key1 = true){
        viewModel.getOrderStatus(id)
        viewModel.getOrderItemList(id)
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val statusState = viewModel.orderingStatusState.collectAsStateWithLifecycle()
    val currentOrder = viewModel.currentOrder.collectAsStateWithLifecycle()
    val currentOrderItem = viewModel.currentOrderItem.collectAsStateWithLifecycle()

    val currentAnim = remember {
        mutableIntStateOf(R.raw.order_sent_anim)
    }
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(currentAnim.value))
    val animProgress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever )

    FypTheme {
        Scaffold(
            topBar = {
                DefaultTopBar(
                    title = "",
                    navigateBack = {
                        navigator.navigateUp()
                    })
            }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (statusState.value.loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                if (statusState.value.errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { },
                        confirmButton = {
                            TextButton(onClick = { navigator.navigateUp() }) {
                                Text(text = "Ok")
                            }
                        },
                        title = {
                            Text(text = "Error!")
                        },
                        text = {
                            Text(text = statusState.value.errorMessage!!)
                        }
                    )
                }
                var message by remember { mutableStateOf("") }
                if (statusState.value.success) {
                    if (currentOrder.value is Response.Success) {
                            when ((currentOrder.value as Response.Success<Order>).data.orderStatus) {
                                Sent -> {
                                    currentAnim.value = R.raw.order_sent_anim
                                    message = "Order is being processed!"
                                }
                                Rejected -> {
                                    message = "Unfortunately, the restaurant is busy"
                                }
                                Confirmed, Ongoing -> {
                                    currentAnim.value = R.raw.order_preparing_anim
                                    message = "Restaurant is preparing your order!"
                                }
                                Finished -> {
                                    currentAnim.value = R.raw.order_completed_anim
                                    message = "Order is finished!"
                                }
                            }
                        if ((currentOrder.value as Response.Success<Order>).data.orderStatus == Rejected) {
                            AlertDialog(
                                onDismissRequest = { },
                                title = {
                                    Text(text = "Sorry :(")
                                },
                                text = {
                                    Text(text = "Unfortunately, restaurant is busy! Please try again next time...")
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            navigator.navigateUp()
                                        }
                                    ) {
                                        Text(text = "Ok")
                                    }
                                }
                            )
                        }
                    }
                    if (currentOrderItem.value is Response.Success)
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Order ID",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            ElevatedCard(modifier = Modifier.fillMaxWidth(0.75f).padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = (currentOrder.value as Response.Success).data.orderId,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    )
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString((currentOrder.value as Response.Success).data.orderId))
                                        successToast("Order ID copied to clipboard!",context)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Id"
                                        )
                                    }
                                }
                            }
                            LottieAnimation(composition = composition, progress = { animProgress}, modifier = Modifier.height(300.dp) )
                            Text(
                                text = message,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            var expandOrderItem by rememberSaveable {
                                mutableStateOf(false)
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                    .animateContentSize(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                            ) {
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Order",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(onClick = {
                                            expandOrderItem = !expandOrderItem
                                        }) {
                                            Icon(
                                                imageVector = if (expandOrderItem) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    if (expandOrderItem) {
                                        LazyColumn{
                                            item {
                                                Divider()
                                            }
                                            items((currentOrderItem.value as Response.Success<List<OrderItem>>).data) { item ->
                                                ViewOrderItem(orderItem = item,
                                                    getFood = { id ->
                                                        productViewModel.getFood(id)
                                                    },
                                                    getModifierItem = { id ->
                                                        productViewModel.getModifierItem(id)
                                                    },
                                                    getModifier = { id ->
                                                        productViewModel.getModifier(id)
                                                    }
                                                )
                                            }

                                        }
                                    }


                                }
                            }


                        }
                }
            }
        }

    }
}

@Composable
fun ViewOrderItem(
    orderItem: OrderItem,
    getFood: (String) -> Food?,
    getModifier: (String) -> com.example.fyp.menucreator.data.model.Modifier?,
    getModifierItem:(String) -> ModifierItem?
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${orderItem.quantity}x",
                modifier = Modifier.padding(end = 16.dp),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.headlineSmall
            )
            val food = getFood(orderItem.foodId)?:return
            Column {
                Text(text = food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                orderItem.modifierItems?.forEach {
                    val modifier = getModifier(it.key)
                    if (modifier != null) {
                        Text(text = modifier.name)
                    }
                    it.value.forEach { it1 ->
                        val modifierItem = getModifierItem(it1)
                        modifierItem?.name?.let { it2 -> Text(text = it2, modifier = Modifier.padding(horizontal = 8.dp)) }
                    }
                }
            }
        }
        AssistChip(
            onClick = {  },
            label = {
                Text(text = orderItem.orderItemStatus.name)
            }
        )
    }
}

data class OngoingOrderScreenState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val successMessage : String? = null,
    val status: String? = null,
    val errorMessage : String? = null
)
