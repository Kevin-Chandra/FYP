package com.example.fyp.pos.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.ui.component.CustomAlertDialog
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import com.example.fyp.pos.util.AddEditTableEvent
import com.example.fyp.pos.util.ManageTableEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSettingScreen(
    navigator: NavController,
    manageTableViewModel: ManageTableViewModel,
) {

    val tables = manageTableViewModel.tables.collectAsStateWithLifecycle()

    val addEditTableState = manageTableViewModel.addEditTableState.collectAsStateWithLifecycle()

    val clickedTable = remember {
        mutableStateOf<Table?>(null)
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showTableOverview by remember { mutableStateOf(false) }
    var showEditTable by remember { mutableStateOf(false) }
    var showAddTable by remember { mutableStateOf(true) }

    var showDeleteAlertDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = addEditTableState.value){
        addEditTableState.value.error?.let{
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
        }
        if (addEditTableState.value.success){
            showBottomSheet = false
            snackbarHostState.showSnackbar(addEditTableState.value.successMessage ?: "", withDismissAction = true, duration = SnackbarDuration.Short)
            manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnReset)
        }
    }

    FypTheme {
        Surface{
            Scaffold(
                floatingActionButton = {
                        FloatingActionButton(onClick = {
                            showBottomSheet = true
                            showAddTable = true
                            showEditTable = false
                            showTableOverview = false
                            manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnReset)
                        }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Table")
                        }
                },
                floatingActionButtonPosition = FabPosition.End,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Tables Settings")
                        },
                        navigationIcon = {
                             IconButton(onClick = { navigator.navigateUp() }) {
                                 Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate Up")
                             }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ){
                Box(modifier = Modifier.padding(it)){

                    if (showDeleteAlertDialog){
                        CustomAlertDialog(
                            onDismiss = { showDeleteAlertDialog = false },
                            onConfirm = {
                                manageTableViewModel.onEvent(ManageTableEvent.OnDeleteTable(clickedTable.value!!.id))
                                showBottomSheet = false
                                clickedTable.value = null
                                showDeleteAlertDialog = false
                            },
                            confirmButtonText = "Delete",
                            dismissButtonText = "Cancel",
                            title = "Delete this table?",
                            text = "Do you want to delete table number [${clickedTable.value?.tableNumber}]? This action cannot be undone!"
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.align(Alignment.TopCenter)){
                        items(tables.value){ table ->
                            PosTable(
                                table = table,
                                settingMode = true,
                                onClick = {
                                    Log.i("GGG", "ManageTableScreen: $table")
                                    clickedTable.value = table
                                    showTableOverview = true
                                    showBottomSheet = true
                                }
                            )

                            if (clickedTable.value?.id == table.id){
                                clickedTable.value = table
                            }
                        }
                    }

                    TableSettingBottomSheet(
                        showSheet = showBottomSheet,
                        onClose = {
                            showBottomSheet = false
                            clickedTable.value = null
                        }
                    ) {
                        if (showTableOverview && (clickedTable.value != null)){
                            TableOverview(
                                table = clickedTable.value,
                                onDelete = {
                                    showDeleteAlertDialog = true
                                },
                                onSave = { it1 ->
                                    manageTableViewModel.onEvent(ManageTableEvent.OnSetTableAvailability(clickedTable.value!!.id,it1))
                                    showBottomSheet = false
                                    clickedTable.value = null
                                },
                                onEdit = {
                                    showTableOverview = false
                                    showEditTable = true
                                    manageTableViewModel.editTable(clickedTable.value!!)
                                }
                            )
                        } else {
                            AddEditTableBottomSheetContent(
                                showLoading = addEditTableState.value.loading,
                                tableNumber = addEditTableState.value.tableNumber,
                                tableName = addEditTableState.value.tableName,
                                paxCapacity = addEditTableState.value.tablePaxCapacity,
                                isEdit = showEditTable,
                                onTableNumberChanged = { str ->
                                    manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnTableNumberChanged(str))
                                },
                                onTableNameChanged = { str ->
                                    manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnTableNameChanged(str))
                                },
                                onPaxCapacityChanged = { pax ->
                                    manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnPaxCapacityChanged(pax))
                                },
                                onSave = {
                                    manageTableViewModel.onAddEditTableEvent(AddEditTableEvent.OnSave)
                                },
                                tableNumberError = addEditTableState.value.tableNumberError,
                                tableNameError = addEditTableState.value.tableNameError,
                            )
                        }

                    }


                }
            }
        }
    }
}

