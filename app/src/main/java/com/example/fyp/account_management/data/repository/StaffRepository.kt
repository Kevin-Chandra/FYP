package com.example.fyp.account_management.data.repository

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
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

class StaffRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val imageDatabase: FirebaseStorage,
) {

    private val adminSettings = database.collection(FireStoreCollection.ADMIN_SETTINGS)
    private val userCollectionRef = database.collection(FireStoreCollection.USER)
//    private val imageRef = imageDatabase.reference


//    private fun updateUserInfo(user: Account, result: (Response<String>) -> Unit) {
//        val document = userCollectionRef.document(user.id)
//        CoroutineScope(Dispatchers.IO).launch{
//            try{
//                document.set(
//                    user,
//                    SetOptions.merge())
//                    .await()
//                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
//            } catch (e: Exception) {
//                result.invoke(Response.Error(e))
//            }
//        }
//    }
//

    fun setToken(token: String, result: (Response<String>) -> Unit){
        adminSettings.document(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN)
            .set(mapOf( FireStoreDocumentField.STAFF_REGISTRATION_TOKEN to token))
            .addOnSuccessListener {
                result.invoke(Response.Success("Token Updated"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }
    }

    suspend fun getToken() : Response<String>{
        return try {
            val data = adminSettings.document(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN)
                .get()
                .await()
                .getString(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN)
            Response.Success(data ?: "")
        } catch (e: Exception){
//            Log.d("StaffRepository", "getToken: ")
            e.printStackTrace()
            Response.Error(e)
        }
    }

    fun getPendingStaff(result: (Flow<Response<List<Account>>>) -> Unit) = CoroutineScope(Dispatchers.IO).launch{
        userCollectionRef
            .whereEqualTo(FireStoreDocumentField.ACCOUNT_TYPE,AccountType.Staff)
            .whereEqualTo(FireStoreDocumentField.STAFF_STATUS,StaffPosition.Pending)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    result.invoke(flowOf(Response.Error(e)))
                    return@addSnapshotListener
                }

                val successResponse = run{
                    val accounts = snapshot?.toObjects<Account>()
                    Response.Success(accounts?: emptyList())
                }

                result.invoke(flowOf(successResponse))
            }
    }

    fun getStaffList(result: (Flow<Response<List<Account>>>) -> Unit) = CoroutineScope(Dispatchers.IO).launch{
        userCollectionRef
            .whereEqualTo(FireStoreDocumentField.ACCOUNT_TYPE,AccountType.Staff)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    result.invoke(flowOf(Response.Error(e)))
                    return@addSnapshotListener
                }

                val successResponse = run{
                    val accounts = snapshot?.toObjects<Account>()
                    Response.Success(accounts?: emptyList())
                }

                result.invoke(flowOf(successResponse))
            }
    }

}