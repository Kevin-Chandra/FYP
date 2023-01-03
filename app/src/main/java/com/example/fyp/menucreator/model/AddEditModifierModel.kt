package com.example.fyp.menucreator.model

import android.content.ClipDescription
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.fyp.database.ProductDatabase
import com.example.fyp.menucreator.Food
import com.example.fyp.menucreator.Modifier
import com.example.fyp.menucreator.ModifierItem
import com.example.fyp.menucreator.Product
import java.util.TreeSet
import kotlin.math.log

private const val TAG = "Modifier View Model"

class AddEditModifierModel : ViewModel() {
    private var productId : String? = null
    private var _modifier : Modifier? = null
    private val itemList = TreeSet<String>()
    private val deletedItem = TreeSet<String>()
    val modifier : Modifier
        get() = _modifier!!

    val menu = ProductDatabase

    fun createModifier(productId: String,name: String, isMultipleChoice:Boolean, isRequired:Boolean){
        val modifier = Modifier(productId,name, isMultipleChoice, isRequired)
        _modifier = modifier
    }

    fun saveModifier() {
        for(i in itemList)
            _modifier!!.addItem(i)
        _modifier?.let { menu.insertModifier(it) }
    }

    fun updateModifier(){
        deleteModifier()
        saveModifier()
    }

    fun deleteModifier(){
        productId?.let { menu.deleteModifier(it) }
    }

    fun setModifier(modifierId: String): Boolean{
        _modifier = menu.getModifier(modifierId)
        productId = modifierId
        _modifier?.let { itemList.addAll(it.modifierList) }
        return (_modifier != null)
    }

    fun addModifierItem(productId: String,name: String,price: Double){
        val mi = ModifierItem(productId, name, price)
        menu.insertModifierItem(mi)
        itemList.add(mi.productId)
    }

    fun addModifierItemId(productId: String){
        itemList.add(productId)
    }

    fun deleteModifierItem(id: String?){
        itemList.remove(id)
        if (id != null) {
            deletedItem.add(id)
        }
    }

    fun getModifierItem(modifierItemId: String) : ModifierItem?{
        return menu.getModifierItem(modifierItemId)
    }

    fun reset() {
        productId = null
        _modifier = null
        itemList.clear()
        deletedItem.clear()
    }

    fun removeDeletedItemFromDatabase(){
        for (i in deletedItem)
            menu.deleteModifierItem(i)
    }

    fun updateModifierItem(id: String, name: String, price: Double) {
        if (itemList.contains(id))
            menu.deleteModifierItem(id)
        addModifierItem(id,name,price)
    }


}