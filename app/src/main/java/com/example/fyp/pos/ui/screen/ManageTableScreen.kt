package com.example.fyp.pos.ui.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.LinearProgressAnimated
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.model.TableStatus.*
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.theme.FYPTheme
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import com.example.fyp.pos.ui.viewmodel.TableOngoingOrderViewModel
import com.example.fyp.pos.util.ManageTableEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTableScreen(
    navigator : NavController,
    viewModel: ManageTableViewModel,
    tableOngoingOrderViewModel: TableOngoingOrderViewModel,
    productViewModel: ProductViewModel
) {
    val tables = viewModel.tables.collectAsStateWithLifecycle()

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val clickedTable = remember {
        mutableStateOf<Table?>(null)
    }

    FYPTheme() {
        Surface() {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
            ) {
                Box(modifier = Modifier.padding(it)){
                    if (showDialog){
                        AddTableDialog( onClick = { it1 ->
                            showDialog = false
                            viewModel.onEvent(ManageTableEvent.OnAddTable(it1))
                        },
                        onDismiss = { bool ->
                            showDialog = bool
                        })
                    }
                    TableDialog(showDialog = clickedTable,
                        onAddOrderClick = {
                            navigator.navigate(PosScreen.PosTableOrderGraph.passId(clickedTable.value!!.id))
//                            navigator.navigate(PosScreen.PosTableOrderGraph.route)
                        },
                        onClose = {
                            clickedTable.value = null
                        },
                        getFood = { id ->
                            productViewModel.getFood(id)
                        },
                        getOngoingOrder = {
                            clickedTable.value?.currentOrder?.let { it1 ->
                                tableOngoingOrderViewModel.getOrder(
                                    it1
                                )
                            }
                        },
                        getOrderItem = { id ->
                            tableOngoingOrderViewModel.getOrderItem(id)
                        },
                        onCheckout = { id ->
                            navigator.navigate(PosScreen.PosCheckoutScreen.withRequiredArgs(id,clickedTable.value?.id ?: ""))
                        },
                        onFinishTable = {
                            viewModel.onEvent(ManageTableEvent.OnFinishTable(clickedTable.value?.id ?: ""))
                            clickedTable.value = null
                        }
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(200.dp),
                        modifier = Modifier.align(Alignment.TopCenter)){
                        items(tables.value){ table ->
                            PosTable(
                                table = table,
                                onClick = {
                                    clickedTable.value = table
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTableDialog(
    onClick: (Table) -> Unit,
    onDismiss: (Boolean) -> Unit
) {
    var tableName by rememberSaveable{
        mutableStateOf("")
    }
    Dialog(
        onDismissRequest = { onDismiss(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column() {
            OutlinedTextField(value = tableName, onValueChange = {
                tableName = it
            })
            Button(onClick = {
                onClick(Table(tableNumber = tableName))
            }){
                Text(text = "Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TableDialog(
    showDialog: MutableState<Table?>,
    onClose: () -> Unit,
    onAddOrderClick: () -> Unit,
    getOngoingOrder: () -> Order?,
    getOrderItem: (String) -> OrderItem?,
    getFood: (String) -> Food?,
    onCheckout: (String) -> Unit,
    onFinishTable: () -> Unit,
) {
    if (showDialog.value != null){
        val table = showDialog.value!!
        Dialog(onDismissRequest = { onClose() } ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "Table Number: ${table.tableNumber}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(
                            onClick = { onClose() },
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(text = "${table.tableStatus}")
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )


                    if (table.tableStatus in ( listOf(
//                            Occupied,
                            Ongoing,
                            Finished
                    ))){
                        val currentOrder = getOngoingOrder()
                        currentOrder?.let {
                            Text(text = "Current Order")
                            Text(
                                text = currentOrder.orderId,
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1
                            )
                            Divider(Modifier.padding(vertical = 4.dp))
                            LazyColumn(Modifier.heightIn(min = 0.dp, max = 250.dp)){
                                items(currentOrder.orderList){
                                    val orderItem = getOrderItem(it) ?: return@items
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
                                        Text(text = getFood(orderItem.foodId)?.name ?: "")
                                        AssistChip(onClick = {}, label = {
                                            Text(text = orderItem.orderItemStatus.name)
                                        })
                                    }
                                }
                            }
                            Divider(Modifier.padding(vertical = 8.dp))
                        }
                    }

                    when(table.tableStatus){
                        Vacant,Occupied -> {
                            Button(onClick = onAddOrderClick) {
                                Text(text = "Add Order")
                            }
                        }
                        Ongoing -> {
                            Button(onClick = onAddOrderClick) {
                                Text(text = "Add Order")
                            }
                            Button(
                                onClick = { if (!table.currentOrder.isNullOrEmpty()) onCheckout(table.currentOrder) },
                                enabled = !table.currentOrder.isNullOrEmpty()
                            ) {
                                Text(text = "Checkout")
                            }
                        }
                        Finished -> {
                            Button(onClick = onFinishTable) {
                                Text(text = "Finish Table")
                            }
                        }
                        Unavailable -> {}
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTable(
    table: Table,
    onClick: () -> Unit
) {
    val icon = when(table.tableStatus){
        Vacant -> Icons.Filled.Check
        Occupied -> Icons.Filled.Groups
        Ongoing -> Icons.Filled.RestaurantMenu
        Finished -> Icons.Filled.CheckCircle
        Unavailable -> Icons.Filled.Close
    }
    Column(
        modifier = Modifier
            .width(125.dp)
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ElevatedAssistChip(
        onClick = {},
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Group, contentDescription = null)
        },
        label = {
            Text(
                text = table.pax.toString(),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        })
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(100.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(text = table.tableNumber,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
//            .padding(8.dp)
        )
    }

}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun TablePreview() {
    PosTable(table = Table(tableStatus = Ongoing, pax = 2, tableNumber = "21") ) {

    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun TableDialogPreview() {
    TableDialog(
        showDialog = mutableStateOf(Table(tableNumber = "2", tableStatus = Ongoing, currentOrder = "jdshbcksnkdsnndabkj")),
        onAddOrderClick = {},
        onClose = {},
        getOngoingOrder = {
            Order(orderList = listOf("a","b"))
        },
        getOrderItem = {
            OrderItem(foodId = "kjd", orderItemStatus = OrderItemStatus.Preparing)
        },
        getFood = {
            Food(name = "ABc")
        },
        onCheckout = {},
        onFinishTable = {}
    )
}