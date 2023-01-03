package com.example.fyp.menucreator.model

import android.content.ClipDescription
import androidx.lifecycle.ViewModel
import com.example.fyp.database.ProductDatabase
import com.example.fyp.menucreator.Food
import com.example.fyp.menucreator.Modifier
import com.example.fyp.menucreator.Product

class AddEditFoodModel : ViewModel() {
    private var productId : String? = null
    private var _food : Food? = null
    val food : Food
        get() = _food!!

    val menu = ProductDatabase

    fun createFood(productId: String,name: String, price:Double, modifiable:Boolean){
        val food = Food(productId,name,price,null,modifiable)
        _food = food
    }

    fun setFood(foodId: String): Boolean{
        _food = menu.getFood(foodId)
        productId = foodId
        return (_food != null)
    }

    fun setDescription(description: String){
        _food?.setFoodDescription(description)
    }

    fun addModifier(modifier: Modifier){
        _food?.addModifier(modifier.productId)
    }

    fun addModifierId(modifierId: String){
        _food?.addModifier(modifierId)
    }

    fun removeModifier(modifier: Modifier){
        _food?.removeModifier(modifier.productId)
    }

    fun removeModifierId(modifierId: String){
        _food?.removeModifier(modifierId)
    }

    fun resetModifierList(){
        _food?.clearModifierList()
    }

    fun saveFood() {
        _food?.let { menu.insertFood(it) }
    }

    fun updateFood(){
        productId?.let { menu.deleteFood(it) }
        saveFood()
    }

    fun deleteFood(){
        productId?.let { menu.deleteFood(it) }
    }

    fun getModifierKeyListFromDatabase(): List<String> {
        return menu.getModifierKeyList()
    }

    fun getModifier(id: String): Modifier? {
        return menu.getModifier(id)
    }

    fun reset() {
        productId = null
        _food = null
    }


}