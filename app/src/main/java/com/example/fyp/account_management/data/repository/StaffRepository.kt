package com.example.fyp.account_management.data.repository

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

class StaffRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val imageDatabase: FirebaseStorage,
) {

    private val adminSettings = database.collection(FireStoreCollection.ADMIN_SETTINGS)
    private val userCollectionRef = database.collection(FireStoreCollection.USER)
    private val imageRef = imageDatabase.reference


//
//    fun registerStaff(
//        email: String,
//        password: String,
//        user: Account,
//        result: (Response<String>) -> Unit,
//    ) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener {
//                if (it.isSuccessful){
//                    user.id = it.result.user?.uid?:""
//                    user.staffPosition = StaffPosition.Pending
//                    updateProfile(user){ state ->
//                        when (state){
//                            is Response.Success ->{
//                                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_SIGNUP))
//                            }
//                            is Response.Error -> {
//                                result.invoke(Response.Error(Exception("Something went wrong")))
//                            }
//                            else -> {}
//                        }
//                    }
////                    result.invoke(Response.Success(Constants.AuthResult.SUCCESS_SIGNUP))
//                } else {
//                    try {
//                        throw it.exception ?: Exception("Invalid authentication")
//                    } catch (e: FirebaseAuthWeakPasswordException) {
//                        result.invoke(Response.Error(Exception("Authentication failed, Password should be at least 6 characters")))
//                    } catch (e: FirebaseAuthInvalidCredentialsException) {
//                        result.invoke(Response.Error(Exception("Authentication failed, Invalid email entered")))
//                    } catch (e: FirebaseAuthUserCollisionException) {
//                        result.invoke(Response.Error(Exception("Authentication failed, Email already registered.")))
//                    } catch (e: Exception) {
//                        result.invoke(Response.Error(e))
//                    }
//                }
//            }
//            .addOnFailureListener{
//                result.invoke(Response.Error(it))
//            }
//    }
//
////    fun updateEmail(oldEmail : String, newEmail: String, password: String, result: (Response<String>) -> Unit){
////        val credential = EmailAuthProvider.getCredential(oldEmail,password)
////        val user = auth.currentUser!!
////
////        user.reauthenticate(credential)
////            .addOnCompleteListener{
////                if (it.isSuccessful){
////                    user.updateEmail(newEmail)
////                        .addOnCompleteListener { update ->
////                            if (update.isSuccessful){
////                                CoroutineScope(Dispatchers.IO).launch {
////                                    val account = getSession()?.copy(email = newEmail)
////                                    if (account != null) {
////                                        updateProfile(account){
////                                            result.invoke(Response.Success("Email Updated!"))
////                                        }
////                                    }
////                                }
////
////                            }
////                            else
////                                result.invoke(Response.Error(Exception("Email Not Updated")))
////                        }
////                } else {
////                    result.invoke(Response.Error(Exception("Failed to authenticate user!")))
////                }
////            }.addOnFailureListener {
////                result.invoke(Response.Error(it))
////            }
////    }
//
    private fun updateUserInfo(user: Account, result: (Response<String>) -> Unit) {
        val document = userCollectionRef.document(user.id)
        CoroutineScope(Dispatchers.IO).launch{
            try{
                document.set(
                    user,
                    SetOptions.merge())
                    .await()
                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }
    }
//
    fun updateProfile(newAccount: Account, profileImage: Uri? = null, result: (Response<String>) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        if (auth.currentUser == null) {
            result.invoke(Response.Error(java.lang.Exception("User Not Available")))
            return@launch
        }
        var temp : Pair<String,String>? = null
        if (profileImage != null){
                async {
                    uploadImage(profileImage){
                        when (it){
                            is Response.Success -> {
                                temp = it.data
                            }
                            is Response.Error -> {
                                result.invoke(Response.Error(it.exception))
                            }
                            else -> {}
                        }
                    }
                }.await()
            if (temp == null)
                return@launch
        }
        auth.currentUser?.let {user->
            try{
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(newAccount.first_name + newAccount.last_name)
                    .build()
                user.updateProfile(profileUpdate)
                    .addOnCompleteListener { it ->
                        if (it.isSuccessful) {
                            if (temp != null){
                                newAccount.profileUri = temp?.first
                                newAccount.profileImagePath = temp?.second
                            }
                            updateUserInfo(newAccount, result)
                            result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
                        }
                    }

            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }
    }

    private suspend fun uploadImage(image: Uri, result: (Response<Pair<String,String>>) -> Unit) {
        val key = auth.currentUser?.uid
        val path = FirebaseStorageReference.PROfILE_IMAGE_REFERENCE + key
        try {
            val uri = imageRef.child(path).putFile(image)
                .await()
                .storage
                .downloadUrl
                .await()
            result.invoke(Response.Success(Pair(uri.toString(),path)))
        } catch (e:java.lang.Exception){
            result.invoke(Response.Error(e))
        }
    }

//    private fun initializeSetting(){
//        adminSettings.add(mapOf(FireStoreDocumentField.ID to "Setting"))
//    }

    fun setToken(token: String, result: (Response<String>) -> Unit){
        adminSettings.document(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN)
            .set(mapOf( FireStoreDocumentField.STAFF_REGISTRATION_TOKEN to token))
            .addOnSuccessListener {
                result.invoke(Response.Success("Token Updated"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }
    }

    fun getToken(result: (Flow<Response<String>>) -> Unit){
        adminSettings.document(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN)
            .addSnapshotListener{ snapshot,e ->
                if (e != null) {
                    result.invoke(flowOf(Response.Error(e)))
                    return@addSnapshotListener
                }
                result.invoke(flowOf(Response.Success(snapshot?.get(FireStoreDocumentField.STAFF_REGISTRATION_TOKEN).toString())))
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

//
//    private fun deleteImage(path: String, result: (Response<String>) -> Unit){
//        imageRef.child(path)
//            .delete()
//            .addOnSuccessListener {
//                result.invoke(Response.Success("IMAGE SUCCESSFULLY DELETED"))
//            }.addOnFailureListener {
//                result.invoke(Response.Error(it))
//            }
//    }

}