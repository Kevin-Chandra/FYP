package com.example.fyp.ordering_system.data.repository.remote

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import kotlin.Exception

class OrderRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private val orderItemDatabase: OrderItemRepository
) {

    private val orderCollectionRef = Firebase.firestore.collection(FireStoreCollection.ORDER)

    suspend fun addOrder(order: Order, result: (Response<String>) -> Unit){
        try {
            orderCollectionRef.add(order)
                .addOnSuccessListener {
                    result.invoke(Response.Success("Order Submitted!"))
                }
                .addOnFailureListener {
                    result.invoke(Response.Error(it))
                }
                .await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun updateOrder(order: Order,result: (Response<String>) -> Unit){
        try {
            val query = orderCollectionRef.whereEqualTo(FireStoreDocumentField.ORDER_ID,order.orderId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Order Id not found!")
            for (doc in query.documents)
                orderCollectionRef.document(doc.id).set(
                    order,
                    SetOptions.merge()
                )
            result.invoke(Response.Success("Order Updated!"))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun updateOrderStatus(orderId: String, orderStatus: OrderStatus, result: (Response<String>) -> Unit){
        try {
            val query = orderCollectionRef.whereEqualTo(FireStoreDocumentField.ORDER_ID,orderId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Order Id not found!")
            for (doc in query.documents)
                orderCollectionRef.document(doc.id).update(
                    FireStoreDocumentField.ORDER_STATUS,
                    orderStatus
                )
            result.invoke(Response.Success("Order Status Updated!"))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun deleteOrder(id: String,result: (Response<String>) -> Unit){
        try {
            val query = orderCollectionRef.whereEqualTo(FireStoreDocumentField.ORDER_ID,id)
                .get()
                .await()
            for (doc in query.documents)
                orderCollectionRef.document(doc.id).delete().await()
            result.invoke(Response.Success("Order deleted successfully!"))
        } catch (e : Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getOrder(id: String, result: (Response<Order>) -> Unit){
        try {
            val query = orderCollectionRef.whereEqualTo(FireStoreDocumentField.ORDER_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Order Id not found!")
            for (doc in query.documents)
                result.invoke(Response.Success(doc.toObject<Order>()!!))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getOrderStatusUpdate(id: String) = callbackFlow {
        val snapshotListener = orderCollectionRef
            .whereEqualTo(FireStoreDocumentField.ORDER_ID,id)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    Response.Error(e)
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                        val orderResponse = run {
                            try {
                                val orders = querySnapshot.toObjects<Order>()
                                val toSend = orders.first()
                                Response.Success(toSend)
                            }catch (e:Exception){
                                e.printStackTrace()
                                Response.Error(Exception("Order Deleted"))
                            }
                        }
                    trySend(orderResponse)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun getOrderByAccount(accountId: String, statusList: List<OrderStatus>) = callbackFlow<Response<List<Order>>>{
        val snapshotListener = orderCollectionRef
            .whereEqualTo(FireStoreDocumentField.ORDER_BY,accountId)
            .whereIn(FireStoreDocumentField.ORDER_STATUS,statusList)
            .orderBy(FireStoreDocumentField.ORDER_START_TIME,Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    Response.Error(e)
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    val orderResponse = run {
                        val orders = querySnapshot.toObjects<Order>()
                        Response.Success(orders)
                    }
                    trySend(orderResponse)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }


    suspend fun finishOrder(orderId: String, result: (Response<String>) -> Unit){
        try {
            val query = orderCollectionRef.whereEqualTo(FireStoreDocumentField.ORDER_ID,orderId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Order Id not found!")
            for (doc in query.documents)
                orderCollectionRef.document(doc.id).update(
                    mapOf(
                        FireStoreDocumentField.ORDER_STATUS to OrderStatus.Finished,
                        FireStoreDocumentField.ORDER_FINISH_TIME to Date()
                    )
                )
            result.invoke(Response.Success("Order Finished!"))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getOrderListByStatus(statusList: List<OrderStatus>, byLastHours: Long = 24) = callbackFlow<Response<List<Order>>> {
        val startTime = Date.from(LocalDateTime.now().minusHours(byLastHours).atZone(ZoneId.systemDefault()).toInstant())
        val snapshotListener = orderCollectionRef
            .whereGreaterThanOrEqualTo(FireStoreDocumentField.ORDER_START_TIME,startTime)
            .whereIn(FireStoreDocumentField.ORDER_STATUS,statusList)
            .orderBy(FireStoreDocumentField.ORDER_START_TIME,Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                e.printStackTrace()
                Response.Error(e)
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val orderResponse = run {
                    val orders = querySnapshot.toObjects(Order::class.java)
                    Response.Success(orders)
                }
                trySend(orderResponse)
            }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

}