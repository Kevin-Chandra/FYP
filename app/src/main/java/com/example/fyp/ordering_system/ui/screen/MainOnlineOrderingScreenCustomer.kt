package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.TabItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainOnlineOrderingScreenCustomer(
    navigator: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    ongoingOrderViewModel: OngoingOrderViewModel,
    account: Account
) {
    val tabs = listOf(
        TabItem(
            title = "Order",
            icon = R.drawable.ic_add_cart,
            screen = { ViewProductListScreen(navigator = navigator, productViewModel = productViewModel, cartViewModel = cartViewModel) }
        ),
        TabItem(
            title = "Ongoing",
            icon = R.drawable.ic_order_approve,
            screen = { OngoingOrderListScreen(navigator = navigator, account = account, ongoingOrderViewModel = ongoingOrderViewModel) }
        ),
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    FypTheme() {
        Surface {
            Scaffold(
            ) {
                Column(modifier = Modifier.padding(it)) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                    ) {
                        tabs.forEachIndexed { index, item ->
                            Tab(
                                selected = index == pagerState.currentPage,
                                text = { Text(text = item.title) },
                                icon = { Icon(painterResource(id = item.icon), item.title) },
                                modifier = Modifier,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            index
                                        )
                                    }
                                },
                            )
                        }
                    }
                    HorizontalPager(
                        pageCount = tabs.size,
                        state = pagerState
                    ) {
                        tabs[pagerState.currentPage].screen()
                    }
                }
            }
        }
    }
}