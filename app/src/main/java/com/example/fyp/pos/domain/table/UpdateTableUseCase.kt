package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class UpdateTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(table : Table, result: (Response<String>) -> Unit) {
        tableRepository.updateTable(table,result)
    }
}