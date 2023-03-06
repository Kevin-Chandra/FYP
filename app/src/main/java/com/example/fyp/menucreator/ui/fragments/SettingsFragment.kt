package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.databinding.FragmentFirstBinding
import com.example.fyp.databinding.FragmentMenuCreatorSettingsBinding
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.adapter.FoodCategoryAdapter
import com.example.fyp.menucreator.ui.adapter.ProductListItemAdapter
import com.example.fyp.menucreator.ui.viewmodel.FoodCategoryViewModel
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentMenuCreatorSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FoodCategoryViewModel>()

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
        shrinkView()

        val categoryAdapter = FoodCategoryAdapter{
            viewModel.deleteCategory(it.id)
        }

//        binding.recyclerView.adapter = categoryAdapter
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.categories.collect() {
                when (it) {
                    is UiState.Success -> {
                        categoryAdapter.submitList(it.data.toMutableList())
                    }
                    is UiState.Failure -> println(it.e)
                    is UiState.Loading ->{
//                    binding.progressBar.visibility = View.VISIBLE
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
            viewModel.addNewCategory(binding.newCatEt.text.toString())
        }
    }

    private fun observeAdd() = viewLifecycleOwner.lifecycleScope.launchWhenStarted{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.addState.collect() {
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

    private fun observeDelete() = viewLifecycleOwner.lifecycleScope.launchWhenStarted{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.deleteState.collect() {
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

    private fun successToast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
    }

    private fun shrinkView() {
        binding.newCatEtl.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun expandView() {
        binding.newCatEtl.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
    }
}