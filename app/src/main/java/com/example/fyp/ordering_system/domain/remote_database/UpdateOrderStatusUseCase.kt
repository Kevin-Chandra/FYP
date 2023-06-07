package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(account:Account, id : String, orderStatus: OrderStatus, result: (Response<String>) -> Unit) {
        if (account.accountType !in listOf(AccountType.Manager,AccountType.Staff,AccountType.Admin)){
            result.invoke(Response.Error(Exception("You don't have permission!")))
            return
        }
        if (account.accountType == AccountType.Staff && (account.staffPosition == StaffPosition.Disabled || account.staffPosition == StaffPosition.Pending)){
            result.invoke(Response.Error(Exception("Your staff account is currently ${account.staffPosition}")))
            return
        }
        orderRepository.updateOrderStatus(id,orderStatus,result)
    }
}