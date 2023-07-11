package com.example.fyp.account_management.data.repository

import com.example.fyp.account_management.data.model.*
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StaffRepository @Inject constructor(
    database: FirebaseFirestore,
    private val imageDatabase: FirebaseStorage,
) {

    private val adminSettings = database.collection(FireStoreCollection.ADMIN_SETTINGS)
    private val userCollectionRef = database.collection(FireStoreCollection.USER)

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