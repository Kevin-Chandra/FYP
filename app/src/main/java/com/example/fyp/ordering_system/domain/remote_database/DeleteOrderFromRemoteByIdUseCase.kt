package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteOrderFromRemoteByIdUseCase @Inject constructor(
    private val getOrderFromRemoteByOrderIdUseCase: GetOrderFromRemoteByOrderIdUseCase,
    private val deleteOrderFromRemoteByOrderUseCase: DeleteOrderFromRemoteByOrderUseCase,
    ) {
    suspend operator fun invoke(orderId: String, result: (Response<String>) -> Unit){
        getOrderFromRemoteByOrderIdUseCase(orderId){
            if (it is Response.Success){
                CoroutineScope(Dispatchers.IO).launch{
                    deleteOrderFromRemoteByOrderUseCase(it.data,result)
                }
            }
        }
    }
}