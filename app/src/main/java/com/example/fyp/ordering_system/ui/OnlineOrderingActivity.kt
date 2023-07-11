package com.example.fyp.ordering_system.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.ordering_system.ui.navigation.OnlineOrderingNavGraph
import com.example.fyp.ordering_system.ui.navigation.SetupOnlineOrderingNavGraph
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlineOrderingActivity : ComponentActivity() {

    lateinit var navController: NavHostController

    lateinit var account: Account
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            val productViewModel = hiltViewModel<ProductViewModel>()
            val authViewModel = hiltViewModel<MainAuthViewModel>()

            var loading by remember { mutableStateOf(true) }

            authViewModel.getSession {
                if (it != null) {
                    account = it
                    loading = false
                }
            }

            if (!loading) {
                if (account.accountType == AccountType.Customer) {
                    val cartViewModel = hiltViewModel<CartViewModel>()
                    val ongoingOrderViewModel = hiltViewModel<OngoingOrderViewModel>()
                    SetupOnlineOrderingNavGraph(
                        navController = navController,
                        productViewModel = productViewModel,
                        cartViewModel = cartViewModel,
                        account = account,
                        ongoingOrderViewModel = ongoingOrderViewModel
                    )
                } else {
                    val incomingOrderViewModel: IncomingOrderViewModel = hiltViewModel()
                    val accountViewModel: AccountViewModel = hiltViewModel()

                    OnlineOrderingNavGraph(
                        navController = navController,
                        incomingOrderViewModel,
                        productViewModel,
                        accountViewModel,
                        authViewModel
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

}