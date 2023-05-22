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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.menucreator.ui.viewmodel.ModifierListingViewModel_Factory
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.ui.components.CustomerOrderBottomNavigation
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.util.errorToast
import com.example.fyp.ordering_system.util.formatDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.roundToInt

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

    LaunchedEffect(key1 = true){
        ongoingOrderViewModel.getOngoingOrderList(account.id)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    FypTheme() {
        Surface() {
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
                                text = "Ongoing Order",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).nestedScroll(nestedScrollConnection)
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(it)) {
                    if (uiState.value.success){
                        if (orderList.value.isEmpty()){
                            Text(
                                text = "No ongoing order now...",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.align(Alignment.TopCenter)
                        ){
                            items(
                                orderList.value
                            ) { item ->
                                OngoingOrderCard(order = item) {
                                    navigator.navigate(Screen.OngoingOrderScreen.withArgs(item.orderId))
                                }
                            }
                        }
                    }
                    if (uiState.value.loading){
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    uiState.value.errorMessage?.let { error ->
                        errorToast(error,context)
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OngoingOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable {
                onClick()
            }
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = "Order Time: ${formatDate(order.orderStartTime)}")
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
                    modifier = Modifier.fillMaxWidth(0.7f).basicMarquee()
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

