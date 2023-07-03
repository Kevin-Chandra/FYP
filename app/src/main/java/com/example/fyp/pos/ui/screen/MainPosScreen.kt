package com.example.fyp.pos.ui.screen

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dining
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType.Admin
import com.example.fyp.account_management.data.model.AccountType.Customer
import com.example.fyp.account_management.data.model.AccountType.Manager
import com.example.fyp.account_management.data.model.AccountType.Staff
import com.example.fyp.account_management.data.model.StaffPosition.Disabled
import com.example.fyp.account_management.data.model.StaffPosition.Kitchen
import com.example.fyp.account_management.data.model.StaffPosition.Pending
import com.example.fyp.account_management.data.model.StaffPosition.Regular
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.ordering_system.ui.components.DefaultTopBar
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.theme.FypTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPosScreen(
    navigator : NavController,
    authViewModel: MainAuthViewModel
) {

    val account  = remember {
        mutableStateOf<Account?>(null)
    }

    val context = LocalContext.current

    val primaryCardColor = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
    val secondaryCardColor = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
    val tertiaryCardColor = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)

    LaunchedEffect(key1 = true){
        authViewModel.getSession{
            account.value = it
        }
    }

    FypTheme {
        Surface {
            Scaffold(topBar = {
                DefaultTopBar(title = "Point of Sales", navigateBack = { (context as? Activity)?.finish() })
            }) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(it)){
                    if (account.value == null){
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when(account.value!!.accountType){
                                Customer -> {
                                    //Go back!
                                }
                                Admin,Manager -> {
                                    PosButton(
                                        title = "Manage Table",
                                        onClick = { navigator.navigate(PosScreen.ManageTableScreen.route) },
                                        icon = Icons.Outlined.PointOfSale,
                                        colors = primaryCardColor
                                    )

                                    PosButton(
                                        title = "Kitchen Manage Order",
                                        onClick = { navigator.navigate(PosScreen.KitchenManageOrderItemScreen.route) },
                                        icon = Icons.Outlined.Dining,
                                        colors = secondaryCardColor
                                    )

                                    PosButton(
                                        title = "View Order",
                                        onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) },
                                        icon = Icons.Outlined.ReceiptLong,
                                        colors = secondaryCardColor
                                    )

                                    PosButton(
                                        title = "Order History",
                                        onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) },
                                        icon = Icons.Outlined.History,
                                        colors = tertiaryCardColor
                                    )
                                }
                                Staff -> {
                                    when (account.value!!.staffPosition){
                                        Disabled,Pending,null -> {}
                                        Regular -> {
                                            PosButton(
                                                title = "Manage Table",
                                                onClick = { navigator.navigate(PosScreen.ManageTableScreen.route) },
                                                icon = Icons.Outlined.PointOfSale,
                                                colors = primaryCardColor
                                            )
                                            PosButton(
                                                title = "View Order",
                                                onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) },
                                                icon = Icons.Outlined.ReceiptLong,
                                                colors = secondaryCardColor
                                            )
                                            PosButton(
                                                title = "Order History",
                                                onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) },
                                                icon = Icons.Outlined.History,
                                                colors = tertiaryCardColor
                                            )
                                        }
                                        Kitchen -> {
                                            PosButton(
                                                title = "Kitchen Manage Order",
                                                onClick = { navigator.navigate(PosScreen.KitchenManageOrderItemScreen.route) },
                                                icon = Icons.Outlined.Dining,
                                                colors = primaryCardColor
                                            )
                                            PosButton(
                                                title = "View Order",
                                                onClick = { navigator.navigate(PosScreen.ManageOrderScreen.route) },
                                                icon = Icons.Outlined.ReceiptLong,
                                                colors = secondaryCardColor
                                            )
                                            PosButton(
                                                title = "Order History",
                                                onClick = { navigator.navigate(PosScreen.OrderHistoryRootGraph.route) },
                                                icon = Icons.Outlined.History,
                                                colors = tertiaryCardColor
                                            )
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

@Composable
fun PosButton(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector,
    colors: CardColors = CardDefaults.cardColors()
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onClick()
            },
        colors = colors
    ) {
        Column(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview
@Composable
fun PosButtonPreview() {
    PosButton(
        title = "Manage Table",
        onClick = { },
        icon = Icons.Outlined.PointOfSale,
    )
}