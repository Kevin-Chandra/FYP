package com.example.fyp.ordering_system.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Chip
import androidx.compose.material3.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@RootNavGraph(true)
@Destination
@Composable
fun ViewProductListScreen(
    navigator: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    FypTheme(){
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            val foodList = productViewModel.foods.collectAsStateWithLifecycle()
            val cart = cartViewModel.cart

            Scaffold(
                bottomBar = {
                    Button(
                        onClick = { navigator.navigate(Screen.ReviewOrderScreen.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cart.value.size.toString(),
//                            style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "View Cart",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = cartViewModel.getSubTotalPrice().toString())
                        }
                    }
                }
            ) {
                if (foodList.value is UiState.Success) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        items((foodList.value as UiState.Success<List<Food>>).data) { item ->
                            ProductCard(item){ it1 ->
                                navigator.navigate(Screen.AddToCartScreen.withArgs(it1, "null"))
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
fun ProductCard(
    food: Food,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                enabled = food.availability
            ) {
                onClick(food.productId)
            },
        elevation = CardDefaults.elevatedCardElevation(),
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = food.name,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.headlineSmall
                )
                if (!food.availability) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "Unavailable",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                } else {
                    Text(
                        text = food.price.toString(),
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Text(
                text = food.description,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ProductCardPreview() {
    ProductCard(food = Food(name = "jdbajh", price = 83.2, description = "Desc", availability = false), onClick = {})
}
