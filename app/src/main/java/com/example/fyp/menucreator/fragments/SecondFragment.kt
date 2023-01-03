package com.example.fyp.menucreator.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.database.ProductDatabase
import com.example.fyp.databinding.FoodViewComponentBinding
import com.example.fyp.databinding.FragmentSecondBinding
import com.example.fyp.databinding.ModifierViewComponentBinding
import com.example.fyp.databinding.RowModifierItemBinding
import com.example.fyp.menucreator.Food
import com.example.fyp.menucreator.Modifier

private const val TAG = "Second Fragment"

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var productId:String
    private val menu = ProductDatabase

    private var _food: Food? = null
    private val food get() = _food!!

    private var _modifier:Modifier? = null
    private val modifier get() = _modifier!!

    private var _foodBinding: FoodViewComponentBinding? = null
    private val foodBinding get() = _foodBinding!!

    private var _modifierBinding: ModifierViewComponentBinding? = null
    private val modifierBinding get() = _modifierBinding!!

    private lateinit var action : NavDirections

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        arguments?.let {
            productId = SecondFragmentArgs.fromBundle(it).productId
            val type = SecondFragmentArgs.fromBundle(it).type
            if (type.contentEquals("fnb")) {
                _food = menu.getFood(productId)
                _foodBinding = FoodViewComponentBinding.inflate(inflater,container,false)
                action = SecondFragmentDirections.actionSecondFragmentToAddEditFoodFragment("edit",productId)
            } else {
                _modifier = menu.getModifier(productId)
                action = SecondFragmentDirections.actionSecondFragmentToAddEditModifierFragment("edit",productId)
            }

            _modifierBinding = ModifierViewComponentBinding.inflate(inflater,container,false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editFab.setOnClickListener{
            findNavController().navigate(action)
        }
        loadData()
    }

    private fun loadData(){
        //Base layout is fragment_second constraint layout
        if (_food != null)
            loadFoodData()
        else
            loadModifierData()
    }

    private fun loadModifierData() {
        //add modifier_view_component to the base layout
        binding.baseLayout.addView(modifierBinding.root)
        loadModifierItem(modifier)
    }

    private fun loadFoodData(){
        //add food_view_component layout to the base layout
        binding.baseLayout.addView(foodBinding.root)

        foodBinding.productNameTextView.text = food.name
        foodBinding.descriptionTextview.text = food.description
        foodBinding.priceTextview.text = food.price.toString()

        if (food.isModifiable && !food.modifierList.isNullOrEmpty()) {
            foodBinding.modifiersContainerLayout.addView(modifierBinding.root)
            for( modifierCode in food.modifierList!!)
                menu.getModifier(modifierCode)?.let { loadModifierItem(it) }
        }
        Log.d(TAG,_food?.modifierList.toString())
    }

    //Only call this function after initializing modifier binding
    private fun loadModifierItem(modifier: Modifier){
        modifierBinding.modifierNameTextview.text = modifier.name
        for (itemCode in modifier.modifierList) {
            val itemBinding = RowModifierItemBinding.inflate(layoutInflater,modifierBinding.root,false)
            val item = menu.getModifierItem(itemCode)
            itemBinding.modifierItemNameTextview.text = item?.name
            itemBinding.modifierItemPriceTextview.text = item?.price.toString()
            modifierBinding.modifierItemContainerLayout.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _foodBinding = null
        _modifierBinding = null
    }
}