package com.example.fyp.menucreator

import java.util.*

private const val TAG = "Modifier Class"

class Modifier (
    productId: String,
    var name : String ,
    private var _isMultipleChoice : Boolean,
    private var _isRequired : Boolean) : Product(productId,ProductType.Modifier){

    val productId: String
        get() = super._productId

    val productType: ProductType
        get() = super._type

    override var _name: String = name
        get() = super._name

    private var _modifierList = TreeSet<String>()
    val modifierList: TreeSet<String>
        get() = _modifierList

    val isMultipleChoice : Boolean
        get() = _isMultipleChoice

    val isRequired : Boolean
        get() = _isRequired

    fun addItem(itemId: String){
        _modifierList.add(itemId)
    }


}