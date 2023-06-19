package com.example.fyp.menucreator.data.repository

import android.net.Uri
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.util.FirebaseStorageReference
import com.example.fyp.menucreator.util.MenuCreatorResponse
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProductImageRepository {
    private val imageStorageRef = FirebaseStorage.getInstance().reference

    suspend fun uploadImage(productType:ProductType, id: String, imageUri: Uri, result: (UiState<Boolean>) -> Unit) : Pair<String,String>?{
        val basePath = FirebaseStorageReference.PRODUCT_IMAGE_REFERENCE
        val path = basePath + when (productType){
            ProductType.FoodAndBeverage -> FirebaseStorageReference.FOOD_IMAGE_PATH + id
            ProductType.Modifier -> FirebaseStorageReference.MODIFIER_IMAGE_PATH + id
            ProductType.ModifierItem -> id
        }
        return try {
            val uri = imageStorageRef.child(path).putFile(imageUri)
                .await()
                .storage
                .downloadUrl
                .await()
            result.invoke(UiState.Success(true))
            Pair(uri.toString(),path)
        } catch (e:java.lang.Exception){
            result.invoke(UiState.Failure(e))
            null
        }
    }

    suspend fun getImage(path: String) : Uri {
        return imageStorageRef.child(path).downloadUrl.await()
    }

    fun deleteImage(path: String,result: (UiState<String>) -> Unit){
        imageStorageRef.child(path)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success(MenuCreatorResponse.PRODUCT_IMAGE_DELETED))
            }.addOnFailureListener {
                result.invoke(UiState.Failure(it))
            }
    }
}