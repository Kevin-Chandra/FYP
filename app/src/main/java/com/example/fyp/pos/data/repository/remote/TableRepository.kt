package com.example.fyp.pos.data.repository.remote

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreCollection
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Exception

class TableRepository @Inject constructor(
    private val database: FirebaseFirestore,
) {

    private val tableCollectionRef = Firebase.firestore.collection(FireStoreCollection.TABLE)

    suspend fun addTable(table: Table, result: (Response<String>) -> Unit){
        try {
            tableCollectionRef.add(table)
                .addOnSuccessListener {
                    result.invoke(Response.Success("Table Added!"))
                }
                .addOnFailureListener {
                    result.invoke(Response.Error(it))
                }
                .await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun removeTable(tableId: String, result: (Response<String>) -> Unit){
        try {
            val table = tableCollectionRef.document(tableId).get().await().toObject<Table>()
            if (table == null){
                result.invoke(Response.Error(Exception("Table Not Found!")))
            } else {
                if (!(table.tableStatus == TableStatus.Finished || table.tableStatus == TableStatus.Vacant)) {
                    result.invoke(Response.Error(Exception("Table Not Found!")))
                }
                if (!table.currentOrder.isNullOrEmpty()) {
                    result.invoke(Response.Error(Exception("Please remove any order associated before removing table")))
                }
                tableCollectionRef.document(tableId)
                    .delete()
                    .addOnSuccessListener {
                        result.invoke(Response.Success("Table Removed!"))
                    }
                    .addOnFailureListener {
                        result.invoke(Response.Error(it))
                    }
                    .await()
            }
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun newOrder(tableId: String, orderId: String, result: (Response<String>) -> Unit ){
        try {
            tableCollectionRef.document(tableId).update(
                mapOf(
                    FireStoreDocumentField.CURRENT_ORDER to orderId,
                    FireStoreDocumentField.TABLE_STATUS to TableStatus.Ongoing
                )
            ).addOnSuccessListener {
                result.invoke(Response.Success("New order assigned!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }.await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun assignSeat(tableId: String, pax: Int, result: (Response<String>) -> Unit ){
        try {
            tableCollectionRef.document(tableId).update(
                mapOf(
                    FireStoreDocumentField.PAX to pax,
                    FireStoreDocumentField.TABLE_STATUS to TableStatus.Occupied
                )
            ).addOnSuccessListener {
                result.invoke(Response.Success("Seat Assigned!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }.await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun updateTable(tableId: String, data: Map<String,Any>, result: (Response<String>) -> Unit ){
        try {
            tableCollectionRef.document(tableId).update(
                data
            ).addOnSuccessListener {
                result.invoke(Response.Success("Table Updated!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }.await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun checkoutTable(tableId: String, result: (Response<String>) -> Unit){
        try {
            tableCollectionRef.document(tableId).update(
                FireStoreDocumentField.TABLE_STATUS, TableStatus.Finished
            ).addOnSuccessListener {
                result.invoke(Response.Success("Table checkout complete!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }.await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun finishTable(tableId: String, result: (Response<String>) -> Unit){
        try {
            tableCollectionRef.document(tableId).update(
                mapOf(
                    FireStoreDocumentField.TABLE_STATUS to TableStatus.Vacant,
                    FireStoreDocumentField.CURRENT_ORDER to null
                )
            ).addOnSuccessListener {
                result.invoke(Response.Success("Table is now vacant!"))
            }.addOnFailureListener {
                result.invoke(Response.Error(it))
            }.await()
        } catch (e: Exception){
            result.invoke(Response.Error(e))
        }
    }

    suspend fun getTables() = callbackFlow<Response<List<Table>>>{
        val snapshotListener = tableCollectionRef
//            .orderBy(FireStoreDocumentField.T,Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    Response.Error(e)
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    val response = run {
                        val tables = querySnapshot.toObjects<Table>()
                        Response.Success(tables)
                    }
                    trySend(response)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

}