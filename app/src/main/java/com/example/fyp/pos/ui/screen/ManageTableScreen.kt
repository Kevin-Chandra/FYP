package com.example.fyp.pos.ui.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.R
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus.*
import com.example.fyp.pos.ui.component.CustomAlertDialog
import com.example.fyp.pos.ui.navigation.PosScreen
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

    var showResetDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }

    val clickedTable = remember {
        mutableStateOf<Table?>(null)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    FypTheme {
        Surface {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                navigator.navigateUp()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                navigator.navigate(PosScreen.TableSettingScreen.route)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings, contentDescription = "Table settings"
                                )
                            }
                        },
                        title = {
                            Text(text = "Tables")
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(modifier = Modifier.padding(it)){
//                    if (showDialog){
////                        AddTableDialog( onClick = { it1 ->
////                            showDialog = false
////                            viewModel.onEvent(ManageTableEvent.OnAddTable(it1))
////                        },
////                        onDismiss = { bool ->
////                            showDialog = bool
////                        })
//                        //TODO assign seating
//                    }

                    if (showResetDialog){
                        CustomAlertDialog(
                            onDismiss = { showResetDialog = false },
                            onConfirm = {
                                viewModel.onEvent(ManageTableEvent.OnResetTable(clickedTable.value?.id ?: ""))
                                clickedTable.value = null
                                showResetDialog = false
                            },
                            confirmButtonText = "Reset",
                            dismissButtonText = "Cancel",
                            title = "Reset Table",
                            text = "Reset table will return table state to initial state. Do you wish to reset?"
                        )
                    }
                    AssignTableDialog(
                        showAssignDialog = showAssignDialog,
                        table = clickedTable.value,
                        onDismiss = {
                            showAssignDialog = false
                        },
                        onAssignClick = { pax, label ->
                            viewModel.onEvent(ManageTableEvent.AssignTable(clickedTable.value!!.id,pax.toInt(),label))
                            showAssignDialog = false
                        }
                    )
                    TableDialog(
                        table = clickedTable.value,
                        onAddOrderClick = {
                            navigator.navigate(PosScreen.PosTableOrderGraph.withOptionalArg("tableId" to clickedTable.value!!.id))
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
                            viewModel.onEvent(ManageTableEvent.OnResetTable(clickedTable.value?.id ?: ""))
                            clickedTable.value = null
                        },
                        onAssignClick = {
                            showAssignDialog = true
                        },
                        onResetTable = {
                            showResetDialog = true
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
                            if (clickedTable.value?.id == table.id){
                                clickedTable.value = table
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTableDialog(
    showAssignDialog: Boolean,
    table: Table?,
    onDismiss: (Boolean) -> Unit,
    onAssignClick: (String,String) -> Unit
) {
    var label by remember {
        mutableStateOf("")
    }
    if (showAssignDialog) {
        Dialog(
            onDismissRequest = { onDismiss(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Table Number: ${table?.tableNumber}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    val options = (1..table!!.paxCapacity).map { it.toString() }
                    var expanded by remember { mutableStateOf(false) }
                    var pax by remember { mutableStateOf(options[0]) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = pax,
                            onValueChange = { },
                            label = { Text("Pax") },
                            trailingIcon = {
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = true)
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            modifier = Modifier.menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            options.forEach { selectionOption ->
                                DropdownMenuItem(
                                    onClick = {
                                        pax = selectionOption
                                        expanded = false
                                    },
                                    text = {
                                        Text(text = selectionOption)
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = label,
                        onValueChange = {
                            label = it
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        label = {
                            Text(text = "Label")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Button(
                        onClick = {
                            onAssignClick(pax, label)
                            pax = ""
                            label = ""
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Assign")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableDialog(
    table: Table?,
    onClose: () -> Unit,
    onAddOrderClick: () -> Unit,
    getOngoingOrder: () -> Order?,
    getOrderItem: (String) -> OrderItem?,
    getFood: (String) -> Food?,
    onCheckout: (String) -> Unit,
    onFinishTable: () -> Unit,
    onAssignClick: () -> Unit,
    onResetTable: () -> Unit,
) {
    if (table != null){
        Dialog(onDismissRequest = { onClose() } ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        val name = remember { table.name.ifEmpty { table.tableNumber } }
                        Text(
                            text = "Table: $name",
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

                    if (table.tableStatus != Available && table.tableStatus != Unavailable){
                        Text(
                            text = "Pax : ${table.pax}/${table.paxCapacity}",
                            modifier = Modifier.align(Alignment.Start),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Divider(Modifier.padding(vertical = 8.dp))
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Pax capacity : ${table.paxCapacity}",
                            modifier = Modifier.align(Alignment.Start),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Divider(Modifier.padding(vertical = 8.dp))
                    }

                    if (table.tableStatus in ( listOf(
                            Ongoing,
                            Finished
                    ))){
                        val currentOrder = getOngoingOrder()
                        currentOrder?.let {
                            var totalItem by remember { mutableIntStateOf(0)}
                            Text(
                                text = "Current Order",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.Start),style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = currentOrder.orderId,
                                modifier = Modifier
                                    .basicMarquee()
                                    .align(Alignment.Start),
                                maxLines = 1
                            )
                            Divider(Modifier.padding(vertical = 4.dp))
                            LazyColumn(Modifier.heightIn(min = 0.dp, max = 250.dp)){
                                items(currentOrder.orderList){
                                    val orderItem = getOrderItem(it) ?: return@items
                                    LaunchedEffect(key1 = true){
                                        totalItem += orderItem.quantity
                                    }
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
                                        Text(text = getFood(orderItem.foodId)?.name ?: "", modifier = Modifier.fillMaxWidth(0.5f))
                                        Text(text = "${orderItem.quantity}x")
                                        AssistChip(onClick = {}, label = {
                                            Text(text = orderItem.orderItemStatus.name)
                                        })
                                    }
                                }
                            }
                            Divider(Modifier.padding(vertical = 8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(
                                    text = "Total item",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = totalItem.toString(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Divider(Modifier.padding(vertical = 8.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    when(table.tableStatus){
                        Available -> {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = onAssignClick) {
                                Text(text = "Assign Seat")
                            }
                        }
                        Occupied ->{
                            Button(onClick = onAddOrderClick) {
                                Text(text = "Add Order")
                            }
                            IconButton(
                                onClick = onResetTable,
//                                colors = IconButtonDefaults.iconButtonColors(Color.Red),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_reset),
                                    contentDescription = null,
                                    tint = Color.Red
                                )
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
    settingMode: Boolean = false,
    onClick: () -> Unit,
) {
    val icon = when (table.tableStatus) {
        Available -> Icons.Filled.Check
        Occupied -> Icons.Filled.Groups
        Ongoing -> Icons.Filled.RestaurantMenu
        Finished -> Icons.Filled.CheckCircle
        Unavailable -> Icons.Filled.Close
    }

    Box(
        modifier = Modifier
            .width(125.dp)
            .height(250.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
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
                colors = ButtonDefaults.buttonColors(
                    if (table.tableStatus == Unavailable) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp))
            }
            if (!table.label.isNullOrEmpty() && !settingMode)
                ElevatedAssistChip(
                    onClick = { },
                    label = {
                        Text(text = table.label,)
                    },
//                    modifier = Modifier.padding(bottom = 40.dp)
                )
            if (settingMode && table.name.isNotEmpty()){
                ElevatedAssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "# ${table.tableNumber}",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                )
            }
            if (table.name.isNotEmpty()){
                Text(
                    text = table.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMedium,
                )
            } else {
                Text(
                    text = table.tableNumber.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
//            .padding(8.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun TablePreview() {
    PosTable(table = Table(tableStatus = Unavailable, pax = 2, tableNumber = 21, label = "AHBJJJKSB", name = "G-2"), settingMode = true ) {

    }
}

@Preview
@Composable
fun AssignTablePreview() {
    AssignTableDialog(showAssignDialog = true,table = Table(), onDismiss = {}, onAssignClick = { _, _ -> })
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun TableDialogPreview() {
    TableDialog(
        table = Table(tableNumber = 1, tableStatus = Occupied, currentOrder = "jdshbcksnkdsnndabkj", name = "B1"),
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
        onFinishTable = {},
        onAssignClick = {},
        onResetTable = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TableDialogAvailablePreview() {
    TableDialog(
        table = Table(tableNumber = 2, tableStatus = Ongoing, currentOrder = "jdshbcksnkdsnndabkj"),
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
        onFinishTable = {},
        onAssignClick = {},
        onResetTable = {}
    )
}