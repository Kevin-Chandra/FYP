package com.example.fyp.ordering_system.ui.screen

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
import com.example.compose.FypTheme
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel

@Composable
fun MainOnlineOrderingScreen(
    navigator: NavController,
) {
    FypTheme() {
        Surface() {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    navigator.navigate(Screen.ManageOrderScreen.withArgs("Incoming"))
                }) {
                    Text(text = "Incoming Order")
                }
                Button(onClick = {
                    navigator.navigate(Screen.ManageOrderScreen.withArgs("Ongoing"))
                }) {
                    Text(text = "Ongoing Order")
                }
            }
        }
    }
}