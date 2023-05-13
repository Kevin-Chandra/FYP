package com.example.fyp.ordering_system.ui.screen

import android.content.Context
import android.widget.RadioGroup
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PlusOne
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.room.util.appendPlaceholders
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.ui.state.AddToCartUiState
import com.example.fyp.ordering_system.ui.viewmodel.AddToCartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.AddToCartState
import com.example.fyp.ordering_system.util.errorToast
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun AddToCartScreen (
    navigator: NavController,
    productViewModel: ProductViewModel = hiltViewModel(),
    foodId: String,
    orderItemId: String? = null
) {
    val context = LocalContext.current

    val addToCartViewModel = hiltViewModel<AddToCartViewModel>()

    val cartState = addToCartViewModel.addToCartState.collectAsStateWithLifecycle()
    val uiState = addToCartViewModel.addToCartUiState.collectAsStateWithLifecycle(AddToCartUiState())
    val food = productViewModel.getFood(foodId)!!

    var count = 0
    food.modifierList.forEach {
        if (productViewModel.getModifier(it)?.required == true)
            count++
    }
    addToCartViewModel.onEvent(AddToCartEvent.FoodChanged(food,count))

    LaunchedEffect(key1 = true){
        orderItemId?.let {
            addToCartViewModel.initializeItemEdit(it)
        }
    }

    FypTheme() {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    Button(
                        onClick = {
                            addToCartViewModel.onEvent(AddToCartEvent.AddToCart)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(text = if (orderItemId == null) "Add to cart ${cartState.value.price}" else "Update cart ${cartState.value.price}")
                    }
                },
            ) {
                Box(modifier = Modifier.padding(it)) {
                    LaunchedEffect(key1 = uiState.value){
                        if (uiState.value.errorMessage != null) {
                            errorToast(uiState.value.errorMessage ?: "", context)
                        }
                        if (uiState.value.successAdding){
                            navigator.navigateUp()
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState()),
                    ) {
//                        item {
                            CoilImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(250.dp)
                                    .padding(bottom = 16.dp),
                                imageModel = {food.imageUri?:R.mipmap.ic_launcher},
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    contentDescription = "Food Image",
                                    colorFilter = null,
                                ),
                                previewPlaceholder = R.mipmap.ic_launcher,
                                component = rememberImageComponent {
                                    +CircularRevealPlugin(
                                        duration = 800
                                    )
                                },
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Text(
                                    text = food.price.toString(),
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = food.description,
                                style = MaterialTheme.typography.bodyLarge
                            )
//                        }

                        //Food Modifier List
                        if (food.modifiable) {
                            Column() {
                                food.modifierList.forEach { id ->
                                    val list = mutableListOf<ModifierItem>()
                                    productViewModel.getModifier(id)
                                        ?.let { it1 ->
                                            it1.modifierItemList.forEach{ id1 ->
                                                productViewModel.getModifierItem(id1)?.let { it2 -> list.add(it2) }
                                            }
                                            if (list.any { item -> item.availability }){
                                                ModifierSelection(
                                                    addToCartViewModel,
                                                    list,
                                                    it1
                                                )
                                            } else {
                                                addToCartViewModel.onEvent(AddToCartEvent.RequiredModifierUnavailable)
                                            }
                                        }
                                }
                            }
                        }

//                        item {
                            OutlinedTextField(
                                value = cartState.value.note,
                                label = { Text(text = "Note") },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Note, contentDescription = "Note Icon") },
                                onValueChange = {
                                    addToCartViewModel.onEvent(AddToCartEvent.NoteChanged(it))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(onClick = {
                                    if (addToCartViewModel.addToCartState.value.quantity > 1)
                                        addToCartViewModel.onEvent(AddToCartEvent.QuantityChanged(cartState.value.quantity.dec()))
                                }) {
                                    Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Minus one")
                                }
                                Text(
                                    text = cartState.value.quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = {
                                    addToCartViewModel.onEvent(AddToCartEvent.QuantityChanged(cartState.value.quantity.inc()))
                                }) {
                                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add one")
                                }
                            }
//                        }
                    }
                    if (uiState.value.loading){
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModifierSelection(
    addToCartViewModel: AddToCartViewModel,
    list: List<ModifierItem>,
    thisModifier: com.example.fyp.menucreator.data.model.Modifier
) {

    val cartState = addToCartViewModel.addToCartState.collectAsStateWithLifecycle()

    if (list.any { it.availability }){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            elevation = CardDefaults.cardElevation()
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thisModifier.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(4f).padding(end = 8.dp).basicMarquee(),
                        maxLines = 1
                    )
                    AssistChip(
                        label = {
                            Text(text = if (thisModifier.required) "Required" else "Optional",
                                maxLines = 1
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.fillMaxWidth()
                            )},
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                if (thisModifier.multipleChoice){

                    val selectedItems = remember {
                        mutableStateListOf<ModifierItem>()
                    }
                    selectedItems.clear()
                    cartState.value.modifierList[thisModifier]?.let { selectedItems.addAll(it) }

                    CheckboxSelection(
                        items = list,
                        selectedItems = selectedItems,
                        onClick = { items ->
                            addToCartViewModel.onEvent(AddToCartEvent.ModifierItemListChanged(thisModifier,items.toMutableList()))
                        }
                    )
                } else {
                    var selectedItem = if (!cartState.value.modifierList[thisModifier].isNullOrEmpty()) cartState.value.modifierList[thisModifier]?.get(0) else null

                    RadioSelection(
                        items = list,
                        selectedItem = selectedItem,
                        onClick = { item ->
                            selectedItem = item
                            addToCartViewModel.onEvent(AddToCartEvent.ModifierItemListChanged(thisModifier,listOf(item)))
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun RadioSelection(
    items : List<ModifierItem>,
    selectedItem: ModifierItem?,
    onClick: (ModifierItem) -> Unit
) {
    Column {
        items.forEach {item ->
            if (item.availability){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = item == selectedItem,
                            onClick = { onClick(item) }
                        )
                        .padding(horizontal = 8.dp)
                        .align(Alignment.Start),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    RadioButton(
                        selected = item == selectedItem,
                        onClick = {
                            onClick(item)
                        }
                    )
                    Text(
                        text = item.name,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(4f)
                    )
                    Text(
                        text = item.price.toString(),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        textAlign = TextAlign.End,
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RadioSelectionPreview(){
    RadioSelection(items = listOf(
        ModifierItem(name = "ABC", price = 2.0),
        ModifierItem(name = "HAdcsD", price = 2.0),
        ModifierItem(name = "AKNvsdsfvsfkjcsddajbudfciouhsicahawikbhibvSUbiushaujabGS<JbAsb", price = 2.0),
    ), selectedItem = null, onClick = {})
}

@Composable
fun CheckboxSelection(
    items: List<ModifierItem>,
    selectedItems : SnapshotStateList<ModifierItem>,
    onClick : (List<ModifierItem>) -> Unit
){
    items.forEach { item ->
        if (item.availability) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable {
                        if (!selectedItems.contains(item)) {
                            selectedItems.add(item)
                        } else {
                            selectedItems.remove(item)
                        }
                        onClick(selectedItems)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedItems.contains(item),
                    onCheckedChange = {
                        if (it){
                            selectedItems.add(item)
                        } else
                            selectedItems.remove(item)
                        onClick(selectedItems)
                    },
                    enabled = true
                )
                Text(
                    text = item.name,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(4f)
                )
                Text(
                    text = item.price.toString(),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}