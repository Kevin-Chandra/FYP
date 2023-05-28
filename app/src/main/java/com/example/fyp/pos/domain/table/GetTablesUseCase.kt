package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.repository.remote.TableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTablesUseCase @Inject constructor(
    private val tableRepository: TableRepository,
) {
    suspend operator fun invoke(result: (Flow<Response<List<Table>>>) -> Unit) {
        result(tableRepository.getTables())
    }
}