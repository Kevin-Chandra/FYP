package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.TableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetTableAvailabilityUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(tableId:String, status : TableStatus, result: (Response<String>) -> Unit) {
        tableRepository.updateTable(tableId, mapOf(FireStoreDocumentField.TABLE_STATUS to status),result)
    }
}