package com.example.fyp.menucreator

private const val TAG = "Modifier Item Class"

class ModifierItem(
    productId: String,
    name:String,
    price:Double) : Product(productId,ProductType.ModifierItem){

    init {
        if (name.isEmpty() || name.isBlank()) throw Exception("Modifier Item Name must not be empty or blank")
        if (price.isNaN() || price < 0.0) throw Exception("Modifier Item Price must not be null or negative")
    }

    override var _name: String = name
    val name get() = _name

    override var _price: Double = price
    val price get() = _price

    val productId: String
        get() = _productId
}