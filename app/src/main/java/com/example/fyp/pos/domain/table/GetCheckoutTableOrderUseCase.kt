package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdByReturnUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import javax.inject.Inject

class GetCheckoutTableOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val getOrderItems: GetOrderItemFromRemoteByOrderIdByReturnUseCase,
    private val getTaxUseCase: GetTaxUseCase,
    private val getServiceChargeUseCase: GetServiceChargeUseCase,
    ) {
    suspend operator fun invoke(table: Table, result: (Response<Order>) -> Unit) {
        if (table.tableStatus != TableStatus.Ongoing){
            result(Response.Error(Exception("Table Invalid State!")))
            return
        }
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

        val tax = getTaxUseCase()
        val serviceCharge = getServiceChargeUseCase()
        val subTotal = list.sumOf { it.price }

        order = order!!.copy(
            subTotal = subTotal,
            taxPercentage = tax,
            serviceChargePercentage = serviceCharge,
            grandTotal = subTotal * (1 + tax + serviceCharge),
            tableNumber = table.tableNumber.toString(),
            pax = table.pax,
        )
        result(Response.Success(order!!))
    }
}