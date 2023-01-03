package com.example.fyp.menucreator.fragments

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
import androidx.appcompat.app.ActionBar
import androidx.core.view.get
import androidx.core.view.indices
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.databinding.FragmentAddEditModifierBinding
import com.example.fyp.menucreator.model.AddEditModifierModel
import com.google.android.material.snackbar.Snackbar

class AddEditModifierFragment : Fragment() {

    private var _binding: FragmentAddEditModifierBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditModifierModel by viewModels()

    private lateinit var command: String

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
            if (command.contentEquals("add")){
                addNewModifier()
            } else if (command.contentEquals("edit")){
                editModifier(AddEditModifierFragmentArgs.fromBundle(it).modifierId)
            } else {
                throw Exception("Unknown command")
            }
        }

        binding.addModifierItemButton.setOnClickListener{
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
        if (modifierId.isNullOrEmpty() || !viewModel.setModifier(modifierId)) errorDialog("ModifierId invalid")
        else loadData()
    }

    private fun loadData() {
        binding.modifierTitleTextView.text = "Edit Modifier"
        binding.modifierIdEditText.setText(viewModel.modifier.productId)
        binding.modifierNameEditText.setText(viewModel.modifier.name)
        binding.isRequiredSwitch.isChecked = viewModel.modifier.isRequired
        binding.isMultipleChoiceSwitch.isChecked = viewModel.modifier.isMultipleChoice
        for (i in viewModel.modifier.modifierList){
            addModifierItemRow(i)
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
                        .setMessage("Do you want to save the current modifier info?")
                        .setPositiveButton("Save"){
                                _,_ -> saveModifier()
                        }
                        .setNegativeButton("Exit"){
                                _, _ ->
                            val action = AddEditModifierFragmentDirections.actionAddEditModifierFragmentToFirstFragment()
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

    private fun saveModifier() {
        try {
            viewModel.createModifier(
                binding.modifierIdEditText.text.toString(),
                binding.modifierNameEditText.text.toString(),
                binding.isMultipleChoiceSwitch.isChecked,
                binding.isRequiredSwitch.isChecked)
            if (binding.modifierItemLayout.childCount < 2) throw Exception("Modifier Item list is empty!")
            viewModel.removeDeletedItemFromDatabase()

            if (command.contentEquals("add")) {
                Log.d("Test","add command")
                for (index in 0 until binding.modifierItemLayout.childCount-1){
                    val view = binding.modifierItemLayout[index]
                    val id = view.findViewById<EditText>(R.id.modifier_item_id).text.toString()
                    val name = view.findViewById<EditText>(R.id.modifier_item_name).text.toString()
                    val price = view.findViewById<EditText>(R.id.modifier_item_price).text.toString().toDoubleOrNull()?:-1.0
                    viewModel.addModifierItem(id,name, price)
                }
                viewModel.saveModifier()
                successToast("New modifier added successfully")
            } else {
                Log.d("Test","edit command")
                for (index in 0 until binding.modifierItemLayout.childCount-1){
                    val view = binding.modifierItemLayout[index]
                    val id = view.findViewById<EditText>(R.id.modifier_item_id).text.toString()
                    val name = view.findViewById<EditText>(R.id.modifier_item_name).text.toString()
                    val price = view.findViewById<EditText>(R.id.modifier_item_price).text.toString().toDoubleOrNull()?:-1.0
                    viewModel.updateModifierItem(id,name, price)
                }
                viewModel.updateModifier()
                successToast("Edited modifier saved")
            }

            val action = AddEditModifierFragmentDirections.actionAddEditModifierFragmentToFirstFragment()
            findNavController().navigate(action)

        } catch(e:Exception) {
            e.message?.let { errorDialog(it) }
        }
    }

    private fun addModifierItemRow(modifierId: String?) {
        val inflater = layoutInflater.inflate(R.layout.row_add_edit_modifier_item, null)
        if (modifierId != null){
            viewModel.addModifierItemId(modifierId)
            inflater.findViewById<EditText>(R.id.modifier_item_id).setText(viewModel.getModifierItem(modifierId)?.productId)
            inflater.findViewById<EditText>(R.id.modifier_item_name).setText(viewModel.getModifierItem(modifierId)?.name)
            inflater.findViewById<EditText>(R.id.modifier_item_price).setText(viewModel.getModifierItem(modifierId)?.price.toString())
        }
        inflater.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
            viewModel.deleteModifierItem(inflater.findViewById<EditText>(R.id.modifier_item_id).text.toString())
            binding.modifierItemLayout.removeView(inflater)
        }
        binding.modifierItemLayout.addView(inflater,binding.modifierItemLayout.childCount-1)
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

    private fun successToast(msg: String){
        view?.let { Snackbar.make(it,msg, Snackbar.LENGTH_SHORT).show() }
    }

    private fun errorDialog(msg: String){
        AlertDialog.Builder(context)
            .setTitle("Exception occurred!")
            .setMessage("Unable to save modifier\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Ok") {
                    dialog, _ -> dialog.dismiss()
            }
            .create()
            .show()
    }


}