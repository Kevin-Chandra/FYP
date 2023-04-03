package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FoodRepository(
) {
     private val foodCollectionRef = Firebase.firestore.collection(FireStoreCollection.FOOD)

    fun addFood(food: Food, result: (UiState<String>) -> Unit) {
        try{
            foodCollectionRef.add(food)
            result.invoke(UiState.Success("Food Data Uploaded!"))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    //Return true if id already exist
    suspend fun checkFoodId(id: String) : Boolean {
        val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
        if (query.documents.isNotEmpty()){
            return true
        }
        return false
    }

    suspend fun checkFoodId(id: String, result: (UiState<Boolean>) -> Unit) {
        try {
            val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
            if (query.documents.isNotEmpty()){
                result.invoke(UiState.Success(true))
            } else {
                result.invoke(UiState.Success(false))
            }
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    fun subscribeFoodUpdates() = callbackFlow<UiState<List<Food>>> {
        val snapshotListener = foodCollectionRef.addSnapshotListener{ querySnapshot, e ->
            if (e != null ){
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let{
                val foodsResponse = run {
                    val foods = querySnapshot.toObjects(Food::class.java)
                    UiState.Success(foods)
                }
                trySend(foodsResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun deleteFood(id: String) : UiState<Boolean> {
        val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            for (doc in query.documents)
                foodCollectionRef.document(doc.id).delete().await()
            UiState.Success(true)
        } catch (e : Exception){
            UiState.Failure(e)
        }
    }

    suspend fun deleteFood(id: String,result: (UiState<Boolean>) -> Unit){
        try {
            val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            for (doc in query.documents)
                foodCollectionRef.document(doc.id).delete().await()
            result.invoke(UiState.Success(true))
        } catch (e : Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    suspend fun updateFood(id: String,food: Food) : UiState<Boolean>{
        val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                foodCollectionRef.document(doc.id).set(
                    food,
                    SetOptions.merge()
                )
            UiState.Success(true)
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

    suspend fun updateFood(food: Food, result: (UiState<String>) -> Unit){
        try {
            val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,food.productId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                foodCollectionRef.document(doc.id).set(
                    food,
                    SetOptions.merge()
                )
            result.invoke(UiState.Success("Food Upload Success"))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    suspend fun updateImageField(foodId : String, image: Pair<String,String>?, result: (UiState<String>) -> Unit){
        if (image == null)
            return
        try {
            val imageMap = mapOf(
                FireStoreDocumentField.FOOD_IMAGE_PATH to image.second,
                FireStoreDocumentField.FOOD_IMAGE_URI to image.first
            )
            val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID, foodId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents){
                foodCollectionRef.document(doc.id)
                    .update(imageMap)
            }
            result.invoke(UiState.Success("Success Image Field Update"))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    suspend fun getFood(id: String, result: (UiState<Food>) -> Unit){
        try {
            val query = foodCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                result.invoke(UiState.Success(doc.toObject<Food>()!!))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

}