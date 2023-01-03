package com.example.fyp.menucreator

abstract class Product(protected var _productId: String,
                       protected var _type: ProductType) {

    protected open lateinit var _name: String

    protected open var _price: Double = 0.0
}