package com.example.fyp.account_management.domain.use_case

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import javax.inject.Inject

class GetRegisterStaffTokenUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) {
    operator fun invoke(
        result: (Flow<Response<String>>) -> Unit
    ) {
        staffRepository.getToken(result)
    }
}