package com.example.fyp.menucreator.data.repository

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MenuSettingsRepository {

    private val foodCategoryRef = Firebase.firestore.collection(FireStoreCollection.FOOD_CATEGORY)

    suspend fun addCategory(category: FoodCategory,result: (UiState<Boolean>) -> Unit){
        foodCategoryRef.add(category)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    result.invoke(UiState.Success(true))
                }else {
                    it.exception?.let { it1 -> UiState.Failure(it1) }
                        ?.let { it2 -> result.invoke(it2) }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }.await()
    }

    suspend fun deleteCategory(category: FoodCategory, result: (UiState<Boolean>) -> Unit){
        foodCategoryRef.document(category.id).delete()
            .addOnCompleteListener{
                if (it.isSuccessful){
                    result.invoke(UiState.Success(true))
                }else {
                    it.exception?.let { it1 -> UiState.Failure(it1) }
                        ?.let { it2 -> result.invoke(it2) }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }.await()
    }

    suspend fun updateCategory(category: FoodCategory, result: (UiState<Boolean>) -> Unit){
        foodCategoryRef.document(category.id)
            .update(FireStoreDocumentField.FOOD_CATEGORY_NAME,category.name)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    result.invoke(UiState.Success(true))
                }else {
                    it.exception?.let { it1 -> UiState.Failure(it1) }
                        ?.let { it2 -> result.invoke(it2) }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }.await()
    }
}