package com.example.fyp.pos.ui.screen

import android.graphics.drawable.Icon
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.util.formatTime
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.viewmodel.PastOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedOrderScreen(
    navigator: NavController,
    pastOrderViewModel: PastOrderViewModel
) {

    val orderList = pastOrderViewModel.pastOrder.collectAsStateWithLifecycle()

    FypTheme() {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Order History")
                        }
                    )
                }
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(it)){

                    if (orderList.value is Response.Success){
                        if ((orderList.value as Response.Success<List<Order>>).data.isEmpty()){
                            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(
                                R.raw.no_order_history_pos))
                            val animProgress by animateLottieCompositionAsState(composition = composition, iterations = 1 )

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
                                    text = "No order history at the moment...",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.align(Alignment.TopCenter)) {
                                items((orderList.value as Response.Success<List<Order>>).data){ item ->
                                    FinishedOrderCard(
                                        onCardClick = {
                                            navigator.navigate(PosScreen.OrderHistoryDetailedScreen.withRequiredArgs(item.orderId))
                                        },
                                        order = item
                                    )
                                }
                            }
                        }
                    }
                    if (orderList.value is Response.Loading){
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinishedOrderCard(
    onCardClick: () -> Unit,
    order: Order,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.9f).padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(order.orderFinishTime),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = order.orderType.name
                            )
                        },
                    )
                }
                Text(
                    text = order.orderId,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            IconButton(
                onClick = onCardClick,
                modifier = Modifier
//                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Icon(imageVector = Icons.Rounded.ChevronRight,contentDescription = null)
            }
        }
    }
}

@Preview
@Composable
fun PastOrderCardPreview() {
    FinishedOrderCard(
        order = Order(orderList = listOf("dsj","dss","fsv","fsj","sjd","dss","fsv","fsj","sjd"), orderId = "ukbdz saduhbsiu anvuifsh uvhduivd fuvhi", orderType = OrderType.Online, tableNumber = "23"),
        onCardClick = {}
    )
}