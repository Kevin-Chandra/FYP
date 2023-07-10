package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteOrderFromRemoteByIdUseCase @Inject constructor(
    private val getOrderFromRemoteByOrderIdUseCase: GetOrderFromRemoteByOrderIdUseCase,
    private val deleteOrderFromRemoteByOrderUseCase: DeleteOrderFromRemoteByOrderUseCase,
    ) {
    suspend operator fun invoke(account: Account, orderId: String, result: (Response<String>) -> Unit){
        getOrderFromRemoteByOrderIdUseCase(orderId){
            if (it is Response.Success){
                CoroutineScope(Dispatchers.IO).launch{
                    deleteOrderFromRemoteByOrderUseCase(account,it.data,result)
                }
            }
        }
    }
}