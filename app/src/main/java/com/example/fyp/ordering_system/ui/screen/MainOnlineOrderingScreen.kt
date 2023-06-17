package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.ordering_system.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
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

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navigator.navigate(Screen.ManageOrderScreen.withArgs("Incoming"))
                        },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .align(Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = "Incoming Order",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = { navigator.navigate(Screen.ManageOrderScreen.withArgs("Incoming")) }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navigator.navigate(Screen.ManageOrderScreen.withArgs("Ongoing"))
                        },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .align(Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = "Ongoing Order",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = { navigator.navigate(Screen.ManageOrderScreen.withArgs("Ongoing")) }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ScreenPreview() {
    MainOnlineOrderingScreen(navigator = rememberNavController())
}