@Composable
fun TableOverview(
    table: Table?,
    onDelete: () -> Unit,
    onSave: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    if (table != null) {

        val currentAvailability = table.tableStatus != TableStatus.Unavailable
        var available by remember {
            mutableStateOf(currentAvailability)
        }
        val buttonEnabled by remember {
            mutableStateOf(table.tableStatus == TableStatus.Available || table.tableStatus == TableStatus.Unavailable)
        }

        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Table Number: ${table.tableNumber}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    TextButton(
                        onClick = { onSave(available) },
                        enabled = buttonEnabled && available != currentAvailability
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(text = "${table.tableStatus}")
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(text = "Pax Capacity: ${table.paxCapacity}",style = MaterialTheme.typography.titleLarge)

                val interactionSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .clickable(
                            enabled = buttonEnabled,
                            interactionSource = interactionSource,
                            // This is for removing ripple when Row is clicked
                            indication = null,
                            role = Role.Switch,
                            onClick = {
                                available = !available
//                                onSetAvailability(available)
                            }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Text(text = "Availability",style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Switch(
                        enabled = buttonEnabled,
                        checked = available,
                        onCheckedChange = {
                            available = it
                        }
                    )

                }
                Button(
                    onClick = onEdit,
                    enabled = buttonEnabled,
                    modifier = Modifier.padding( vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Edit Table", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onDelete,
                    enabled = buttonEnabled,
                    modifier = Modifier.padding( vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Table", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSettingBottomSheet(
    showSheet: Boolean,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    if (showSheet){
        val modalBottomSheetState = androidx.compose.material3.rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            content = {
                content()
            }
        )
    }


}

@Composable
fun AddEditTableBottomSheetContent(
    showLoading: Boolean,
    tableNumber: String = "",
    tableName: String = "",
    paxCapacity : Int = 1,
    isEdit: Boolean = false,
    onSave: () -> Unit,
    onTableNumberChanged: (String) -> Unit,
    tableNumberError: String?,
    onTableNameChanged: (String) -> Unit,
    tableNameError: String?,
    onPaxCapacityChanged: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isEdit) "Edit Table" else "Add Table",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(
                    onClick = onSave,
                    enabled = !showLoading
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            OutlinedTextField(
                value = tableNumber,
                onValueChange = {
                    onTableNumberChanged(it)
                },
                leadingIcon = {
                      Icon(imageVector = Icons.Default.Tag, contentDescription = null)
                },
                label = {
                    Text(text = "Table Number")
                },
                isError = tableNumberError != null,
                trailingIcon = {
                    if (tableNumberError != null)
                        Icon(Icons.Filled.Error, "error", tint = MaterialTheme.colorScheme.error)
                },
                supportingText = {
                    if (tableNumberError != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = tableNumberError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = tableName,
                onValueChange = {
                    onTableNameChanged(it)
                },
                label = {
                    Text(text = "Table Name")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.TableBar, contentDescription = null)
                },
                isError = tableNameError != null,
                trailingIcon = {
                    if (tableNameError != null)
                        Icon(Icons.Filled.Error, "error", tint = MaterialTheme.colorScheme.error)
                },
                supportingText = {
                    if (tableNameError != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = tableNameError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Pax capacity icon",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                    )
                    Text(
                        text = "Pax Capacity",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(
                        border = BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    IconButton(
                        onClick = {
                            if (paxCapacity > 1) {
                                onPaxCapacityChanged(paxCapacity - 1)
                            }
                        },
                        enabled = paxCapacity > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = paxCapacity.toString(),
//                            fontWeight = FontWeight.,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                    IconButton(
                        onClick = {
                            onPaxCapacityChanged(paxCapacity + 1)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (showLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

}

@Preview
@Composable
fun TableManagementDialogPreview() {
    TableOverview(
        table = Table(paxCapacity = 10, tableStatus = TableStatus.Available),
        onDelete = { },
//        onSetAvailability = {  },
        onSave = {}
    ) {

    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true
)
@Composable
fun TableBottomSheetPreview() {
    Surface {
        AddEditTableBottomSheetContent(
            onTableNumberChanged = {},
            onPaxCapacityChanged = {},
            onTableNameChanged = {},
//        onClose = {},
            onSave = {},
//        showSheet = true,
            tableNameError = "aww",
            tableNumberError = "no!",
            showLoading = false
        )

    }
}