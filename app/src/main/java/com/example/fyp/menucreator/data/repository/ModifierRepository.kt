package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ModifierRepository(
) {
    private val modifierCollectionRef = Firebase.firestore.collection(FireStoreCollection.MODIFIER)

    fun addModifier(modifier: Modifier) : UiState<Boolean>{
        return try {
            modifierCollectionRef.add(modifier)
            UiState.Success(true)
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

    suspend fun updateModifier(id: String,modifier: Modifier) : UiState<Boolean>{
        val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                modifierCollectionRef.document(doc.id).set(
                    modifier,
                    SetOptions.merge()
                )
            UiState.Success(true)
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

    fun subscribeModifierUpdates() = callbackFlow<UiState<Map<String,Modifier>>> {
        val snapshotListener = modifierCollectionRef.addSnapshotListener{ querySnapshot, e ->
            if (e != null ){
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let{
                val modifiersResponse = run {
                    val map = mutableMapOf<String, Modifier>()
                    for (document in querySnapshot.documents){
                        val modifier = document.toObject<Modifier>()
                        if (modifier != null){
                            map[modifier.productId] = modifier
                        }
                    }
                    UiState.Success(map)
                }
                trySend(modifiersResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun deleteModifier(id: String) : UiState<Boolean> {
        val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            for (doc in query.documents)
                modifierCollectionRef.document(doc.id).delete().await()
            println("[$id] deleted")
            UiState.Success(true)
        } catch (e : Exception){
            UiState.Failure(e)
        }
    }

    suspend fun checkModifierId(id: String) : Boolean{
        val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
        if (query.documents.isNotEmpty()){
            return true
        }
        return false
    }


}