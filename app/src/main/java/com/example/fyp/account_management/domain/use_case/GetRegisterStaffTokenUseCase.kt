package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.StaffRepository
import javax.inject.Inject

class GetRegisterStaffTokenUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) {
    suspend operator fun invoke() = staffRepository.getToken()
}