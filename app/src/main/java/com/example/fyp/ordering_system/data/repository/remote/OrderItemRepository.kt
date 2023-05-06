package com.example.fyp.ordering_system.data.repository.remote

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class OrderItemRepository @Inject constructor(
    private val database: FirebaseFirestore,
) {
    private val itemCollectionReference = database.collection(FireStoreCollection.ORDER_ITEM)

    suspend fun addItem(orderItem: OrderItem,result: (Response<String>) -> Unit){
        try {
            itemCollectionReference.add(orderItem)
                .addOnSuccessListener {
                    result.invoke(Response.Success("Order Item Added!"))
                }
                .addOnFailureListener {
                    result.invoke(Response.Error(it))
                }
                .await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun updateItem(orderItem: OrderItem,result: (Response<String>) -> Unit){
        try {
            val query = itemCollectionReference.whereEqualTo(FireStoreDocumentField.ORDER_ITEM_ID,orderItem.orderItemId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                itemCollectionReference.document(doc.id).set(
                    orderItem,
                    SetOptions.merge()
                )
            result.invoke(Response.Success("Order Item Updated!"))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun updateItemStatus(id: String, orderItemStatus: OrderItemStatus ,result: (Response<String>) -> Unit){
        try {
            val query = itemCollectionReference.whereEqualTo(FireStoreDocumentField.ORDER_ITEM_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Order item ID not found!")
            for (doc in query.documents)
                itemCollectionReference.document(doc.id).update(
                    FireStoreDocumentField.ORDER_ITEM_STATUS,
                    orderItemStatus
                )
            result.invoke(Response.Success("Order Item Status Updated!"))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun deleteOrderItem(id: String,result: (Response<String>) -> Unit){
        try {
            val query = itemCollectionReference.whereEqualTo(FireStoreDocumentField.ORDER_ITEM_ID,id)
                .get()
                .await()
            for (doc in query.documents)
                itemCollectionReference.document(doc.id).delete().await()
            result.invoke(Response.Success("Order Item Deleted!"))
        } catch (e : Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getOrderItem(id: String, result: (Response<OrderItem>) -> Unit){
        try {
            val query = itemCollectionReference.whereEqualTo(FireStoreDocumentField.PRODUCT_ID,id)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            for (doc in query.documents)
                result.invoke(Response.Success(doc.toObject<OrderItem>()!!))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getOrderItemListByStatus(orderItemStatus: OrderItemStatus) = callbackFlow<Response<List<OrderItem>>> {
        val snapshotListener = itemCollectionReference.whereEqualTo(FireStoreDocumentField.ORDER_ITEM_STATUS,orderItemStatus)
            .addSnapshotListener{ querySnapshot, e ->
                if (e != null ){
                    Response.Error(e)
                    return@addSnapshotListener
                }
                querySnapshot?.let{
                    val itemsResponse = run {
                        val items = querySnapshot.toObjects(OrderItem::class.java)
                        Response.Success(items)
                    }
                    trySend(itemsResponse)
                }
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    suspend fun getOrderItemListByOrderId(orderId: String, result: (Response<List<OrderItem>>) -> Unit) {
        try {
            val query = itemCollectionReference.whereEqualTo(FireStoreDocumentField.ORDER_ID,orderId)
                .get()
                .await()
            if (query.documents.isEmpty())
                throw Exception("Product Id not found!")
            val items = query.toObjects(OrderItem::class.java)
            result.invoke(Response.Success(items))
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

}