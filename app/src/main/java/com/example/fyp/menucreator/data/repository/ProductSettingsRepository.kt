package com.example.fyp.menucreator.data.repository

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.fyp.account_management.data.model.*
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.FirebaseStorageReference
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class ProductSettingsRepository @Inject constructor(
    private val database: FirebaseFirestore,
) {
    private val productSettingsCollectionReference = database.collection(FireStoreCollection.ADMIN_SETTINGS)

    fun setServiceCharge(amount: Double, result: (Response<String>) -> Unit){
        productSettingsCollectionReference.document(FireStoreDocumentField.PRODUCT_SETTINGS)
            .update(mapOf( FireStoreDocumentField.SERVICE_CHARGE to amount))
            .addOnSuccessListener {
                result.invoke(Response.Success("Service Charge Updated!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }
    }

    suspend fun getServiceCharge(): Response<Double> {
        return try {
            val data = productSettingsCollectionReference.document(FireStoreDocumentField.PRODUCT_SETTINGS)
                .get().await()
            Response.Success(data.getDouble(FireStoreDocumentField.SERVICE_CHARGE)?:-1.0)
        } catch (e: Exception){
            Response.Error(e)
        }
    }

    suspend fun getVersionName(): Response<Pair<String,String>> {
        return try {
            val versionName = productSettingsCollectionReference.document(FireStoreDocumentField.APP_VERSION)
                .get().await()
            Response.Success(Pair(versionName.getString(FireStoreDocumentField.VERSION_NAME)?:"", versionName.getString(FireStoreDocumentField.DOWNLOAD_LINK) ?: ""))
        } catch (e: Exception){
            Response.Error(e)
        }
    }

    fun setTax(amount: Double, result: (Response<String>) -> Unit){
        productSettingsCollectionReference.document(FireStoreDocumentField.PRODUCT_SETTINGS)
            .update(mapOf( FireStoreDocumentField.TAX to amount))
            .addOnSuccessListener {
                result.invoke(Response.Success("Tax Updated!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }
    }

    suspend fun getTax() : Response<Double> {
        return try {
            val data = productSettingsCollectionReference.document(FireStoreDocumentField.PRODUCT_SETTINGS)
                .get().await()
            Response.Success(data.getDouble(FireStoreDocumentField.TAX)?:-1.0)
        } catch (e: Exception){
            Response.Error(e)
        }
    }
}