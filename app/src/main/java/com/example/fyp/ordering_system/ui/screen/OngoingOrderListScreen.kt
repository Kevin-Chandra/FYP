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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.ui.components.CustomScaffold
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.util.errorToast
import com.example.fyp.ordering_system.util.formatDate
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingOrderListScreen(
    navigator: NavController,
    ongoingOrderViewModel: OngoingOrderViewModel,
    account: Account
) {
    val orderList = ongoingOrderViewModel.ongoingOrderList.collectAsStateWithLifecycle()
    val uiState = ongoingOrderViewModel.ongoingOrderListStatusState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        ongoingOrderViewModel.getOngoingOrderList(account.id)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    FypTheme {
        Surface {
            CustomScaffold(
                navigator = navigator,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Ongoing Order",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                content = { it, _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        if (uiState.value.success) {
                            if (orderList.value.isEmpty()) {
                                val composition by rememberLottieComposition(
                                    spec = LottieCompositionSpec.RawRes(
                                        R.raw.order_food
                                    )
                                )
                                val animProgress by animateLottieCompositionAsState(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever
                                )

                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { animProgress },
                                        modifier = Modifier
                                            .width(400.dp)
                                            .height(350.dp)
                                    )
                                    Text(
                                        text = "No ongoing order now...",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                    )
                                    AssistChip(
                                        modifier = Modifier.padding(16.dp),
                                        onClick = { navigator.navigate(Screen.ProductListScreen.route) },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowBack,
                                                    contentDescription = null
                                                )
                                                Text(
                                                    text = "Order now",
                                                    modifier = Modifier.padding(horizontal = 8.dp),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        }
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.align(Alignment.TopCenter)
                                ) {
                                    items(
                                        orderList.value
                                    ) { item ->
                                        OngoingOrderCard(order = item) {
                                            navigator.navigate(
                                                Screen.OngoingOrderScreen.withArgs(
                                                    item.orderId
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (uiState.value.loading) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                        uiState.value.errorMessage?.let { error ->
                            errorToast(error, context)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OngoingOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            }
    ) {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Text(
                text = "Order Time: ${formatDate(order.orderStartTime)}",
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderId,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .basicMarquee()
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = order.orderStatus.name,
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun OngoingOrderCardPreview() {
    OngoingOrderCard(
        order = Order(orderId = "WNDwnlwsilik", orderStartTime = Date(), orderStatus = OrderStatus.Sent),
        onClick = {}
    )
}

