package com.example.fyp.menucreator.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.databinding.FragmentAddEditModifierBinding
import com.example.fyp.menucreator.ui.viewmodel.AddEditModifierViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditModifierFragment : Fragment() {

    private var _binding: FragmentAddEditModifierBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditModifierViewModel by viewModels()

    private var isAddObserved = false
    private var isEditObserved = false
    private var isEditItemObserved = false

    private lateinit var command: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        _binding = FragmentAddEditModifierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            command = AddEditModifierFragmentArgs.fromBundle(it).command
            resetField()
            viewModel.clearDeleteCache()
            if (command.contentEquals(NavigationCommand.ADD)) {
                addNewModifier()
            } else if (command.contentEquals(NavigationCommand.EDIT)) {
                viewModel.initialize(AddEditModifierFragmentArgs.fromBundle(it).modifierId!!)
                editModifier()
            } else {
                throw Exception("Unknown command")
            }
        }

        binding.addModifierItemButton.setOnClickListener {
            addModifierItemRow(null)
        }
        binding.saveButton.setOnClickListener {
            saveModifier()
        }
        binding.resetButton.setOnClickListener {
            resetField()
        }
    }
    private fun addNewModifier() {
        binding.modifierTitleTextView.text = "Add New Modifier"
    }
    private fun editModifier() {
        binding.modifierTitleTextView.text = "Edit Modifier"
        observeLoadModifier()
    }

    private fun loadData() {
        binding.modifierTitleTextView.text = "Edit Modifier"
        binding.modifierIdEditText.setText(viewModel.modifier.productId)
        binding.modifierNameEditText.setText(viewModel.modifier.name)
        binding.isRequiredSwitch.isChecked = viewModel.modifier.required
        binding.isMultipleChoiceSwitch.isChecked = viewModel.modifier.multipleChoice
        for (i in viewModel.modifier.modifierItemList) {
            if (viewModel.modifier.modifierItemList.size > binding.modifierItemLayout.childCount-1 )
                addModifierItemRow(i,true)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(context)
                        .setTitle("Save Data?")
                        .setMessage("Do you want to save the current modifier info?")
                        .setPositiveButton("Save") { _, _ ->
                            saveModifier()
                        }
                        .setNegativeButton("Exit") { _, _ ->
                            val action =
                                AddEditModifierFragmentDirections.actionAddEditModifierFragmentToFirstFragment()
                            findNavController().navigate(action)
                        }
                        .setNeutralButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun saveModifier() {
        viewModel.createNewItemList()
        try{
            if (binding.modifierItemLayout.childCount == 1) {
                throw (Exception("No Modifier Item Found"))
            }
            if (command.contentEquals(NavigationCommand.EDIT)) {
                if (!isEditObserved){
                    isEditObserved = !isEditObserved
                    observeEditModifier()
                }
                updateModifier()
            } else {
                addModifier()
            }
        } catch (e:Exception){
            e.message?.let { errorDialog(it) }
        }
    }
    private fun addModifier(){
        if (command == NavigationCommand.ADD){
            var check = true
            for (index in 0 until binding.modifierItemLayout.childCount - 1) {
                if (!check)
                    break
                val view = binding.modifierItemLayout[index]
                val id = view.findViewById<EditText>(R.id.row_modifier_item_id).text.toString()
                val name = view.findViewById<EditText>(R.id.modifier_item_name).text.toString()
                val price = view.findViewById<EditText>(R.id.modifier_item_price).text.toString()
                viewModel.addItems(id, name, price){
                    check = it
                }
            }
            if (!isAddObserved){
                isAddObserved = !isAddObserved
                observeAddModifier()
//                observeAddItemModifier()
                observeAddItemFinishModifier()
            }
        }
    }

    private fun updateModifier() {
        //check Item is valid, if valid add to ItemMap in VM
        if (command == NavigationCommand.EDIT){
            if (!isEditItemObserved){
                isEditItemObserved = !isEditItemObserved
                observeUpdateModifierItem()
                observeEditFinishModifier()
            }
            for (index in 0 until binding.modifierItemLayout.childCount - 1) {
                val view = binding.modifierItemLayout[index]
                val idView = view.findViewById<EditText>(R.id.row_modifier_item_id)
                val id = idView.text.toString()
                val name = view.findViewById<EditText>(R.id.modifier_item_name).text.toString()
                val price = view.findViewById<EditText>(R.id.modifier_item_price).text.toString()
                val isEdit = !idView.isFocusable
                viewModel.updateItem(id, name, price, isEdit)
            }
        }

    }

    private fun addModifierToVm() {
        binding.apply {
            viewModel.addNewModifier(
                modifierIdEditText.text.toString(),
                modifierNameEditText.text.toString(),
                isMultipleChoiceSwitch.isChecked,
                isRequiredSwitch.isChecked,
                command == NavigationCommand.EDIT
            )
        }
    }

    private fun navigateBack() =
        findNavController().navigate(AddEditModifierFragmentDirections.actionAddEditModifierFragmentToFirstFragment())

    private fun addModifierItemRow(modifierId: String?, isEdit:Boolean = false) {
        val inflater = layoutInflater.inflate(R.layout.row_add_edit_modifier_item, null)
        val item = modifierId?.let { viewModel.getModifierItem(it) }
        if (modifierId != null && item != null) {
            inflater.findViewById<EditText>(R.id.row_modifier_item_id)
                .setText(item.productId)
            inflater.findViewById<EditText>(R.id.modifier_item_name)
                .setText(item.name)
            inflater.findViewById<EditText>(R.id.modifier_item_price)
                .setText(item.price.toString())
            if(isEdit)
                inflater.findViewById<EditText>(R.id.row_modifier_item_id).isFocusable = false
        }
        inflater.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
            viewModel.deleteModifierItem(inflater.findViewById<EditText>(R.id.row_modifier_item_id).text.toString())
            binding.modifierItemLayout.removeView(inflater)
        }
        binding.modifierItemLayout.addView(inflater, binding.modifierItemLayout.childCount - 1)
    }

    private fun resetField() {
        if (command == NavigationCommand.EDIT){
            binding.modifierIdEditText.isFocusable = false
        } else {
            binding.modifierIdEditText.text = null
        }
        binding.modifierNameEditText.text = null
        binding.isRequiredSwitch.isChecked = false
        binding.isMultipleChoiceSwitch.isChecked = false
        binding.modifierItemLayout.removeAllViews()
        binding.modifierItemLayout.addView(binding.addModifierItemButton)
    }

    private fun successToast(msg: String) {
        view?.let { Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show() }
    }

    private fun errorDialog(msg: String) {
        binding.progressBar.visibility = View.GONE
        AlertDialog.Builder(context)
            .setTitle("Exception occurred!")
            .setMessage("Unable to save modifier\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun observeLoadModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        loadData()
                        binding.progressBar.visibility = View.GONE
                        successToast("Modifier Loaded successfully")
                    }
                }
            }
        }
    }

    private fun observeAddModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.saveButton.isFocusable = false
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.saveButton.isFocusable = true
                        binding.saveButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        successToast("Modifier Added successfully")
                        navigateBack()
                        binding.saveButton.isFocusable = true
                        binding.saveButton.isEnabled = true
                    }
                }

            }
        }
    }

    private fun observeAddItemFinishModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addItemFinishResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.saveButton.isFocusable = false
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Add item finish response error")
                        binding.progressBar.visibility = View.GONE
                        binding.saveButton.isEnabled = true
                        binding.saveButton.isFocusable = true
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        println("my data : ${it.data}")
                        if (it.data == binding.modifierItemLayout.childCount-1) {
                            println("im inside")
                            binding.progressBar.visibility = View.GONE
                            addModifierToVm()
//                        }
                        }
                    }
                }
            }
        }
    }

    private fun observeEditModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.editResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.saveButton.isFocusable = false
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.saveButton.isFocusable = true
                        binding.saveButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        addModifier()
                    }
                }
            }
        }
    }

    //main update observer
    private fun observeEditFinishModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.editFinishResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.saveButton.isFocusable = false
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        binding.saveButton.isFocusable = true
                        binding.saveButton.isEnabled = true
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data){
                            binding.progressBar.visibility = View.GONE
                            binding.saveButton.isFocusable = true
                            binding.saveButton.isEnabled = true
                            successToast("Modifier successfully updated")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun observeUpdateModifierItem() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.editItemResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        binding.saveButton.isFocusable = false
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.saveButton.isFocusable = true
                        binding.saveButton.isEnabled = true
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data == binding.modifierItemLayout.childCount-1) {
                            addModifierToVm()
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

}
