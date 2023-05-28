package com.example.fyp.ordering_system.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.compose.FypTheme
import com.example.fyp.R
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.ui.components.CustomerOrderBottomNavigation
import com.example.fyp.ordering_system.ui.navigation.Screen
import com.example.fyp.ordering_system.ui.viewmodel.CartViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProductListScreen(
    navigator: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    val filteredFoodList = productViewModel.filteredFoods.collectAsStateWithLifecycle()
    val foodCategory = productViewModel.foodCategories.collectAsStateWithLifecycle()
    val selectedCategory = productViewModel.selectedCategory.collectAsStateWithLifecycle()
    val cart = cartViewModel.cart

    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.value + delta
                bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                return Offset.Zero
            }
        }
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    FypTheme() {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            if (foodCategory.value is UiState.Success) {
                                CategoryList(
                                    categoryList = (foodCategory.value as UiState.Success<List<FoodCategory>>).data,
                                    selected = selectedCategory.value?.id,
                                    onSelected = {
                                        productViewModel.filterFoodByCategory(it)
                                    })
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                bottomBar = {
                    CustomerOrderBottomNavigation(
                        navController = navigator,
                        modifier = Modifier
                            .height(bottomBarHeight)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.value.roundToInt()
                                )
                            })
                },
                modifier = Modifier.nestedScroll(nestedScrollConnection).nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    if (filteredFoodList.value is UiState.Success) {
                        LazyColumn(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding()
                        ) {
                            items((filteredFoodList.value as UiState.Success<List<Food>>).data) { item ->
                                ProductCard(item) { it1 ->
                                    navigator.navigate(Screen.AddToCartScreen.withArgs(it1, "null","1"))
                                }
                            }
                        }

                    }
                    Button(
                        onClick = { navigator.navigate(Screen.ReviewOrderScreen.route) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.value.roundToInt()
                                )
                            },
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

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryList(
    categoryList: List<FoodCategory>,
    selected: String?,
    onSelected: (FoodCategory?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        contentPadding = PaddingValues(8.dp)
    ){
        items(categoryList){
            ElevatedFilterChip(
                selected = (it.id == selected),
                onClick = {
                    if (selected == it.id){
                        onSelected(null)
                    } else {
                        onSelected(it)
                    }},
                label = {
                    Text(text = it.name)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
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
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoilImage(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(10.dp)),
                imageModel = {food.imageUri?: R.mipmap.ic_launcher},
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    contentDescription = "Food Image",
                    colorFilter = null,
                ),
                previewPlaceholder = R.mipmap.ic_launcher,
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        baseColor = Color.Gray,
                        highlightColor = Color.White
                    )
                },
            )
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ProductCardPreview() {
    ProductCard(food = Food(name = "jdbajh", price = 83.2, description = "Desc", availability = false), onClick = {})
}
