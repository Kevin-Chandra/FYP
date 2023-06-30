package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.food.SortedBy.*
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import java.lang.reflect.TypeVariable
import javax.inject.Inject
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class SortFoodUseCase {
    operator fun invoke(data: List<Food>, sortBy: SortedBy , result:(List<Food>) -> Unit){
        CoroutineScope(Dispatchers.Default).launch{
            result.invoke(
                when(sortBy){
                    POPULARITY -> data.sortedByDescending { it.allTimeSales }
                    CATEGORY -> data.sortedBy { it.category }
                    DATE_CREATED -> data.sortedByDescending { it.createdAt }
                    NAME -> data.sortedBy { it.name }
                }
            )
        }
    }
}

enum class SortedBy(val title: String){
    POPULARITY("Popularity"),
    CATEGORY("Category"),
    DATE_CREATED("Date Created"),
    NAME("Name"),
}