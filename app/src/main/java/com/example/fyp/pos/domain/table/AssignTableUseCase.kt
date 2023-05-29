package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class AssignTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(tableId:String, pax: Int, label: String? = null, result: (Response<String>) -> Unit) {
        val newLabel = if (label.isNullOrEmpty()) null else label
        tableRepository.assignSeat(tableId,pax,newLabel,result)
    }
}