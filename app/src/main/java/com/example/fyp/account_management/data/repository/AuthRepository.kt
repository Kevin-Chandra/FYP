package com.example.fyp.account_management.data.repository

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.fyp.account_management.data.model.*
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.FirebaseStorageReference
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.random.Random


class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val imageDatabase: FirebaseStorage,
) {

    private val userCollectionRef = database.collection(FireStoreCollection.USER)
    private val imageRef = imageDatabase.reference

    fun registerUser(
        email: String,
        password: String,
        image: Uri?,
        user: Account,
        result: (Response<String>) -> Unit,
    ) {
        try{
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
//                        user.id =
                        updateProfile(user.copy(id = it.result.user?.uid?:""),image){ state ->
                            when (state){
                                is Response.Success ->{
                                    result.invoke(Response.Success(Constants.AuthResult.SUCCESS_SIGNUP))
                                }
                                else -> {

                                }
                            }
                        }
                    } else {
                        try {
                            throw it.exception ?: Exception("Invalid authentication")
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            result.invoke(Response.Error(Exception("Authentication failed, Password should be at least 6 characters")))
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            result.invoke(Response.Error(Exception("Authentication failed, Invalid email entered")))
                        } catch (e: FirebaseAuthUserCollisionException) {
                            result.invoke(Response.Error(Exception("Authentication failed, Email already registered.")))
                        } catch (e: Exception) {
                            result.invoke(Response.Error(e))
                        }
                    }
                }
        } catch (e: Exception){
            if (e is IllegalArgumentException)
                result.invoke(Response.Error(IllegalArgumentException("Email/Password is blank")))
            else
                result.invoke(Response.Error(e))
        }
    }

    fun updatePassword(oldPass: String, newPass: String, result: (Response<String>) -> Unit){
        val user = auth.currentUser!!
        val credential = EmailAuthProvider.getCredential(user.email!!,oldPass)
        try{
            user.reauthenticate(credential)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        user.updatePassword(newPass)
                            .addOnCompleteListener { update ->
                            if (update.isSuccessful)
                                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE_PASSWORD))
                            else
                                result.invoke(Response.Error(Exception("Password Not Updated")))
                        }
                    } else {
                        result.invoke(Response.Error(Exception("Failed to authenticate user!")))
                    }
                }.addOnFailureListener {
                    result.invoke(Response.Error(it))
                }
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

