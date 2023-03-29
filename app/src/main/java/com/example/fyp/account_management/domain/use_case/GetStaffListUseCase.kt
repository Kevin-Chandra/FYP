package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStaffListUseCase @Inject constructor(
    private val staffRepository: StaffRepository
    ) {
    operator fun invoke(result: (Flow<Response<List<Account>>>) -> Unit) = staffRepository.getStaffList(result)
}