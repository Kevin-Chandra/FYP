package com.example.fyp.account_management.domain.use_case

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        account: Account,
        image : Uri?,
        result: (Response<String>) -> Unit
    ) = authRepository.registerUser(email,password,image,account,result)
}