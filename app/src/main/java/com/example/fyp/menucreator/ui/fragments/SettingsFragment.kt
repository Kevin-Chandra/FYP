package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.fyp.ProductSettingsViewModel
import com.example.fyp.R
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentMenuCreatorSettingsBinding
import com.example.fyp.menucreator.ui.adapter.FoodCategoryAdapter
import com.example.fyp.menucreator.ui.viewmodel.FoodCategoryViewModel
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentMenuCreatorSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FoodCategoryViewModel>()

    private val settingsViewModel by viewModels<ProductSettingsViewModel>()

    private val accountViewModel by activityViewModels<MainAuthViewModel>()

//    private var account: Account? = null

    private var cardExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentMenuCreatorSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAdd()
        observeDelete()
        observeUpdateValue()
        shrinkView()

        observeValue(
            updateTax = {
                binding.taxValue.setText(String.format("%.2f",it * 100))
            },
            updateServiceCharge = {
                binding.scValue.setText(String.format("%.2f",it * 100))
            }
        )
        settingsViewModel.getSettings()

        val categoryAdapter = FoodCategoryAdapter{
            accountViewModel.getSession { account ->
                account?.let { it1 -> viewModel.deleteCategory(it1,it.id) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.categories.collect {
                when (it) {
                    is UiState.Success -> {
                        categoryAdapter.submitList(it.data.toMutableList())
                    }
                    is UiState.Failure -> println(it.e)
                    is UiState.Loading ->{
                    }
                }
            }
        }
        binding.recyclerView.adapter = categoryAdapter

        binding.viewMoreBtn.setOnClickListener {
            if (!cardExpanded){
                cardExpanded = !cardExpanded
                expandView()
            } else {
                cardExpanded = !cardExpanded
                shrinkView()
            }
        }

        binding.newCatEtl.setEndIconOnClickListener {
            accountViewModel.getSession { account ->
                account?.let { it1 -> viewModel.addNewCategory(it1, binding.newCatEt.text.toString()) }
            }
        }

        onFinishEditTextView(binding.scValue, binding.scValueEtl){ it1 ->
            accountViewModel.getSession { acc ->
                if (acc != null) {
                    settingsViewModel.setServiceChargePercentage(acc,it1)
                }
            }
        }

        onFinishEditTextView(binding.taxValue, binding.taxValueEtl){ it1 ->
            accountViewModel.getSession { acc ->
                if (acc != null) {
                    settingsViewModel.setTaxPercentage(acc,it1)
                }
            }
        }


    }

    private fun onEditTextView(tv: EditText, textInputLayout: TextInputLayout, onFinishEdit:(String) -> Unit){
        tv.isEnabled = true
        textInputLayout.setEndIconDrawable(R.drawable.ic_done)
        textInputLayout.setEndIconOnClickListener {
            onFinishEditTextView(tv,textInputLayout,onFinishEdit)
            onFinishEdit.invoke(tv.text.toString())
        }
    }

    private fun onFinishEditTextView(tv: EditText, textInputLayout: TextInputLayout,onFinishEdit:(String) -> Unit){
        tv.isEnabled = false
        textInputLayout.setEndIconDrawable(R.drawable.ic_edit)
        textInputLayout.setEndIconOnClickListener {
            onEditTextView(tv,textInputLayout,onFinishEdit)
        }
    }

    private fun observeValue( updateTax: (Double) -> Unit, updateServiceCharge: (Double) -> Unit) = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            combine(settingsViewModel.tax, settingsViewModel.serviceCharge){ tax, service ->
                updateTax.invoke(tax)
                updateServiceCharge.invoke(service)
            }.stateIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun observeAdd() = viewLifecycleOwner.lifecycleScope.launch{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addState.collect {
                when (it) {
                    is UiState.Loading -> {
                        binding.newCatEtl.isEnabled = false
                        binding.newCatEt.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.newCatEtl.isEnabled = true
                        binding.newCatEt.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorToast(it1) }
                    }
                    is UiState.Success -> {
                        binding.newCatEtl.isEnabled = true
                        binding.newCatEt.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        if(it.data){
                            binding.newCatEt.setText("")
                            successToast("New category added")
                        }
                    }
                }
            }
        }
    }

    private fun observeDelete() = viewLifecycleOwner.lifecycleScope.launch{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.deleteState.collect {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorToast(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if(it.data){
                            binding.newCatEt.setText("")
                            successToast("Category deleted")
                        }
                    }
                }
            }
        }
    }

    private fun observeUpdateValue() = viewLifecycleOwner.lifecycleScope.launch{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.updateState.collect {
                when (it) {
                    is Response.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.progressBar.visibility = View.GONE
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if(it.data.isNotEmpty()){
                            successToast(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun successToast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
    }

    private fun shrinkView() {
        binding.newCatEtl.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.viewMoreBtn.setIconResource(R.drawable.ic_expand_more)
    }

    private fun expandView() {
        binding.newCatEtl.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.viewMoreBtn.setIconResource(R.drawable.ic_expand_less)
    }
}