package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FoodRepository(
) {
     private val foodCollectionRef = Firebase.firestore.collection(FireStoreCollection.FOOD)

    fun addFood(food: Food) : UiState<Boolean>{
        return try {
            foodCollectionRef.add(food)
            UiState.Success(true)
        } catch (e: Exception){
            UiState.Failure(e)
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

    fun subscribeFoodUpdates() = callbackFlow<UiState<List<Food>>> {
        val snapshotListener = foodCollectionRef.addSnapshotListener{ querySnapshot, e ->
            if (e != null ){
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let{
//                val foodList = arrayListOf<Food>()
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


}