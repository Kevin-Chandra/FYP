package com.example.fyp.pos.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fyp.theme.FypTheme
import com.example.fyp.R
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.navigation.PosScreen
import com.example.fyp.pos.ui.viewmodel.TableOrderCartViewModel
import com.example.fyp.pos.util.TableOrderEvent
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun PosOrderScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: TableOrderCartViewModel,
    tableId: String,
    onBackPressed: () -> Unit
) {

    val foodList = productViewModel.filteredFoods.collectAsStateWithLifecycle()
    val foodCategoryList = productViewModel.foodCategories.collectAsStateWithLifecycle()
    val cart = orderViewModel.cart

    val currentCategory = remember {
        mutableStateOf<FoodCategory?>(null)
    }

    BackHandler() {
        onBackPressed()
    }

    FypTheme() {
        Surface() {
            Scaffold(
                bottomBar = {
                    Button(
                        onClick = {
                            navController.navigate(PosScreen.PosOrderSummaryScreen.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = "${cart.value.size} Item • ")
                        Text(text = "View order summary")
                    }
                }
            ) {
                PosOrderScreenContent(
                    categoryList = (foodCategoryList.value as UiState.Success).data,
                    foodList = (foodList.value as UiState.Success).data,
                    currentCategory = currentCategory.value,
                    onFilterFoodClick = { cat ->
                        currentCategory.value = cat
                        productViewModel.filterFoodByCategory(cat)
                    },
                    onAddToCartClick = { food, qty ->
                        if ( !food.modifiable || food.modifierList.isEmpty()){
                            orderViewModel.onEvent(TableOrderEvent.OnAddFood(food,qty))
                        } else {
                            navController.navigate(
                                PosScreen.PosAddToCartScreen.withRequiredArgs(food.productId,qty.toString())
                            )
                        }
                    },
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}

@Composable
fun PosOrderScreenContent(
    categoryList: List<FoodCategory>,
    foodList: List<Food>,
    currentCategory: FoodCategory?,
    onFilterFoodClick: (FoodCategory?) -> Unit,
    onAddToCartClick: (Food,Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()){
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
//            .fillMaxHeight(0.05f)
//            .align(Alignment.TopCenter)
            ,
            horizontalArrangement = Arrangement.Center,
        ){
            items(categoryList) { cat ->
                TextButton(onClick = {
                     if (currentCategory == cat){
                         onFilterFoodClick(null)
                     } else {
                         onFilterFoodClick(cat)
                     }
                }, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = cat.name,
                        fontWeight = if (currentCategory == cat) FontWeight.Bold else FontWeight.Light,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStartPercent = 7, topEndPercent = 7),
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                val categoryText = currentCategory?.name?: "All Foods"
                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyVerticalGrid(columns = GridCells.Adaptive(175.dp)){
                    items(foodList) { food ->
                        var quantity by rememberSaveable {
                            mutableIntStateOf(0)
                        }
                        ProductItem(
                            food = food,
                            onAddToCartClick = {
                                onAddToCartClick(food,quantity)
                                quantity = 0
                            },
                            quantity = quantity,
                            onQuantityDecrement = {
                                if (quantity > 0) quantity -= 1
                            },
                            onQuantityIncrement = {
                                quantity += 1
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItem(
    food: Food,
    quantity: Int,
    onAddToCartClick: () -> Unit,
    onQuantityIncrement: () -> Unit,
    onQuantityDecrement: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .width(175.dp)
            .height(350.dp)
            .padding(8.dp)
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Column(
//                modifier = Modifier.fillMaxHeight(),
//
//            ) {
//                CoilImage(
//                    modifier = Modifier
//                        .size(150.dp)
//                        .clip(RoundedCornerShape(10.dp)),
//                    imageModel = {food.imageUri?: R.mipmap.ic_launcher},
//                    imageOptions = ImageOptions(
//                        contentScale = ContentScale.Crop,
//                        alignment = Alignment.Center,
//                        contentDescription = "Food Image",
//                        colorFilter = null,
//                    ),
//                    previewPlaceholder = R.mipmap.ic_launcher,
//                    component = rememberImageComponent {
//                        +ShimmerPlugin(
//                            baseColor = Color.Gray,
//                            highlightColor = Color.White
//                        )
//                    },
//                )
//                placeholder
            Image(
                imageVector = Icons.Default.Panorama, contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Text(
                text = food.name,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                modifier = Modifier
                    .basicMarquee()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.productId,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = food.price.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .basicMarquee()
                )
            }
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = onQuantityDecrement,
                    modifier = Modifier.size(32.dp),
                    enabled = quantity > 0
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                }
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                    text = quantity.toString(),
                )
                OutlinedIconButton(
                    onClick = onQuantityIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
            if (!food.availability) {
                AssistChip(onClick = {}, label = {
                    Text(text = "Unavailable")
                })
            } else {
                Button(
                    onClick = onAddToCartClick,
                    enabled = quantity > 0
                ) {
                    Text(text = "Add To Cart")
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun ProductItemPreview() {
    ProductItem(food = Food(
        name = "ABC",
        productId = "F-1",
        description = "kjsncd sdwjcna awkcnfawsk kwaidncoln kacswdina wkdicfnsa kwicns",

    ),
    onAddToCartClick = {},
        onQuantityIncrement = {},
        onQuantityDecrement = {},
        quantity = 1
    )
}

@Preview(showBackground = true, device = "id:Nexus One",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun PosContentPreview() {
    PosOrderScreenContent(
        categoryList = listOf(
            FoodCategory(name = "Main"),
            FoodCategory(name = "Dessert"),
            FoodCategory(name = "Beverages"),
        ),
        foodList = listOf(Food(
            name = "ABC",
            productId = "F-1",
            description = "kjsncd sdwjcna awkcnfawsk kwaidncoln kacswdina wkdicfnsa kwicns",
            ),
            Food(
                name = "Afw",
                productId = "F-2",
                price = 13.0,
                description = "kjsncd sdwjcna awkcnfawsk kwaidncoln kacswdina wkdicfnsa kwicns",
            ),
        ),
        currentCategory = null,
        onFilterFoodClick = {},
        onAddToCartClick = { _, _ -> }
    )
}