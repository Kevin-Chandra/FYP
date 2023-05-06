package com.example.fyp.menucreator.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.FragmentAddEditFoodBinding
import com.example.fyp.databinding.RowAddEditModifierBinding
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.ui.activity.MenuCreatorActivity
import com.example.fyp.menucreator.ui.viewmodel.AddEditFoodViewModel
import com.example.fyp.menucreator.ui.viewmodel.FoodCategoryViewModel
import com.example.fyp.menucreator.util.AddEditFoodEvent
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddEditFoodFragment"

@AndroidEntryPoint
class AddEditFoodFragment : Fragment() {

    private var _binding: FragmentAddEditFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel : AddEditFoodViewModel by viewModels()
    private val catViewModel  by activityViewModels<FoodCategoryViewModel>()
    private val authViewModel  by activityViewModels<MainAuthViewModel>()

    private lateinit var checkedItems : BooleanArray
    private var modifierList: Array<String> = arrayOf()
    private val selectedItems = TreeSet<String>()

    private var categoryList: List<FoodCategory> = listOf()
    private var categoryString: MutableList<String> = mutableListOf()

    private lateinit var command: String

    private var account: Account? = null

    private var arrayAdapter: ArrayAdapter<String>? = null

    private var allowReset = false
    private var allowBack = true

