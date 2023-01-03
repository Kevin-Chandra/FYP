package com.example.fyp.menucreator

import android.util.Log
import java.util.*

private const val TAG = "Food Class"

class Food(
    productId: String,
    name: String,
    price: Double,
    category: FoodCategory?,
    isModifiable : Boolean) : Product(productId,ProductType.FoodAndBeverage){

    init{
        if (productId.isNullOrBlank()) throw Exception("Product Id must not be blank")
        if (name.isBlank() || name.isEmpty()) throw Exception("Name must not be blank")
        if (price.isNaN() || price < 0.0) throw Exception("Price must not be null or less than 0")
    }

    private var _description : String? = null
    val description: String
        get() = _description?:""

    val productId: String
    get() = super._productId

    val productType:ProductType
        get() = super._type

    override var _name: String = name
        set(name) {
            if (name.isNotBlank() && name.isNotEmpty()) field = name
            else throw Exception("Name must not be blank")
        }
    val name : String
        get() = _name

    override var _price: Double = price
        set(price) {
            if (!price.isNaN() && price >= 0.0) field = price
            else throw Exception("Price must not be null")
        }
    val price : Double
        get() = _price

    private var _allTimeSales: Int = 0
    val allTimeSales : Int
        get() = _allTimeSales
    private var _isModifiable:Boolean = isModifiable
    val isModifiable : Boolean
        get() = _isModifiable
    private var _modifierList : TreeSet<String>? = null
    val modifierList : List<String>?
        get() = _modifierList?.toList()

    fun setFoodDescription(value: String){
        _description = value
    }

    fun addModifier(modifierId : String){
        if (!isModifiable) throw Exception("Food is not modifiable")
        if (_modifierList == null) _modifierList = TreeSet<String>()
        _modifierList!!.add(modifierId)
    }

    fun removeModifier(modifierId: String){
        if (_modifierList==null)
            throw Exception("Modifier list is not initialized")
        else
            if (_modifierList!!.remove(modifierId))
                Log.d(TAG,"Modifier removed")
            else
                Log.d(TAG,"Modifier not found!")
    }

    fun clearModifierList(){
        _modifierList?.clear()
    }





}