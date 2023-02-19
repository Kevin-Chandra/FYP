package com.example.fyp.database

import android.util.Log
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ProductDatabase {
    private val productCollectionRef = Firebase.firestore.collection("products")
    private val productDatabaseRef = Firebase.firestore.collection("data")

    private val foodDatabase = hashMapOf<String, Food>()
    private val modifierDatabase = hashMapOf<String, Modifier>()
    private val modifierItemDatabase = hashMapOf<String, ModifierItem>()

    fun insertFood(food : Food){
        if (foodDatabase.containsKey(food.productId)) throw Exception("Product ID already exist")
        foodDatabase[food.productId] = food
//        saveFoodToDatabase(food)
    }

    fun updateFood(food: Food, id: String){
        foodDatabase[id] = food
    }

    fun deleteFood(foodId: String){
        foodDatabase.remove(foodId)
    }

    fun deleteModifier(modifierId: String){
        modifierDatabase.remove(modifierId)
    }

    fun deleteModifierItem(modifierItemId: String){
        modifierItemDatabase.remove(modifierItemId)
    }

    fun insertModifierItem(item : ModifierItem){
        if (modifierItemDatabase.containsKey(item.productId)) throw Exception("Product ID [${item.productId}] already exist")
        modifierItemDatabase[item.productId] = item
    }

    fun updateModifierItem(oldId: String,modifierItem: ModifierItem){
        deleteModifierItem(oldId)
        insertModifierItem(modifierItem)
    }

    fun insertModifier(modifier : Modifier){
        if (modifierDatabase.containsKey(modifier.productId)) throw Exception("Product ID [${modifier.productId}] already exist")
        modifierDatabase[modifier.productId] = modifier
    }

    fun getFood(keyId : String) : Food? {
        return foodDatabase[keyId]
    }

    fun getModifier(keyId : String) : Modifier?{
        return modifierDatabase[keyId]
    }

    fun getModifierItem(keyId : String) : ModifierItem?{
        return modifierItemDatabase[keyId]
    }

    fun getFoodList() : List<Food>{
        return foodDatabase.values.toList()
    }

    fun getModifierList() : List<Modifier>{
        return modifierDatabase.values.toList()
    }

    fun getModifierKeyList() : List<String>{
        return modifierDatabase.keys.toList()
    }

    private fun saveFoodToDatabase(food: Food) = CoroutineScope(Dispatchers.IO).launch {
        try {
            productCollectionRef.add(food).await()
            Log.d("ProductDatabase","Success adding food")
//            Toast.makeText(applicationContext,"Success adding food",Toast.LENGTH_SHORT).show()
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Log.d("ProductDatabase", "${e.message}")
//                Toast.makeText(applicationContext,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveToDatabase() = CoroutineScope(Dispatchers.IO).launch {
        try {
            productDatabaseRef.add(foodDatabase).await()
            Log.d("ProductDatabase","Success uploading")
//            Toast.makeText(applicationContext,"Success adding food",Toast.LENGTH_SHORT).show()
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Log.d("ProductDatabase", "${e.message}")
//                Toast.makeText(applicationContext,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }
}