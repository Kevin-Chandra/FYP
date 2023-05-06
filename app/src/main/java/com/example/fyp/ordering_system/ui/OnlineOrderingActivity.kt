package com.example.fyp.ordering_system.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.ordering_system.ui.navigation.OnlineOrderingNavGraph
import com.example.fyp.ordering_system.ui.navigation.SetupOnlineOrderingNavGraph
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlineOrderingActivity : ComponentActivity() {

    lateinit var navController: NavHostController

    lateinit var accountType: AccountType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            accountType = extras.getSerializable("accountType") as AccountType
        }
        setContent {
            navController = rememberNavController()
            val productViewModel = hiltViewModel<ProductViewModel>()

            if (accountType == AccountType.Customer){
                val cartViewModel = hiltViewModel<CartViewModel>()
                SetupOnlineOrderingNavGraph(
                    navController = navController,
                    productViewModel = productViewModel,
                    cartViewModel = cartViewModel
                )
            } else {
                val incomingOrderViewModel: IncomingOrderViewModel = hiltViewModel()

                OnlineOrderingNavGraph(navController = navController,incomingOrderViewModel,productViewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FypTheme {
        Greeting(name = "KAk")
    }
}