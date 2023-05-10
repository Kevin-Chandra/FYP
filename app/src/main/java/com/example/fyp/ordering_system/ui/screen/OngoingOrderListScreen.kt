package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
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

    LaunchedEffect(key1 = true){
        ongoingOrderViewModel.getOngoingOrderList(account.id)
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                ) {
                    OngoingOrderCard(order = it) {
                        navigator.navigate(Screen.OngoingOrderScreen.withArgs(it.orderId))
                    }
                }
            }
        }
        if (uiState.value.loading){
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        uiState.value.errorMessage?.let {
            errorToast(it,context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable {
            onClick()
        }
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = "Order Time: ${order.orderStartTime}")
            Row(
                modifier = Modifier.align(CenterHorizontally).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderId,
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = order.orderStatus.name,
                        )
                    }
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

