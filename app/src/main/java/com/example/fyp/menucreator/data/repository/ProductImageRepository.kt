package com.example.fyp.menucreator.data.repository

import android.net.Uri
import com.example.fyp.menucreator.util.FirebaseStorageReference
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductImageRepository {
    private val imageStorageRef = FirebaseStorage.getInstance().reference

    suspend fun uploadImage(foodId: String, imageUri: Uri, result: (UiState<Pair<String,String>>) -> Unit) : Pair<String,String>?{
        val path = FirebaseStorageReference.PRODUCT_IMAGE_REFERENCE + foodId

        return try {
            val uri = imageStorageRef.child(path).putFile(imageUri)
                .await()
                .storage
                .downloadUrl
                .await()
            result.invoke(UiState.Success(Pair(uri.toString(),path)))
            Pair(uri.toString(),path)
        } catch (e:java.lang.Exception){
            result.invoke(UiState.Failure(e))
            null
        }
    }

    suspend fun getImage(path: String) : Uri {
        return imageStorageRef.child(path).downloadUrl.await()
    }

    fun deleteImage(path: String,result: (UiState<Boolean>) -> Unit){
        imageStorageRef.child(path)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success(false))
            }.addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }
    }
}