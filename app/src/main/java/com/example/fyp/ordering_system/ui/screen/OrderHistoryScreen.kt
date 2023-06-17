package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fyp.theme.FypTheme
import com.example.fyp.R
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.ui.components.CustomerOrderBottomNavigation
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.util.formatDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navigator: NavController,
    accountId : String,
    viewModel: OngoingOrderViewModel
) {

    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.value + delta
                bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                return Offset.Zero
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(key1 = true){
        viewModel.getPastOrderList(accountId)
    }
    val statusState = viewModel.pastOrderState.collectAsStateWithLifecycle()
    val pastOrders = viewModel.pastOrderList.collectAsStateWithLifecycle()

    FypTheme() {
        Scaffold(
            bottomBar = {
                CustomerOrderBottomNavigation(
                    navController = navigator,
                    modifier = Modifier
                        .height(bottomBarHeight)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = -bottomBarOffsetHeightPx.value.roundToInt()
                            )
                        })
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Order History",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).nestedScroll(nestedScrollConnection)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(it)) {
                if (statusState.value.loading){
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                if (statusState.value.errorMessage != null){
                    AlertDialog(
                        onDismissRequest = {  },
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
                if (statusState.value.success){
                    if (pastOrders.value.isEmpty()){
                        val composition by rememberLottieComposition(
                            spec = LottieCompositionSpec.RawRes(R.raw.no_order_history)
                        )
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
                                text = "No order history in this account...",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    } else {
                        LazyColumn {
                            items(pastOrders.value.sortedByDescending { it1 -> it1.orderFinishTime }){ item ->
                                PastOrderRow(order = item){
                                    navigator.navigate(Screen.OrderHistoryDetailedScreen.withArgs(item.orderId))
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PastOrderRow(
    order: Order,
    onClick:() -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = order.orderId,modifier = Modifier.fillMaxWidth(0.75f).basicMarquee())
            Text(
                text = order.grandTotal.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatDate(order.orderFinishTime))
            AssistChip(
                onClick = {},
                label = {
                    Text(text = order.orderStatus.name)
                }
            )
        }
        Divider()
    }
}
