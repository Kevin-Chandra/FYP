package com.example.fyp.menucreator.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.databinding.FragmentAddEditFoodBinding
import com.example.fyp.menucreator.model.AddEditFoodModel
import com.google.android.material.snackbar.Snackbar
import java.util.*

private const val TAG = "AddEditFoodFragment"

class AddEditFoodFragment : Fragment() {

    private var _binding: FragmentAddEditFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditFoodModel by viewModels()

    private lateinit var checkedItems : BooleanArray
    private lateinit var modifierList: Array<String>
    private val selectedItems = TreeSet<String>()

    private lateinit var command: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //modifier list -> list of modifier key retrieved directly from database
        modifierList = viewModel.getModifierKeyListFromDatabase().toTypedArray()
        //checked items -> boolean array to track which elements is ticked
        checkedItems = BooleanArray(modifierList.size)

        arguments?.let {
            command = AddEditFoodFragmentArgs.fromBundle(it).command
            if (command.contentEquals("add")){
                addNewFood()
            } else if (command.contentEquals("edit")){
                editFood(AddEditFoodFragmentArgs.fromBundle(it).foodId)
            } else {
                throw Exception("Unknown command")
            }
        }

        binding.addModifierButton.setOnClickListener{
            handleAddModifier()
        }
        binding.resetButton.setOnClickListener {
            resetField()
        }
        binding.saveButton.setOnClickListener {
            handleSaveOnClick()
        }
        binding.modifierSwitch.setOnClickListener {
            modifierLayoutEnabler(binding.modifierSwitch.isChecked)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(context)
                        .setTitle("Save Data?")
                        .setMessage("Do you want to save the current food info?")
                        .setPositiveButton("Save"){
                            _,_ -> handleSaveOnClick()
                        }
                        .setNegativeButton("Exit"){
                                _, _ ->
                            val action = AddEditFoodFragmentDirections.actionAddEditFoodFragmentToFirstFragment()
                            findNavController().navigate(action)
                        }
                        .setNeutralButton("Cancel"){
                                dialog,_ -> dialog.dismiss()
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }

    private fun editFood(foodId: String?) {
        viewModel.reset()
        if (foodId.isNullOrEmpty() || !viewModel.setFood(foodId)) errorDialog("FoodId invalid")
        else loadData()
    }

    private fun loadData() {
        binding.productIdEditText.setText(viewModel.food.productId)
        binding.productNameEditText.setText(viewModel.food.name)
        binding.productPriceEditText.setText(viewModel.food.price.toString())
        binding.productDescriptionEditText.setText(viewModel.food.description)
        binding.modifierSwitch.isChecked = viewModel.food.isModifiable
        modifierLayoutEnabler(viewModel.food.isModifiable)
        if (viewModel.food.modifierList?.isEmpty() == false && viewModel.food.isModifiable)
            for (i in viewModel.food.modifierList!!)
                addModifierRow(i)
    }

    private fun addNewFood(){
        viewModel.reset()
        resetField()
        modifierLayoutEnabler(false)
    }

    private fun handleAddModifier() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Modifier")
            .setMultiChoiceItems(modifierList,checkedItems) { _, which, isChecked ->
                 // If the user checked the item, add it to the selected items
                 if (isChecked)
                    checkedItems[which] = true
            }
            .setPositiveButton("Done"){ _, _ ->
                selectedItems.clear()
                binding.modifiersLinearLayout.removeAllViews()
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        addModifierRow(modifierList[i])
                        //selectedItems contains modifier key
                    }
                }
            }
            .setNegativeButton("Cancel"){
                    _,_ -> {}
            }
            .create()
            .show()
    }

    private fun addModifierRow(string: String) {
        selectedItems.add(string)
        val index = binding.modifiersLinearLayout.childCount
        val inflater = layoutInflater.inflate(R.layout.row_add_edit_modifier, null)
        inflater.findViewById<TextView>(R.id.modifier_name_textView).text = viewModel.getModifier(string)?.name
        inflater.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
            selectedItems.remove(string)
            binding.modifiersLinearLayout.removeView(inflater)
            checkedItems[modifierList.indexOf(string)] = false
        }
        checkedItems[modifierList.indexOf(string)] = true
        binding.modifiersLinearLayout.addView(inflater,index)
    }

    private fun resetField() {
        viewModel.reset()
        binding.productIdEditText.text = null
        binding.productNameEditText.text = null
        binding.productPriceEditText.text = null
        binding.productDescriptionEditText.text = null
        binding.modifierSwitch.isChecked = false
        binding.modifiersLinearLayout.removeAllViews()
    }

    private fun reset(){
        for(i in checkedItems.indices){
            checkedItems[i] = false
        }
        selectedItems.clear()
    }

    private fun handleSaveOnClick(){
        try {
            viewModel.createFood(
                binding.productIdEditText.text.toString(),
                binding.productNameEditText.text.toString(),
                binding.productPriceEditText.text.toString().toDoubleOrNull()?:-1.0,
                binding.modifierSwitch.isChecked
            )
            viewModel.setDescription(binding.productDescriptionEditText.text.toString())

            if (viewModel.food.isModifiable) {
                viewModel.resetModifierList()
                for (i in selectedItems)
                    viewModel.addModifierId(i)
            }

            if (command.contentEquals("add")) {
                viewModel.saveFood()
                successToast("New food added successfully")
            } else {
                viewModel.updateFood()
                successToast("Edited food saved")
            }

            val action = AddEditFoodFragmentDirections.actionAddEditFoodFragmentToFirstFragment()
            findNavController().navigate(action)

        } catch (e:Exception){
            e.message?.let { errorDialog(it) }
        }
    }

    private fun successToast(msg: String){
        view?.let { Snackbar.make(it,msg,Snackbar.LENGTH_SHORT).show() }
    }

    private fun errorDialog(msg: String){
        AlertDialog.Builder(context)
            .setTitle("Exception occured")
            .setMessage("Unable to save food\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Ok") {
                    dialog, _ -> dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun modifierLayoutEnabler(boolean: Boolean){
        binding.addModifierButton.isEnabled  = boolean
        for (i in 0  until binding.modifiersLinearLayout.childCount){
            val child: View = binding.modifiersLinearLayout.getChildAt(i)
            child.findViewById<ImageButton>(R.id.remove_button).isEnabled = boolean
            child.isEnabled = boolean
        }
        binding.modifiersLinearLayout.isEnabled = boolean
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        reset()
    }
}