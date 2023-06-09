package com.example.fyp.pos.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.ui.components.CheckboxSelection
import com.example.fyp.ordering_system.ui.components.RadioSelection
import com.example.fyp.ordering_system.ui.state.AddToCartUiState
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.errorToast
import com.example.fyp.pos.ui.viewmodel.PosAddToCartViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent

@Composable
fun PosAddToCartScreen (
    navigator: NavController,
    productViewModel: ProductViewModel = hiltViewModel(),
    foodId: String,
    orderItemId: String? = null,
    quantity: Int = 1
) {
    val context = LocalContext.current

    val addToCartViewModel = hiltViewModel<PosAddToCartViewModel>()

    val cartState = addToCartViewModel.addToCartState.collectAsStateWithLifecycle()
    val uiState = addToCartViewModel.addToCartUiState.collectAsStateWithLifecycle(AddToCartUiState())
    val food = productViewModel.getFood(foodId)!!

    var count = 0
    food.modifierList.forEach {
        if (productViewModel.getModifier(it)?.required == true)
            count++
    }
    addToCartViewModel.onEvent(AddToCartEvent.FoodChanged(food,count))
    addToCartViewModel.onEvent(AddToCartEvent.QuantityChanged(quantity))

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
                                                PosModifierSelection(
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
                                leadingIcon = { Icon(imageVector = Icons.Filled.Description, contentDescription = "Note Icon") },
                                onValueChange = {
                                    addToCartViewModel.onEvent(AddToCartEvent.NoteChanged(it))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            Row(
                                modifier = Modifier.padding(8.dp).border(
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(50))
                                    .align(Alignment.CenterHorizontally),
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
fun PosModifierSelection(
    addToCartViewModel: PosAddToCartViewModel,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ){
                        Text(
                            text = thisModifier.name,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
//                                .weight(4f)
                                .padding(end = 8.dp)
                                .basicMarquee(),
                            maxLines = 1,
                            fontWeight = FontWeight.Bold
                        )
                        if (thisModifier.multipleChoice && cartState.value.modifierList[thisModifier]!= null){
                            ElevatedAssistChip(
                                onClick = {},
                                label = {
                                    Text(text = "${cartState.value.modifierList[thisModifier]?.size} items selected")
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                    AssistChip(
                        label = {
                            Text(text = if (thisModifier.required) "Required" else "Optional",
                                maxLines = 1
                            )},
                        onClick = {},
//                        modifier = Modifier.weight(1f)
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

                    val selectedItem = remember {
                        mutableStateOf<ModifierItem?>(null)
                    }
                    selectedItem.value = cartState.value.modifierList[thisModifier]?.get(0)

                    RadioSelection(
                        items = list,
                        selectedItem = selectedItem.value,
                        onClick = { item ->
                            selectedItem.value = item
                            addToCartViewModel.onEvent(AddToCartEvent.ModifierItemListChanged(thisModifier,
                                listOf(item)
                            ))
                        }
                    )
                }
            }
        }
    }

}


