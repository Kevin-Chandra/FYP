package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.theme.FYPTheme

@Composable
fun MainPosScreen(
    navigator : NavController
) {
    FYPTheme() {
        Surface() {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
        }
    }
}