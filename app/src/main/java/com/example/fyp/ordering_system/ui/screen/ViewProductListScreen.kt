package com.example.fyp.ordering_system.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.ui.theme.FYPTheme
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Destination(start = true)
@Composable
fun ViewProductListScreen(
    navigator: DestinationsNavigator?
) {
    val productViewModel = hiltViewModel<ProductViewModel>()
    val foodList = productViewModel.foods.collectAsStateWithLifecycle()
    val showBottomSheet = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true){

    }

    if (foodList.value is UiState.Success){
        LazyColumn(modifier = Modifier.fillMaxSize()){
            items((foodList.value as UiState.Success<List<Food>>).data){
                ProductCard(food = it,showBottomSheet)
            }
        }
        


    }

    if(showBottomSheet.value){
        BottomSheetProduct()
    }



}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BottomSheetProduct(
) {
    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded },
        skipHalfExpanded = false
    )
    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetContent = {
            Column(
                //...
            ) {
                //...

                Button(
                    onClick = {
                        coroutineScope.launch { modalSheetState.hide() }
                    }
                ) {
                    Text(text = "Hide Sheet")
                }
            }
        }
    ) {
        Scaffold {
            Box(
                modifier = Modifier.padding(it)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (modalSheetState.isVisible)
                                modalSheetState.hide()
                            else
                                modalSheetState.show()
                        }
                    },
                ) {
                    Text(text = "Open Sheet")
                }
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun Preview() {
    FYPTheme {
//        ProductCard(Food(
//            name = "Jksn",
//            description = "jncajdknk",
//            price = 3.3
//        ))
        ViewProductListScreen(navigator = null)
    }
}

@Composable
fun ProductCard(food: Food,state: MutableState<Boolean>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                state.value = true
            }
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(food.name, modifier = Modifier.align(Alignment.CenterVertically))
            Text(text = food.price.toString(), modifier = Modifier.align(Alignment.CenterVertically))
        }
        Text(food.description, modifier = Modifier.align(Alignment.Start))
        Divider()
    }
}