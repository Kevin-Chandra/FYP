package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdByReturnUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.data.repository.remote.TableRepository
import com.example.fyp.pos.domain.UpdateFoodAllTimeSalesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class CheckoutTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
    private val orderRepository: OrderRepository,
    private val getOrderItems: GetOrderItemFromRemoteByOrderIdByReturnUseCase,
    private val updateFoodAllTimeSalesUseCase: UpdateFoodAllTimeSalesUseCase,
    ) {
    suspend operator fun invoke(table: Table, order: Order,result: (Response<String>) -> Unit) {
        if (table.tableStatus != TableStatus.Ongoing){
            result(Response.Error(Exception("Table Invalid State!")))
            return
        }
        if (table.currentOrder.isNullOrEmpty()){
            result(Response.Error(Exception("Current Order is empty!")))
            return
        }
        val list = mutableListOf<OrderItem>()

        CoroutineScope(Dispatchers.IO).launch {
            val job = async { getOrderItems(table.currentOrder) }
            val response = job.await()
            if (response is Response.Success){
                list.addAll(response.data)
            }
            if (list.any { it.orderItemStatus != OrderItemStatus.Finished }){
                result.invoke(Response.Error(Exception("Order Item is not fully finished!")))
                return@launch
            }
            CoroutineScope(Dispatchers.IO).launch {
                list.forEach {
                    launch{
                        updateFoodAllTimeSalesUseCase(it.foodId,it.quantity){}
                    }
                }
            }
            orderRepository.updateOrder(
                    order.copy(
                        orderStatus = OrderStatus.Finished,
                        orderFinishTime = Date(),
                    )
                    ){
                result(it)
            }
            tableRepository.checkoutTable(table.id){
                result(it)
            }
        }
    }
}