package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteOrderFromRemoteByOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val deleteOrderItemFromRemoteByIdUseCase: DeleteOrderItemFromRemoteByIdUseCase,
    ) {
    suspend operator fun invoke(account : Account,order: Order, result: (Response<String>) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        if (account.accountType !in listOf(AccountType.Manager, AccountType.Staff, AccountType.Admin)){
            result.invoke(Response.Error(Exception("You don't have permission!")))
            return@launch
        }
        if (account.accountType == AccountType.Staff && (account.staffPosition == StaffPosition.Disabled || account.staffPosition == StaffPosition.Pending)){
            result.invoke(Response.Error(Exception("Your staff account is currently ${account.staffPosition}")))
            return@launch
        }
        val childJob = mutableListOf<Job>()
        order.orderList.forEach{ item ->
            childJob.add(
                launch {
                    deleteOrderItemFromRemoteByIdUseCase(item,result)
                }
            )
        }
        childJob.forEach { it.join() }
        orderRepository.deleteOrder(order.orderId,result)
    }
}