package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String,
                                password:String,
                                result: (Response<Boolean>) -> Unit
    ) = authRepository.loginUser(email,password,result)
}