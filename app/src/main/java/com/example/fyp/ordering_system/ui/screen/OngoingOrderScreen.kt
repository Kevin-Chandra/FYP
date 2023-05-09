package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.OngoingOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingOrderScreen(
    navigator: NavController,
    id : String,
    viewModel: OngoingOrderViewModel = hiltViewModel()
) {
    val statusState = viewModel.orderingStatusState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true){
        viewModel.getOrderStatus(id)
    }

    FypTheme() {
        Scaffold() {
            Box(Modifier.fillMaxSize().padding(it)) {
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
                        }
                    )
                }
                if (statusState.value.success){
                    when (statusState.value.status){
                        "Processing" -> {
                            Text(
                                text = "Order is being processed!",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        "Rejected" -> {
                            Text(
                                text = "Unfortunately, the restaurant is busy",
                                modifier = Modifier.align(Alignment.Center)
                            )
                            AlertDialog(
                                onDismissRequest = {  },
                                title = {
                                    Text(text = "Restaurant is busy!")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
//                                        navigator.popBackStack(Screen.ReviewOrderScreen.route,true)
                                        navigator.navigateUp()
                                    }
                                    ) {
                                        Text(text = "Ok")
                                    }
                                }
                            )
                        }
                        "Confirmed" -> {
                            Text(
                                text = "Restaurant is preparing your order!",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        "Preparing" -> {
                            Text(
                                text = "Restaurant is preparing your order!",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        "Finished" -> {
                            Text(
                                text = "Order is finished!",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

    }
}

data class OngoingOrderScreenState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val status: String? = null,
    val errorMessage : String? = null
)
