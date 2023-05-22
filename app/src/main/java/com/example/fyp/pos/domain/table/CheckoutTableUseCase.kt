package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import com.example.fyp.ordering_system.domain.remote_database.GetOrderFromRemoteByOrderIdUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdByReturnUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdUseCase
import com.example.fyp.ordering_system.domain.remote_database.SubmitOrderUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.repository.TableRepository
import com.example.fyp.pos.domain.UpdateFoodAllTimeSalesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class CheckoutTableUseCase @Inject constructor(
    private val tableRepository: TableRepository,
    private val orderRepository: OrderRepository,
    private val getOrderItems: GetOrderItemFromRemoteByOrderIdByReturnUseCase,
    private val getTaxUseCase: GetTaxUseCase,
    private val getServiceChargeUseCase: GetServiceChargeUseCase,
    private val updateFoodAllTimeSalesUseCase: UpdateFoodAllTimeSalesUseCase,
    ) {
    suspend operator fun invoke(table: Table, result: (Response<Order>) -> Unit) {
        if (table.currentOrder.isNullOrEmpty()){
            result(Response.Error(Exception("Current Order is empty!")))
            return
        }
        val list = mutableListOf<OrderItem>()
        var order : Order? = null

        val response = getOrderItems(table.currentOrder)
        if (response is Response.Success){
            list.addAll(response.data)
        }
        orderRepository.getOrder(table.currentOrder){
            if (it is Response.Success){
                order = it.data
            }
        }
        if (order == null){
            result(Response.Error(Exception("Order not found!")))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            list.forEach {
                launch{
                    updateFoodAllTimeSalesUseCase(it.foodId,it.quantity){}
                }
            }
        }

        val tax = getTaxUseCase()
        val serviceCharge = getServiceChargeUseCase()
        val subTotal = list.sumOf { it.price * it.quantity }

        order = order!!.copy(
            orderStatus = OrderStatus.Finished,
            orderFinishTime = Date(),
            subTotal = subTotal,
            taxPercentage = tax,
            serviceChargePercentage = serviceCharge,
            grandTotal = subTotal * (1 + serviceCharge + serviceCharge),
            tableNumber = table.name,
            pax = table.pax,
        )

        orderRepository.updateOrder(order!!){
            //TODO
        }

        tableRepository.checkoutTable(table.id){
            // TODO
        }
        result(Response.Success(order!!))
    }
}