//    fun updateEmail(oldEmail : String, newEmail: String, password: String, result: (Response<String>) -> Unit){
//        val credential = EmailAuthProvider.getCredential(oldEmail,password)
//        val user = auth.currentUser!!
//
//        user.reauthenticate(credential)
//            .addOnCompleteListener{
//                if (it.isSuccessful){
//                    user.updateEmail(newEmail)
//                        .addOnCompleteListener { update ->
//                            if (update.isSuccessful){
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    val account = getSession()?.copy(email = newEmail)
//                                    if (account != null) {
//                                        updateProfile(account){
//                                            result.invoke(Response.Success("Email Updated!"))
//                                        }
//                                    }
//                                }
//
//                            }
//                            else
//                                result.invoke(Response.Error(Exception("Email Not Updated")))
//                        }
//                } else {
//                    result.invoke(Response.Error(Exception("Failed to authenticate user!")))
//                }
//            }.addOnFailureListener {
//                result.invoke(Response.Error(it))
//            }
//    }

    private fun updateUserInfo(user: Account, result: (Response<String>) -> Unit) {
        val document = userCollectionRef.document(user.id)
        CoroutineScope(Dispatchers.IO).launch{
            try{
                document.set(
                    user,
                    SetOptions.merge())
                    .await()
                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_FIELD_UPDATE))
            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }
    }
    private fun updateUserField(userId: String,field: String,data: Any?,result: (Response<String>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            val document = userCollectionRef.document(userId)
            try {
                document.update(field,data).await()
                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_FIELD_UPDATE))
            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }

    fun updateProfile(newAccount: Account, profileImage: Uri? = null, result: (Response<String>) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        if (auth.currentUser == null) {
            result.invoke(Response.Error(java.lang.Exception("User Not Available")))
            return@launch
        }
        var a: Deferred<Pair<String, String>?>? = null
        val parentJob = CoroutineScope(Dispatchers.IO).launch {
            if (profileImage != null) {
                a = async { uploadImage(profileImage, result) }
            }
            val updateUserJob = launch {
                updateUserInfo(newAccount, result)
            }
            auth.currentUser?.let { user ->
                try {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(newAccount.first_name + newAccount.last_name)
                        .build()
                    user.updateProfile(profileUpdate)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                if (profileImage != null) {
                                    launch {
                                        updateUserJob.join()
                                        launch {
                                            updateUserField(
                                                newAccount.id,
                                                FireStoreDocumentField.PROFILE_URI,
                                                a?.await()?.first,
                                                result
                                            )
                                        }
                                        launch {
                                            updateUserField(
                                                newAccount.id,
                                                FireStoreDocumentField.PROFILE_IMAGE_PATH,
                                                a?.await()?.second,
                                                result
                                            )
                                        }
                                    }
                                }
                            }
                        }

                } catch (e: Exception) {
                    result.invoke(Response.Error(e))
                }
            }
        }
        parentJob.invokeOnCompletion {
            if (it != null) {
                result.invoke(Response.Error(Exception(it.message)))
                return@invokeOnCompletion
            }
            println("Finished")
            result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
        }
    }
    fun loginUser(
        email: String,
        password: String,
        result: (Response<Boolean>) -> Unit,
    ){
        try{
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        result.invoke(Response.Success(true))
                    }
                }
                .addOnFailureListener {
                    when (it){
                        is FirebaseAuthInvalidCredentialsException -> result.invoke(Response.Error(Exception("Email/Password is invalid")))
                        is FirebaseAuthInvalidUserException -> result.invoke(Response.Error(Exception("No user found!")))
                        else -> result.invoke(Response.Error(it))
                    }
                }
        } catch (e: Exception){
            if (e is IllegalArgumentException)
                result.invoke(Response.Error(IllegalArgumentException("Email/Password is blank")))
            else
                result.invoke(Response.Error(e))
        }
    }

    fun logout(result: () -> Unit) {
        auth.signOut()
        result.invoke()
    }

    fun forgotPassword(email: String, result: (Response<String>) -> Unit) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        result.invoke(Response.Success(Constants.AuthResult.SUCCESS_EMAIL_SENT))
                    }
                }.addOnFailureListener {
                    result.invoke(Response.Error(Exception("Invalid Email!")))
                }
        } catch (e: Exception) {
            if (e is IllegalArgumentException)
                result.invoke(Response.Error(IllegalArgumentException("Email is blank")))
            else
                result.invoke(Response.Error(e))
        }
    }

    suspend fun getSession() : Account? {
        return if (auth.currentUser == null)
            null
        else {
            val document = auth.currentUser?.uid?.let { userCollectionRef.document(it) }?.get()
                ?.await()
            document?.toObject<Account>()
        }
    }

    private suspend fun uploadImage(image: Uri, result: (Response<String>) -> Unit) : Pair<String,String>? {
        val key = auth.currentUser?.uid
        val path = FirebaseStorageReference.PROfILE_IMAGE_REFERENCE + key
        return try {
            val uri = imageRef.child(path).putFile(image)
                .await()
                .storage
                .downloadUrl
                .await()
            result.invoke(Response.Success("Image Uploaded"))
            Pair(uri.toString(),path)
        } catch (e:java.lang.Exception){
            result.invoke(Response.Error(e))
            null
        }
    }

    fun deleteAccount(password: String, result: (Response<String>) -> Unit) = CoroutineScope(Dispatchers.IO).launch{
        val user = auth.currentUser!!
        val credential = EmailAuthProvider.getCredential(user.email!!,password)
        val account = userCollectionRef.document(user.uid).get().await()
        user.reauthenticate(credential)
            .addOnCompleteListener{
                if (it.isSuccessful){
                    if (account[FireStoreDocumentField.PROFILE_IMAGE_PATH] != null || account[FireStoreDocumentField.PROFILE_IMAGE_PATH] != "")
                        deleteImage(account.get(FireStoreDocumentField.PROFILE_IMAGE_PATH).toString(),result)
                    userCollectionRef.document(user.uid).delete()
                        .addOnSuccessListener {
                            result.invoke(Response.Success("Account data deleted!"))
                        }.addOnFailureListener { e ->
                            result.invoke(Response.Error(e))
                            throw CancellationException()
                        }
                    auth.currentUser!!.delete()
                        .addOnSuccessListener {
                            result.invoke(Response.Success("Account deleted successfully!"))
                        }.addOnFailureListener { e ->
                            result.invoke(Response.Error(e))
                            throw CancellationException()
                    }
                }
            }.addOnFailureListener {
                result.invoke(Response.Error(Exception("Authentication failed")))
            }
    }

    private fun deleteImage(path: String, result: (Response<String>) -> Unit){
        println("path $path")
        imageRef.child(path)
            .delete()
            .addOnSuccessListener {
                result.invoke(Response.Success("IMAGE SUCCESSFULLY DELETED"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }
    }

}