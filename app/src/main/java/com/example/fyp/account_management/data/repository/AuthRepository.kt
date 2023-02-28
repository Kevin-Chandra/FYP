package com.example.fyp.account_management.data.repository

import com.example.fyp.account_management.data.Result
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.CustomerAccount
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreCollection
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore
) {

    private val userCollectionRef = database.collection(FireStoreCollection.USER)

    fun registerUser(
        email: String,
        password: String,
        user: Account,
        result: (Response<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    user.id = it.result.user?.uid?:""
                    updateProfile(user){ state ->
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

    private fun updateUserInfo(user: Account, result: (Response<String>) -> Unit) {
        val document = userCollectionRef.document(user.id)
        CoroutineScope(Dispatchers.IO).launch{
            try{
                document.set(user).await()
                result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }
    }

    fun updateProfile(newAccount: Account, result: (Response<String>) -> Unit) {
        if (auth.currentUser == null) result.invoke(Response.Error(java.lang.Exception("User Not Available")))
        auth.currentUser?.let {user->
            try{
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(newAccount.first_name + newAccount.last_name)
                    .build()
                updateUserInfo(newAccount,result)
                user.updateProfile(profileUpdate)
                    .addOnCompleteListener { it ->
                        if (it.isSuccessful)
                            result.invoke(Response.Success(Constants.AuthResult.SUCCESS_UPDATE))
                    }

            } catch (e: Exception) {
                result.invoke(Response.Error(e))
            }
        }


    }
    fun loginUser(
        email: String,
        password: String,
        result: (Response<Boolean>) -> Unit
    ){
        try{
            println("From authdb")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        println("Im here??")
                        result.invoke(Response.Success(true))
                    }
                }
                .addOnFailureListener {
                    println("Oh no")
                    result.invoke(Response.Error(it))
                }
        } catch (e: Exception) {
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

    suspend fun getSession() : Account?{
        return if (auth.currentUser == null)
            null
        else {
            val document = auth.currentUser?.uid?.let { userCollectionRef.document(it) }?.get()
                ?.await()
            document!!.toObject<CustomerAccount>()
        }
    }
}