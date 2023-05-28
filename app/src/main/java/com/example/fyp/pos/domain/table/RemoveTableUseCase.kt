package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class RemoveTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(tableId:String,result: (Response<String>) -> Unit) {
        tableRepository.removeTable(tableId,result)
    }
}