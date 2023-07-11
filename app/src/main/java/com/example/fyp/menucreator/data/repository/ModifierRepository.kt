package com.example.fyp.menucreator.data.repository

import com.example.fyp.menucreator.data.model.Modifier
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

class ModifierRepository {
    private val modifierCollectionRef = Firebase.firestore.collection(FireStoreCollection.MODIFIER)

    fun addModifier(modifier: Modifier,result: (UiState<String>) -> Unit){
        try {
            modifierCollectionRef.add(modifier).addOnSuccessListener {
                result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_UPLOAD_SUCCESS))
            }.addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }
        } catch (e: Exception){
            UiState.Failure(e)
        }
    }

    suspend fun updateModifier(modifier: Modifier,result: (UiState<String>) -> Unit){
        try {
            val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,modifier.productId)
                .get()
                .await()
            for (doc in query.documents)
                modifierCollectionRef.document(doc.id).set(
                    modifier,
                    SetOptions.merge()
                )
            result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_MERGE_SUCCESS))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }

//    fun subscribeModifierUpdates() = callbackFlow<UiState<Map<String,Modifier>>> {
//        val snapshotListener = modifierCollectionRef.addSnapshotListener{ querySnapshot, e ->
//            if (e != null ){
//                UiState.Failure(e)
//                return@addSnapshotListener
//            }
//            querySnapshot?.let{
//                val modifiersResponse = run {
//                    val map = mutableMapOf<String, Modifier>()
//                    for (document in querySnapshot.documents){
//                        val modifier = document.toObject<Modifier>()
//                        if (modifier != null){
//                            map[modifier.productId] = modifier
//                        }
//                    }
//                    UiState.Success(map)
//                }
//                trySend(modifiersResponse)
//            }
//        }
//        awaitClose {
//            snapshotListener.remove()
//        }
//    }

    fun subscribeModifierUpdates() = callbackFlow<UiState<List<Modifier>>> {
        val snapshotListener = modifierCollectionRef.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                UiState.Failure(e)
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val modifiersResponse = run {
                    val modifiers = querySnapshot.toObjects(Modifier::class.java)
                    UiState.Success(modifiers)
                }
                trySend(modifiersResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun deleteModifier(id: String, result: (UiState<String>) -> Unit) {
        try{
            val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            for (doc in query.documents)
                modifierCollectionRef.document(doc.id).delete().await()
            result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_DELETED))
        } catch (e : Exception){
            result.invoke(UiState.Failure(e))
        }
    }

    suspend fun checkModifierId(id: String) : Boolean{
        val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id).get().await()
        if (query.documents.isNotEmpty()){
            return true
        }
        return false
    }

    suspend fun getModifier(id: String, result: (UiState<Modifier?>) -> Unit){
        try{
            val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                result.invoke(UiState.Success(null))
            else{
                for (doc in query.documents){
                    val modifier = modifierCollectionRef
                        .document(doc.id)
                        .get()
                        .await()
                        .toObject<Modifier>()
                    result.invoke(UiState.Success(modifier))
                }
            }
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }

    }

    suspend fun getModifierList(id: String) : List<String>?  {
        try{
            val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                return null
            else{
                for (doc in query.documents){
                    return doc.get(FireStoreDocumentField.MODIFIER_ITEM_LIST) as List<String>
                }
            }
            return null
        } catch (e: Exception){
            e.printStackTrace()
            return null
        }

    }

    suspend fun updateImageField(modifierId : String, image: Pair<String,String>?, result: (UiState<String>) -> Unit){
        if (image == null)
            return
        try {
            val imageMap = mapOf(
                FireStoreDocumentField.PRODUCT_IMAGE_PATH to image.second,
                FireStoreDocumentField.PRODUCT_IMAGE_URI to image.first
            )
            val query = modifierCollectionRef.whereEqualTo(FireStoreDocumentField.PRODUCT_ID, modifierId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product with id [$modifierId] not found!")
            for (doc in query.documents){
                modifierCollectionRef.document(doc.id)
                    .update(imageMap).await()
            }
            result.invoke(UiState.Success("Success Image Field Update"))
        } catch (e: Exception){
            result.invoke(UiState.Failure(e))
        }
    }


}