package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.AccountType.*
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.data.model.StaffPosition.*
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.theme.FYPTheme

@Composable
fun MainPosScreen(
    navigator : NavController,
    authViewModel: MainAuthViewModel
) {

    val account  = remember {
        mutableStateOf<Account?>(null)
    }

    LaunchedEffect(key1 = true){
        authViewModel.getSession{
            account.value = it
        }
    }

    FYPTheme() {
        Surface() {
            Box(modifier = Modifier.fillMaxSize()){
                if (account.value == null){
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when(account.value!!.accountType){
                            Customer -> {
                                //Go back!
                            }
                            Admin,Manager -> {
                                Button(onClick = { navigator.navigate(PosScreen.ManageTableScreen.route) }) {
                                    Text(text = "Manage Table")
                                }
                                Button(onClick = { navigator.navigate(PosScreen.KitchenManageOrderItemScreen.route) }) {
                                    Text(text = "Kitchen Manage Incoming Order")
                                }
                                Button(onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) }) {
                                    Text(text = "Manage Order")
                                }
                                Button(onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) }) {
                                    Text(text = "Order History")
                                }
                            }
                            Staff -> {
                                when (account.value!!.staffPosition){
                                    Disabled,Pending,null -> {}
                                    Regular -> {
                                        Button(onClick = { navigator.navigate(PosScreen.ManageTableScreen.route) }) {
                                            Text(text = "Manage Table")
                                        }
                                        Button(onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) }) {
                                            Text(text = "Manage Order")
                                        }
                                        Button(onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) }) {
                                            Text(text = "Order History")
                                        }
                                    }
                                    Kitchen -> {
                                        Button(onClick = { navigator.navigate(PosScreen.KitchenManageOrderItemScreen.route) }) {
                                            Text(text = "Kitchen Manage Incoming Order")
                                        }
                                        Button(onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) }) {
                                            Text(text = "Manage Order")
                                        }
                                        Button(onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) }) {
                                            Text(text = "Order History")
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