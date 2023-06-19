package com.example.fyp.menucreator.data.model


abstract class Product (
    open val productId: String,
    open val type: ProductType,
    open val name: String = "",
    open val price: Double = 0.0,
    )

enum class ProductType(val type:String) {
    FoodAndBeverage("fnb"),
    Modifier("modifier"),
    ModifierItem("modifierItem")
}