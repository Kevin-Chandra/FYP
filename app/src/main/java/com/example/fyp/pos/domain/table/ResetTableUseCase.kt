package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class ResetTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(tableId:String, result: (Response<String>) -> Unit) {
        tableRepository.updateTable(tableId, mapOf(
            FireStoreDocumentField.TABLE_LABEL to "",
            FireStoreDocumentField.PAX to 0,
            FireStoreDocumentField.CURRENT_ORDER to "",
            FireStoreDocumentField.TABLE_STATUS to TableStatus.Available
        ),result)
    }
}