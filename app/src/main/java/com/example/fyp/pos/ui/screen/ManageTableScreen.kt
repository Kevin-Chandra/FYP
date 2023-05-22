package com.example.fyp.pos.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.formatTime
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.model.TableStatus.*
import com.example.fyp.pos.ui.theme.FYPTheme
import com.example.fyp.pos.ui.viewmodel.IncomingOrderItemViewModel
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import com.example.fyp.pos.util.KitchenManageOrderItemEvent
import com.example.fyp.pos.util.ManageTableEvent
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTableScreen(
    navigator : NavController,
    viewModel: ManageTableViewModel
) {
    val tables = viewModel.tables.collectAsStateWithLifecycle()

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var clickedTable = remember {
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
                    TableDialog(showDialog = clickedTable) {
                        clickedTable.value = null
                    }
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

@Composable
fun TableDialog(
    showDialog: MutableState<Table?>,
    onClose: () -> Unit
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
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(text = "Table Number: ${table.tableNumber}")
                    Text(text = "Status: ${table.tableStatus}")
                    Button(onClick = {  }) {
                        Text(text = "Add Order")
                    }
                    Button(onClick = {  }) {
                        Text(text = "Checkout")
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

@Preview(showBackground = true)
@Composable
fun TableDialogPreview() {
    TableDialog(showDialog = mutableStateOf(Table(tableNumber = "2", tableStatus = Occupied))) {
        
    }
}