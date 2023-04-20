package com.example.fyp.ordering_system.data.repository.remote

import com.example.fyp.menucreator.util.FireStoreCollection
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private val orderItemDatabase: OrderItemRepository
) {

    private val userCollectionRef = database.collection(FireStoreCollection.ORDER)



}