package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MenuSettingsRepository {

    private val foodCategoryRef = Firebase.firestore.collection(FireStoreCollection.FOOD_CATEGORY)

    suspend fun addCategory(category: FoodCategory,result: (UiState<Boolean>) -> Unit){
        foodCategoryRef.add(category)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    it.result.update(FireStoreDocumentField.ID,it.result.id)
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

    suspend fun deleteCategory(id: String, result: (UiState<Boolean>) -> Unit){
        foodCategoryRef.document(id).delete()
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

//    fun getFoodCategory()
    
    fun getFoodCategory() = callbackFlow<UiState<List<FoodCategory>>> {
        println("AAA")
        val snapshotListener = foodCategoryRef.addSnapshotListener{ querySnapshot, e ->
            if (e != null ){
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let{
                val categoryResponse = run {
                    val categories = querySnapshot.toObjects(FoodCategory::class.java)
                    UiState.Success(categories)
                }
                trySend(categoryResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    //return false if category already exist
    suspend fun checkCategory(name: String) : Boolean{
        val query = foodCategoryRef.whereEqualTo(FireStoreDocumentField.FOOD_CATEGORY_NAME,name).get().await()
        return query.documents.isEmpty()
    }
}