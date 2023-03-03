package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
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

class ModifierItemRepository {

    private val modifierItemCollectionRef = Firebase.firestore.collection(FireStoreCollection.MODIFIER_ITEM)

    fun addModifierItem(item: ModifierItem) : UiState<Boolean> {
        return try {
            modifierItemCollectionRef.add(item)
            UiState.Success(false)
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

    fun subscribeModifierItemUpdates() = callbackFlow<UiState<Map<String,ModifierItem>>> {
        val snapshotListener = modifierItemCollectionRef.addSnapshotListener{ querySnapshot, e ->
            if (e != null ){
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let{
                val modifierItemsResponse = run {
                    val map = mutableMapOf<String,ModifierItem>()
                    for (document in querySnapshot.documents){
                        val mi = document.toObject<ModifierItem>()
                        if (mi != null){
                            map[mi.productId] = mi
                        }
                    }
//                    val modifierItems = querySnapshot.toObjects(ModifierItem::class.java)
                    UiState.Success(map)
                }
                trySend(modifierItemsResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun deleteModifierItem(id: String): UiState<Boolean> {
        val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            for (doc in query.documents)
                modifierItemCollectionRef.document(doc.id).delete().await()
            println("[$id] deleted in database")
            UiState.Success(true)
        } catch (e : Exception){
            UiState.Failure(e)
        }
    }

    suspend fun updateModifierItem(id: String, item: ModifierItem): UiState<Boolean> {
        val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
            .get()
            .await()
        return try {
            if (query.documents.isEmpty()){
                addModifierItem(item)
            } else {
                for (doc in query.documents)
                    modifierItemCollectionRef.document(doc.id).set(
                        item,
                        SetOptions.merge()
                    )
            }
            UiState.Success(false)
        } catch (e : Exception){
            UiState.Failure(e)
        }
    }

    suspend fun checkModifierId(id: String) : Boolean {
        val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
        if (query.documents.isNotEmpty()){
            return true
        }
        return false
    }

}