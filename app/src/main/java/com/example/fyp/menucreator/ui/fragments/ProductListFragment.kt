package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.menucreator.ui.adapter.ProductListItemAdapter
import com.example.fyp.databinding.FragmentProductListBinding
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.viewmodel.FoodListingViewModel
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel : FoodListingViewModel by activityViewModels()
    private val accountViewModel by viewModels<MainAuthViewModel>()

    private lateinit var foodAdapter : ProductListItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.getSession {
            if (it != null && (it.accountType == AccountType.Admin || it.accountType == AccountType.Manager))
                binding.fabAddFood.visibility = View.VISIBLE
        }

        foodAdapter = ProductListItemAdapter{
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(it.productId,ProductType.FoodAndBeverage)
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.foods.collect() {
                when (it) {
                    is UiState.Success -> {foodAdapter.submitList(it.data.toMutableList())
                        binding.progressBar.visibility = View.GONE}
                    is UiState.Failure -> println(it.e)
                    is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        binding.recyclerView.adapter = foodAdapter

        binding.fabAddFood.setOnClickListener{
            val action = FirstFragmentDirections.actionFirstFragmentToAddEditFoodFragment(Constants.Command.ADD,null)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}