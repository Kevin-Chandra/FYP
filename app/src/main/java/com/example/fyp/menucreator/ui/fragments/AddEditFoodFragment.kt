package com.example.fyp.menucreator.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.databinding.FragmentAddEditFoodBinding
import com.example.fyp.menucreator.ui.viewmodel.AddEditFoodViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddEditFoodFragment"

@AndroidEntryPoint
class AddEditFoodFragment : Fragment() {

    private var _binding: FragmentAddEditFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel : AddEditFoodViewModel by activityViewModels()

    private lateinit var checkedItems : BooleanArray
    private var modifierList: Array<String> = arrayOf()
    private val selectedItems = TreeSet<String>()

    private var isAddFoodObserved = false
    private var isUpdateFoodObserved = false

    private lateinit var command: String

    private var allowReset = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditFoodBinding.inflate(inflater, container, false)
        loadModifier()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        modifierList = viewModel.modifierMap.keys.toTypedArray()
//        println("List ${modifierList.size}")
        //checked items -> boolean array to track which elements is ticked


        arguments?.let {
            command = AddEditFoodFragmentArgs.fromBundle(it).command
            viewModel.reset()
            resetField()
            if (command.contentEquals(NavigationCommand.ADD)){
                addNewFood()
            } else if (command.contentEquals(NavigationCommand.EDIT)){
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

    private fun loadModifier() = lifecycleScope.launch{
        viewModel.modifiers.collect() {
            when (it) {
                is UiState.Success -> {
                    modifierList = it.data.keys.toTypedArray()
                    checkedItems = BooleanArray(modifierList.size)
                    allowReset = true
                    println("Modifier Loaded")
                }
                is UiState.Failure -> println(it.e)
                is UiState.Loading -> allowReset = false
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editFood(foodId: String?) {
        if (foodId.isNullOrEmpty()) errorDialog("FoodId invalid")
        else{
            println("editFood block")
            viewModel.initialize(foodId)
            binding.productIdEditText.focusable = View.NOT_FOCUSABLE
            observeLoadFood()
        }
    }

    private fun loadData() {
        try {
            binding.apply {
                productIdEditText.setText(viewModel.food.productId)
                productNameEditText.setText(viewModel.food.name)
                productPriceEditText.setText(viewModel.food.price.toString())
                productDescriptionEditText.setText(viewModel.food.description)
                modifierSwitch.isChecked = viewModel.food.modifiable
            }
            modifierLayoutEnabler(viewModel.food.modifiable)
            if (viewModel.food.modifierList.isNotEmpty() && viewModel.food.modifiable)
                for (i in viewModel.food.modifierList)
                    if (binding.modifiersLinearLayout.childCount < viewModel.food.modifierList.size)
                        addModifierRow(i)
        } catch (e: Exception){
            e.message?.let { errorDialog(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNewFood(){
        binding.productIdEditText.focusable = View.FOCUSABLE
        modifierLayoutEnabler(false)
    }

    private fun handleAddModifier() {
        val dialog = AlertDialog.Builder(requireContext())
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
                _,_ ->
            }
            .create()
        dialog.show()
    }

    private fun addModifierRow(id: String) {
        selectedItems.add(id)
        val index = binding.modifiersLinearLayout.childCount
        val inflater = layoutInflater.inflate(R.layout.row_add_edit_modifier, null)
        inflater.findViewById<TextView>(R.id.modifier_name_textView).text = viewModel.getModifier(id)?.name
        inflater.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
            selectedItems.remove(id)
            binding.modifiersLinearLayout.removeView(inflater)
            checkedItems[modifierList.indexOf(id)] = false
        }
        checkedItems[modifierList.indexOf(id)] = true
        binding.modifiersLinearLayout.addView(inflater,index)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetField() {
        if (command == NavigationCommand.EDIT){
            binding.productIdEditText.focusable = View.NOT_FOCUSABLE
        } else {
            binding.productIdEditText.text = null
        }
        binding.productNameEditText.text = null
        binding.productPriceEditText.text = null
        binding.productDescriptionEditText.text = null
        binding.modifierSwitch.isChecked = false
        binding.modifiersLinearLayout.removeAllViews()
        reset()
    }

    private fun reset(){
        if (allowReset) {
            for (i in checkedItems.indices) {
                checkedItems[i] = false
            }
            selectedItems.clear()
        }
    }

    private fun handleSaveOnClick(){
        try {
            val modifierList = arrayListOf<String>()
            if (binding.modifierSwitch.isChecked) {
                for (i in selectedItems)
                    modifierList.add(i)
            }

            if (command.contentEquals(NavigationCommand.ADD)) {
                if (!isAddFoodObserved){
                    isAddFoodObserved = !isAddFoodObserved
                    observeAddFood()
                }
                println("handle soc ${binding.productIdEditText.text.toString()}")
                viewModel.addNewFood(
                    binding.productIdEditText.text.toString(),
                    binding.productNameEditText.text.toString(),
                    binding.productPriceEditText.text.toString(),
                    binding.productDescriptionEditText.text.toString(),
                    binding.modifierSwitch.isChecked,
                    modifierList
                )

//                successToast("New food added successfully")
            } else {
                if (!isUpdateFoodObserved){
                    isUpdateFoodObserved = !isUpdateFoodObserved
                    observeUpdateFood()
                }
                viewModel.updateFood(
                    binding.productIdEditText.text.toString(),
                    binding.productNameEditText.text.toString(),
                    binding.productPriceEditText.text.toString(),
                    binding.productDescriptionEditText.text.toString(),
                    binding.modifierSwitch.isChecked,
                    modifierList
                )
//                successToast("Edited food saved")
            }

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

    private fun navigateBack() =
        findNavController().navigate(AddEditFoodFragmentDirections.actionAddEditFoodFragmentToFirstFragment())


    private fun observeAddFood() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addFoodResponse.collect() {
                when (it) {
                    is UiState.Loading -> {
                        println("Add food response loading")
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Add food response encountered error")
                        binding.progressBar2.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        println("Add food success data ${it.data}")
                        if (it.data){
                            binding.progressBar2.visibility = View.GONE
                            successToast("Food Added successfully")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }



    private fun observeUpdateFood() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.updateFoodResponse.collect() {
                when (it) {
                    is UiState.Loading -> {
                        println("Update food response loading")
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Update food response encountered error")
                        binding.progressBar2.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data){
                            binding.progressBar2.visibility = View.GONE
                            successToast("Update food successful is ${it.data}")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun observeLoadFood() = viewLifecycleOwner.lifecycleScope.launch{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.foodLoaded.collect() {
                println(it)
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.progressBar2.visibility = View.GONE
//                    binding.progressBar.hide()
//                    it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar2.visibility = View.GONE
                        loadData()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        reset()
    }
}