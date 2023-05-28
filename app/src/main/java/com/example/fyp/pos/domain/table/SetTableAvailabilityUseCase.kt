package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class SetTableAvailabilityUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(tableId:String, status : TableStatus, result: (Response<String>) -> Unit) {
        tableRepository.updateTable(tableId, mapOf(FireStoreDocumentField.TABLE_STATUS to status),result)
    }
}