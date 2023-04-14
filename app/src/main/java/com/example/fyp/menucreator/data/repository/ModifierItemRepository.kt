package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.MenuCreatorResponse
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

    suspend fun addModifierItem(item: ModifierItem,result:(UiState<String>) -> Unit) {
        try {
            modifierItemCollectionRef.add(item).addOnSuccessListener {
                result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_ITEM_ADD_SUCCESS))
            }.addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }.await()
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

//    fun subscribeModifierItemUpdates() = callbackFlow<UiState<Map<String,ModifierItem>>> {
//        val snapshotListener = modifierItemCollectionRef.addSnapshotListener{ querySnapshot, e ->
//            if (e != null ){
//                UiState.Failure(e)
//                return@addSnapshotListener
//            }
//            querySnapshot?.let{
//                val modifierItemsResponse = run {
//                    val map = mutableMapOf<String,ModifierItem>()
//                    for (document in querySnapshot.documents){
//                        val mi = document.toObject<ModifierItem>()
//                        if (mi != null){
//                            map[mi.productId] = mi
//                        }
//                    }
////                    val modifierItems = querySnapshot.toObjects(ModifierItem::class.java)
//                    UiState.Success(map)
//                }
//                trySend(modifierItemsResponse)
//            }
//        }
//        awaitClose {
//            snapshotListener.remove()
//        }
//    }

    fun subscribeModifierItemUpdates() = callbackFlow<UiState<List<ModifierItem>>> {
        val snapshotListener = modifierItemCollectionRef.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val itemsResponse = run {
                    val items = querySnapshot.toObjects(ModifierItem::class.java)
                    UiState.Success(items)
                }
                trySend(itemsResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun deleteModifierItem(id: String, result: (UiState<String>) -> Unit) {
        try {
            val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            for (doc in query.documents)
                modifierItemCollectionRef.document(doc.id).delete().await()
            result.invoke(UiState.Success(MenuCreatorResponse.ITEM_DELETED))
        } catch (e : Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    suspend fun updateModifierItem(newItem: ModifierItem, result: (UiState<String>) -> Unit){
        try {
            val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,newItem.productId)
                .get()
                .await()
            if (query.documents.isEmpty()){
                addModifierItem(newItem,result)
            } else {
                for (doc in query.documents)
                    modifierItemCollectionRef.document(doc.id).set(
                        newItem,
                        SetOptions.merge()
                    )
            }
            UiState.Success(false)
        } catch (e : Exception){
            UiState.Failure(e)
        }
    }

    suspend fun checkModifierItemId(id: String) : Boolean {
        val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
        if (query.documents.isNotEmpty()){
            return true
        }
        return false
    }

    suspend fun getModifier(id: String, result: (UiState<ModifierItem?>) -> Unit){
        try{
            val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                result.invoke(UiState.Success(null))
            else{
                for (doc in query.documents){
                    val modifier = modifierItemCollectionRef
                        .document(doc.id)
                        .get()
                        .await()
                        .toObject<ModifierItem>()
                    result.invoke(UiState.Success(modifier))
                }
            }
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }

    }

    suspend fun updateAvailability(id: String, value: Boolean, result: (UiState<String>) -> Unit){
        try {
            val query = modifierItemCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents){
                modifierItemCollectionRef.document(doc.id)
                    .update(FireStoreDocumentField.AVAILABILITY,value)
                    .await()
            }
            result.invoke(UiState.Success("Updated Availability!"))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

}