package com.example.fyp.menucreator.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentAddEditModifierBinding
import com.example.fyp.menucreator.ui.adapter.ModifierItemAddEditAdapter
import com.example.fyp.menucreator.ui.viewmodel.AddEditModifierViewModel
import com.example.fyp.menucreator.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddEditModifierFragment : Fragment() {

    private var _binding: FragmentAddEditModifierBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditModifierViewModel by viewModels()
    private val authViewModel: MainAuthViewModel by activityViewModels()

    private lateinit var command: String

    private lateinit var adapter: ModifierItemAddEditAdapter

    private var list : MutableList<Triple<Pair<String,Boolean>,String,String>> = mutableListOf()
    private var errorlist : MutableList<Triple<String?,String?,String?>?> = mutableListOf()

    private var imageUri : Uri? = null

    private var allowBack = true

    private var disableRvTouch = false

    private var account: Account? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null) {
            imageUri = it
            viewModel.onEvent(AddEditModifierEvent.ImageChanged(it))
            setImage(it)
        }
    }

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
        observeAddEditModifier()
        observeError()

        authViewModel.getSession {
            account = it
        }

        arguments?.let {
            command = AddEditModifierFragmentArgs.fromBundle(it).command
            if (command.contentEquals(NavigationCommand.ADD)) {
                addNewModifier()
            } else if (command.contentEquals(NavigationCommand.EDIT)) {
                viewModel.initialize(AddEditModifierFragmentArgs.fromBundle(it).modifierId!!)
                editModifier()
            } else {
                throw Exception("Unknown command")
            }
        }

        //true to disable touch
        binding.itemRv.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return !disableRvTouch
            }
        })

        adapter = ModifierItemAddEditAdapter(
            onItemRemoveClicked = { _, pos ->
                println("${errorlist.size} -- $errorlist")
                println("${list.size} -- $list")
                val viewHolder = binding.itemRv.findViewHolderForAdapterPosition(pos)
                viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_id_etl)?.isErrorEnabled = false
                viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_name_etl)?.isErrorEnabled = false
                viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_price_etl)?.isErrorEnabled = false
                list.removeAt(pos)
                errorlist.removeAt(pos)
                viewModel.onEvent(AddEditModifierEvent.ItemErrorListChanged(errorlist))
                viewModel.onEvent(AddEditModifierEvent.ItemListChanged(list))
                binding.itemRv.adapter?.notifyItemRemoved(pos)
            },
            onIdChanged = { id , pos ->
                list[pos] = list[pos].copy(first = Pair(id,false))
                viewModel.onEvent(AddEditModifierEvent.ItemListChanged(list))
            },
            onNameChanged = { name , pos ->
                list[pos] = list[pos].copy(second = name)
                viewModel.onEvent(AddEditModifierEvent.ItemListChanged(list))
            },
            onPriceChanged = { price , pos ->
                list[pos] = list[pos].copy(third = price)
                viewModel.onEvent(AddEditModifierEvent.ItemListChanged(list))
            }
        )
        if (command.contentEquals(NavigationCommand.ADD))
            adapter.submitList(list)
        binding.itemRv.adapter = adapter

        binding.modifierIdEditText.doAfterTextChanged {
            viewModel.onEvent(AddEditModifierEvent.ProductIdChanged(binding.modifierIdEditText.text.toString()))
        }
        binding.modifierNameEditText.doAfterTextChanged {
            viewModel.onEvent(AddEditModifierEvent.NameChanged(binding.modifierNameEditText.text.toString()))
        }
        binding.isRequiredSwitch.setOnClickListener {
            viewModel.onEvent(AddEditModifierEvent.RequiredChanged(binding.isRequiredSwitch.isChecked))
        }
        binding.isMultipleChoiceSwitch.setOnClickListener {
            viewModel.onEvent(AddEditModifierEvent.MultipleChoiceChanged(binding.isMultipleChoiceSwitch.isChecked))
        }
        binding.addModifierItemButton.setOnClickListener {
            addItemRow()
        }
        binding.editImgBtn.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.saveButton.setOnClickListener {
            viewModel.onEvent(AddEditModifierEvent.Save(
                command.contentEquals(NavigationCommand.EDIT),account?:return@setOnClickListener)
            )
        }
        binding.resetButton.setOnClickListener {
            resetField()
        }
    }
    private fun addNewModifier() {
        binding.modifierTitleTextView.text = "Add New Modifier"
        uiEnabled(true)
        loadData()
    }
    private fun editModifier() {
        binding.modifierTitleTextView.text = "Edit Modifier"
        binding.modifierIdEditText.isEnabled = false
        observeLoadModifier()
    }

    private fun loadData() {
        binding.modifierIdEditText.setText(viewModel.addEditModifierState.value.productId)
        binding.modifierNameEditText.setText(viewModel.addEditModifierState.value.name)
        binding.isRequiredSwitch.isChecked = viewModel.addEditModifierState.value.isRequired
        binding.isMultipleChoiceSwitch.isChecked = viewModel.addEditModifierState.value.isMultipleChoice
        list = viewModel.addEditModifierState.value.itemList.toMutableList()
        viewModel.addEditModifierState.value.image?.let {
            Glide.with(requireContext())
                .load(it)
                .centerCrop()
                .into(binding.modifierIv)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val backDialog = MaterialAlertDialogBuilder(context)
                        .setTitle("Save Data?")
                        .setMessage("Do you want to save the current modifier info?")
                        .setPositiveButton("Save") { _, _ ->
                            viewModel.onEvent(AddEditModifierEvent.Save(command.contentEquals(NavigationCommand.EDIT),account?:return@setPositiveButton))
                        }
                        .setNegativeButton("Exit") { _, _ ->
                            findNavController().navigateUp()
                        }
                        .setNeutralButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(true)
                        .create()
                    if (allowBack)
                        backDialog.show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun addItemRow(data: Triple<Pair<String,Boolean>,String,String> = Triple(Pair("",false),"","")){
        list.add(data)
        errorlist.add(null)
        adapter.notifyItemInserted(list.size-1)
        viewModel.onEvent(AddEditModifierEvent.ItemErrorListChanged(errorlist))
        viewModel.onEvent(AddEditModifierEvent.ItemListChanged(list))
    }

    private fun navigateBack() = findNavController().navigateUp()

    private fun resetField() {
        if (command == NavigationCommand.EDIT){
            binding.modifierIdEditText.isFocusable = false
        } else {
            binding.modifierIdEditText.text = null
        }
        binding.modifierNameEditText.text = null
        binding.isRequiredSwitch.isChecked = false
        binding.isMultipleChoiceSwitch.isChecked = false
        binding.modifierIv.setImageURI(null)
        list.clear()
        errorlist.clear()
        viewModel.reset()
    }

    private fun successToast(msg: String) {
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(msg: String) {
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun errorDialog(msg: String) {
        binding.progressBar.visibility = View.GONE
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exception occurred!")
            .setMessage("Unable to save modifier\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun observeError() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addEditModifierState.collect() {
                if (it.productIdError != null){
                    binding.modifierName.error = it.nameError
                } else {
                    binding.modifierName.error = null
                }
                if (it.nameError != null){
                    binding.modifierId.error = it.productIdError
                } else {
                    binding.modifierId.error = null
                }
                errorlist = it.itemErrorList.toMutableList()
                println("aaaa" + errorlist)
                if (errorlist.isNotEmpty()) {
                    for (i in errorlist.indices){
                        val viewHolder = binding.itemRv.findViewHolderForAdapterPosition(i)
                        val idEt = viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_id_etl)
                        val nameEt = viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_name_etl)
                        val priceEt = viewHolder?.itemView?.findViewById<TextInputLayout>(R.id.item_price_etl)
                        if (errorlist[i]?.first != null){
                            idEt?.isErrorEnabled = true
                            idEt?.error = errorlist[i]?.first
                        } else {
                            idEt?.error = null
                            idEt?.isErrorEnabled = false
                        }
                        if (errorlist[i]?.second != null){
                            nameEt?.isErrorEnabled = true
                            nameEt?.error = errorlist[i]?.second
                        } else {
                            nameEt?.error = null
                            nameEt?.isErrorEnabled = false
                        }
                        if (errorlist[i]?.third != null){
                            priceEt?.isErrorEnabled = true
                            priceEt?.error = errorlist[i]?.third
                        } else {
                            priceEt?.error = null
                            priceEt?.isErrorEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun observeLoadModifier() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        uiEnabled(false)
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        uiEnabled(true)
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        uiEnabled(true)
                        loadData()
                        adapter.submitList(list)
                        for (i in list){
                            errorlist.add(null)
                        }
                        binding.progressBar.visibility = View.GONE
                        if (it.data == "Load Success"){
                            successToast("Modifier Loaded successfully")
                        }
                    }
                }
            }
        }
    }

    private fun observeAddEditModifier() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addEditModifierResponse.collect() { it ->
                when (it) {
                    is UiState.Loading -> {
                        uiEnabled(false)
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        uiEnabled(true)
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorToast(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data == MenuCreatorResponse.MODIFIER_ADD_SUCCESS || it.data == MenuCreatorResponse.MODIFIER_UPDATE_SUCCESS){
                            navigateBack()
                            binding.progressBar.visibility = View.GONE
                            uiEnabled(true)
                            successToast(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun uiEnabled(boolean: Boolean){
        allowBack = boolean
        disableRvTouch = boolean
        binding.apply {
            addModifierItemButton.isEnabled = boolean
            editImgBtn.isEnabled = boolean
            saveButton.isEnabled = boolean
            resetButton.isEnabled = boolean
            isMultipleChoiceSwitch.isEnabled = boolean
            isRequiredSwitch.isEnabled = boolean
            modifierNameEditText.isEnabled = boolean
            if (command == NavigationCommand.ADD)
                modifierIdEditText.isEnabled = boolean
        }
    }

    private fun setImage(uri: Uri){
        binding.modifierIv.setImageURI(uri)
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

}
