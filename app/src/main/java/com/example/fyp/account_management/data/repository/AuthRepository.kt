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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    user.id = it.result.user?.uid?:""
                    updateProfile(user,image){ state ->
                        when (state){
                            is Response.Success ->{
                                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
                            }
                            else -> {

                            }
                        }
                    }
                    result.invoke(Response.Success(Constants.AuthResult.SUCCESS_SIGNUP))
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
            .addOnFailureListener{
                result.invoke(Response.Error(it))
            }
    }

    private fun generateRandomPassword(length: Int): String{
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars[Random.nextInt(0, chars.length)] }
            .joinToString("")
    }

    fun updatePassword(oldPass: String, newPass: String, result: (Response<String>) -> Unit){
        val user = auth.currentUser!!
        val credential = EmailAuthProvider.getCredential(user.email!!,oldPass)
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
                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }
    }

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
                    if (it is FirebaseAuthInvalidCredentialsException)
                        result.invoke(Response.Error(Exception("Email/Password is invalid")))
                    else
                        result.invoke(Response.Error(it))
                }
        }catch (e: Exception) {
            result.invoke(Response.Error(e))
        }
    }

    fun logout(result: () -> Unit) {
        auth.signOut()
        result.invoke()
    }

    fun forgotPassword(email: String, result: (Response<String>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.invoke(Response.Success(Constants.AuthResult.SUCCESS_EMAIL_SENT))

                } else {
                    task.exception?.let { Response.Error(it) }?.let { result.invoke(it) }
                }
            }.addOnFailureListener {
                result.invoke(Response.Error(Exception("Authentication failed, Check email")))
            }
    }

    suspend fun getSession() : Account? {
        return if (auth.currentUser == null)
            null
        else {
            val document = auth.currentUser?.uid?.let { userCollectionRef.document(it) }?.get()
                ?.await()
            document?.toObject<Account>()
//            when (document?.get(FireStoreDocumentField.ACCOUNT_TYPE) ){
//                AccountType.Customer -> document.toObject<CustomerAccount>()
//                AccountType.Admin -> document.toObject<AdminAccount>()
//                AccountType.Manager -> document.toObject<CustomerAccount>()
//                is AccountType.KitchenStaff -> document.toObject<KitchenStaffAccount>()
//                is AccountType.Staff -> document.toObject<StaffAccount>()
//                else -> null
//            }
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