    private var imageUri : Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null) {
            imageUri = it
            viewModel.onEvent(AddEditFoodEvent.ImageChanged(it))
            setImage(it)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditFoodBinding.inflate(inflater, container, false)
        (activity as MenuCreatorActivity).apply{
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        loadModifier()
        loadCategory()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFoodUpdate()
        observeError()
        //checked items -> boolean array to track which elements is ticked

        authViewModel.getSession {
            account = it
        }

        arguments?.let {
            command = AddEditFoodFragmentArgs.fromBundle(it).command
            resetField()
            if (command.contentEquals(NavigationCommand.ADD)){
                addNewFood()
            } else if (command.contentEquals(NavigationCommand.EDIT)){
                editFood(AddEditFoodFragmentArgs.fromBundle(it).foodId)
            } else {
                throw Exception("Unknown command")
            }
        }

        binding.apply {
            productIdEditText.doAfterTextChanged {
                viewModel.onEvent(AddEditFoodEvent.ProductIdChanged(productIdEditText.text.toString()))
            }
            productNameEditText.doAfterTextChanged {
                viewModel.onEvent(AddEditFoodEvent.NameChanged(productNameEditText.text.toString()))
            }
            productPriceEditText.doAfterTextChanged {
                viewModel.onEvent(AddEditFoodEvent.PriceChanged(productPriceEditText.text.toString()))
            }
            productDescriptionEditText.doAfterTextChanged {
                viewModel.onEvent(AddEditFoodEvent.DescriptionChanged(productDescriptionEditText.text.toString()))
            }
            categoryEt.doAfterTextChanged {
                viewModel.onEvent(AddEditFoodEvent.FoodCategoryChanged(categoryEt.text.toString()))
            }
            modifierSwitch.setOnClickListener {
                modifierLayoutEnabler(modifierSwitch.isChecked)
                viewModel.onEvent(AddEditFoodEvent.ModifiableChanged(modifierSwitch.isChecked))
            }
            saveButton.setOnClickListener {
                handleSaveClicked()
            }
            setImageBtn.setOnClickListener {
                getContent.launch("image/*")
            }
            resetButton.setOnClickListener {
                resetField()
            }
            deleteImgBtn.setOnClickListener {
                binding.imageView.setImageResource(R.drawable.ic_image)
            }
            addModifierButton.setOnClickListener{
                handleAddModifier()
            }
        }
    }

    private fun handleSaveClicked() {
        account?:return
        viewModel.onEvent(AddEditFoodEvent.Save(command == NavigationCommand.EDIT,account!!))
    }

    private fun setImage(uri: Uri){
        binding.imageView.setImageURI(uri)
    }

    private fun loadModifier() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.modifiers.collect() {
                when (it) {
                    is UiState.Success -> {
                        modifierList = viewModel.modifierMap.keys.toTypedArray()
                        checkedItems = BooleanArray(modifierList.size)
                        allowReset = true
                    }
                    is UiState.Failure -> {
                        println(it.e)
                    }
                    is UiState.Loading ->{
                        allowReset = false
                    }
                }
            }
        }
    }
    private fun loadCategory() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            catViewModel.categories.collect() {
                when (it) {
                    is UiState.Success -> {
                        categoryList = it.data.toList()
                        setDropdown()
                    }
                    is UiState.Failure -> println(it.e)
                    is UiState.Loading -> {}
                }
            }
        }
    }

    private fun setDropdown(){
        categoryString.clear()
        for (i in categoryList) {
            categoryString.add(i.name)
        }
        arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item,categoryString)
        binding.categoryEt.setAdapter(arrayAdapter)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    val dialog = MaterialAlertDialogBuilder(context)
                        .setTitle("Save Data?")
                        .setMessage("Do you want to save the current food info?")
                        .setPositiveButton("Save"){
                            _,_ -> handleSaveClicked()
                        }
                        .setNegativeButton("Exit"){
                                _, _ ->
                            findNavController().navigateUp()
                        }
                        .setNeutralButton("Cancel"){
                                dialog,_ -> dialog.dismiss()
                        }
                        .setCancelable(true)
                        .create()
                    if (allowBack)
                        dialog.show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }

    private fun editFood(foodId: String?) {
        if (foodId.isNullOrEmpty()) errorDialog("FoodId invalid")
        else{
            viewModel.initialize(foodId)
            binding.productIdEditText.focusable = View.NOT_FOCUSABLE
            binding.productIdEditText.isEnabled = false
            observeLoadFood()
        }
    }

    private fun loadData() {
        try {
            if (viewModel.addEditFoodState.value.image != null){
                Glide.with(requireContext())
                    .load(viewModel.addEditFoodState.value.image)
                    .placeholder(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(binding.imageView)
            } else {
                binding.imageView.setImageResource(R.mipmap.ic_launcher)
            }
            binding.apply {
                productIdEditText.setText(viewModel.addEditFoodState.value.productId)
                productNameEditText.setText(viewModel.addEditFoodState.value.name)
                productPriceEditText.setText(viewModel.addEditFoodState.value.price)
                productDescriptionEditText.setText(viewModel.addEditFoodState.value.description)
                categoryEt.setText(viewModel.addEditFoodState.value.foodCategory,false)
                modifierSwitch.isChecked = viewModel.addEditFoodState.value.isModifiable
                modifierLayoutEnabler(modifierSwitch.isChecked)
            }
            if (!viewModel.addEditFoodState.value.modifierList.isNullOrEmpty() && viewModel.addEditFoodState.value.isModifiable) {
                selectedItems.clear()
                selectedItems.addAll(viewModel.addEditFoodState.value.modifierList!!)
                for (i in viewModel.addEditFoodState.value.modifierList!!)
                    if (binding.modifiersLinearLayout.childCount < viewModel.addEditFoodState.value.modifierList!!.size)
                        addModifierRow(i)
            }

        } catch (e: Exception){
            e.message?.let { errorDialog(it) }
        }
    }

    private fun addNewFood(){
        binding.productIdEditText.focusable = View.FOCUSABLE
        binding.productIdEditText.isEnabled = true
        loadData()
    }

    private fun handleAddModifier() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Modifier")
            .setCancelable(false)
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
        viewModel.onEvent(AddEditFoodEvent.ModifierChanged(selectedItems.toList()))
        val index = binding.modifiersLinearLayout.childCount
        val addEditModifierRowBinding = RowAddEditModifierBinding.inflate(layoutInflater, binding.modifiersLinearLayout,false)
        val modifier = viewModel.getModifier(id)
        addEditModifierRowBinding.apply {
            modifierNameTextView.text = modifier?.name
            modifierIdTextView.text = modifier?.productId
            modifierRemoveButton.setOnClickListener {
                selectedItems.remove(id)
                viewModel.onEvent(AddEditFoodEvent.ModifierChanged(selectedItems.toList()))
                binding.modifiersLinearLayout.removeView(addEditModifierRowBinding.root)
                checkedItems[modifierList.indexOf(id)] = false
            }
        }
        checkedItems[modifierList.indexOf(id)] = true
        binding.modifiersLinearLayout.addView(addEditModifierRowBinding.root,index)
    }

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
        modifierLayoutEnabler(false)
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

    private fun successToast(msg: String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(msg: String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun errorDialog(msg: String){
        MaterialAlertDialogBuilder(requireContext())
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
        binding.modifiersLinearLayout.visibility = if (boolean) View.VISIBLE else View.GONE
        for (i in 0  until binding.modifiersLinearLayout.childCount){
            val child: View = binding.modifiersLinearLayout.getChildAt(i)
            child.findViewById<MaterialButton>(R.id.modifier_remove_button).isEnabled = boolean
            child.isEnabled = boolean
        }
        binding.modifiersLinearLayout.isEnabled = boolean
    }

    private fun navigateBack() = findNavController().navigateUp()

    private fun observeFoodUpdate() =  viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.addEditFoodResponse.collect() {
                println(it)
                when (it) {
                    is UiState.Loading -> {
                        uiEnabled(false)
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        uiEnabled(true)
                        binding.progressBar2.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorToast(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data == "Add Food Success" || it.data == "Update Food Success"){
                            uiEnabled(true)
                            binding.progressBar2.visibility = View.GONE
                            navigateBack()
                            successToast(it.data)
                        }
                    }
                }
            }
        }
    }



    private fun observeError() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addEditFoodState.collect() {
                if (it.nameError != null){
                    binding.productName.error = it.nameError
                } else {
                    binding.productName.error = null
                }
                if (it.productIdError != null){
                    binding.productId.error = it.productIdError
                } else {
                    binding.productId.error = null
                }
                if (it.priceError != null){
                    binding.productPrice.error = it.priceError
                } else {
                    binding.productPrice.error = null
                }
                if (it.foodCategoryError != null){
                    binding.categoryEtl.error = it.foodCategoryError
                } else {
                    binding.categoryEtl.error = null
                }
            }
        }
    }

    private fun observeLoadFood() = viewLifecycleOwner.lifecycleScope.launch{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.foodLoaded.collect() {
                when (it) {
                    is UiState.Loading -> {
                        uiEnabled(false)
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        uiEnabled(true)
                        binding.progressBar2.visibility = View.GONE
                    }
                    is UiState.Success -> {
                        loadData()
                        uiEnabled(true)
                        binding.progressBar2.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun uiEnabled(boolean: Boolean){
        allowBack = boolean
        binding.apply {
            productDescriptionEditText.isEnabled = boolean
            setImageBtn.isEnabled = boolean
            productNameEditText.isEnabled = boolean
            saveButton.isEnabled = boolean
            resetButton.isEnabled = boolean
            productPriceEditText.isEnabled = boolean
            categoryEtl.isEnabled = boolean
            modifierSwitch.isEnabled = boolean
            if (modifierSwitch.isChecked)
                addModifierButton.isEnabled = boolean
            for (i in 0 until modifiersLinearLayout.childCount){
                modifiersLinearLayout.getChildAt(i)
                    .findViewById<MaterialButton>(R.id.modifier_remove_button)
                    .isEnabled = boolean
            }
            if (command == NavigationCommand.ADD)
                productIdEditText.isEnabled = boolean
        }
    }


    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).apply{
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        reset()
    }
}