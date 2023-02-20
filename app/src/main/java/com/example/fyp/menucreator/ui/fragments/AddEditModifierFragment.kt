package com.example.fyp.menucreator.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
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

@AndroidEntryPoint
class AddEditModifierFragment : Fragment() {

    private var _binding: FragmentAddEditModifierBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditModifierViewModel by viewModels()

    private var isAddObserved = false

    private lateinit var command: String

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditModifierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            command = AddEditModifierFragmentArgs.fromBundle(it).command
            if (command.contentEquals("add")) {
                addNewModifier()
            } else if (command.contentEquals("edit")) {
                editModifier(AddEditModifierFragmentArgs.fromBundle(it).modifierId)
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

    private fun editModifier(modifierId: String?) {
        viewModel.reset()
//        if (modifierId.isNullOrEmpty() || !viewModel.setModifier(modifierId)) errorDialog("ModifierId invalid")
//        else
        loadData()
    }

    private fun loadData() {
        binding.modifierTitleTextView.text = "Edit Modifier"
        binding.modifierIdEditText.setText(viewModel.modifier.productId)
        binding.modifierNameEditText.setText(viewModel.modifier.name)
        binding.isRequiredSwitch.isChecked = viewModel.modifier.required
        binding.isMultipleChoiceSwitch.isChecked = viewModel.modifier.multipleChoice
        for (i in viewModel.modifier.modifierItemList) {
            addModifierItemRow(i)
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
        count = 0
        try {
            viewModel.createNewItemList()
            if (binding.modifierItemLayout.childCount == 1) {
                viewModel.error(Exception("No Modifier Item found"))
            }
            for (index in 0 until binding.modifierItemLayout.childCount - 1) {
                val view = binding.modifierItemLayout[index]
                val id = view.findViewById<EditText>(R.id.modifier_item_id).text.toString()
                val name = view.findViewById<EditText>(R.id.modifier_item_name).text.toString()
                val price = view.findViewById<EditText>(R.id.modifier_item_price).text.toString()
                viewModel.addItems(id, name, price)
            }
            if (command.contentEquals(NavigationCommand.ADD)) {
                if (!isAddObserved) {
                    isAddObserved = !isAddObserved
                    observeAddModifier()
                    observeAddItemModifier()
                    observeAddItemFinishModifier()
                }

            } else {
                Log.d("Test", "edit command")

                viewModel.updateModifier()
                successToast("Edited modifier saved")
            }


        } catch (e: Exception) {
            e.message?.let { errorDialog(it) }
        }
    }

    private fun addModifierToVm() {
        binding.apply {
            viewModel.addNewModifier(
                modifierIdEditText.text.toString(),
                modifierNameEditText.text.toString(),
                isMultipleChoiceSwitch.isChecked,
                isRequiredSwitch.isChecked
            )
        }
    }

    private fun navigateBack() =
        findNavController().navigate(AddEditModifierFragmentDirections.actionAddEditModifierFragmentToFirstFragment())

    private fun addModifierItemRow(modifierId: String?) {
        val inflater = layoutInflater.inflate(R.layout.row_add_edit_modifier_item, null)
        if (modifierId != null) {
            viewModel.addModifierItemId(modifierId)
            inflater.findViewById<EditText>(R.id.modifier_item_id)
                .setText(viewModel.getModifierItem(modifierId)?.productId)
            inflater.findViewById<EditText>(R.id.modifier_item_name)
                .setText(viewModel.getModifierItem(modifierId)?.name)
            inflater.findViewById<EditText>(R.id.modifier_item_price)
                .setText(viewModel.getModifierItem(modifierId)?.price.toString())
        }
        inflater.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
            viewModel.deleteModifierItem(inflater.findViewById<EditText>(R.id.modifier_item_id).text.toString())
            binding.modifierItemLayout.removeView(inflater)
        }
        binding.modifierItemLayout.addView(inflater, binding.modifierItemLayout.childCount - 1)
    }

    private fun addNewModifier() {
        binding.modifierTitleTextView.text = "Add New Modifier"
        resetField()
    }

    private fun resetField() {
        binding.modifierIdEditText.text = null
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

//        private fun observe() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.addResponse.collect() {
//                    when (val first = it.first) {
//                        is UiState.Loading -> {
//                            println("Add modifier response loading")
//                            binding.progressBar.visibility = View.VISIBLE
//                        }
//                        is UiState.Failure -> {
//                            binding.progressBar.visibility = View.GONE
//                            first.e?.message?.let { it1 -> errorDialog(it1) }
//                        }
//                        is UiState.Success -> {
//                            binding.progressBar.visibility = View.GONE
//                            successToast("Food Added successfully")
//                            navigateBack()
//                        }
//                    }
//
//                }
//            }
//        }

    private fun observeAddModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        println("Add modifier response loading")
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Add modifier response error")
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        println("CHECKKKKKKKK")
                        binding.progressBar.visibility = View.GONE
                        successToast("Modifier Added successfully")
                        navigateBack()
                    }
                }

            }
        }
    }

    private fun observeAddItemModifier() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addItemResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        println("Add item response loading")
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Add item response error")
                        binding.progressBar.visibility = View.GONE
                        viewModel.deleteCache()
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        successToast("Modifier Item Added successfully")
//                        addModifierToVm()
//                        }
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
                        println("Add item finish response loading")
//                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        println("Add item finish response error")
//                        binding.progressBar.visibility = View.GONE
//                        viewModel.deleteCache()
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        println("my data : ${it.data}")
                        if (it.data == binding.modifierItemLayout.childCount-1) {
                            println("im inside")
//                            binding.progressBar.visibility = View.GONE
                            successToast("Modifier Item Finish")
                            addModifierToVm()
//                        }
                        }
                    }
                }
            }
        }
    }
}
