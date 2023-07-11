package com.example.fyp.account_management.domain.use_case

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class EditAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        newAccount: Account,
        image : Uri?,
        result: (Response<String>) -> Unit
    ) = authRepository.updateProfile(newAccount,image,result)
}