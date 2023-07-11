package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.domain.food.SortedBy.CATEGORY
import com.example.fyp.menucreator.domain.food.SortedBy.DATE_CREATED
import com.example.fyp.menucreator.domain.food.SortedBy.NAME
import com.example.fyp.menucreator.domain.food.SortedBy.POPULARITY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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