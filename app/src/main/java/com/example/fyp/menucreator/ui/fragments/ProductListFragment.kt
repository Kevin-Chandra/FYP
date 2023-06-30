package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.R
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.databinding.FragmentProductListBinding
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.domain.food.SortedBy
import com.example.fyp.menucreator.ui.adapter.ProductListItemAdapter
import com.example.fyp.menucreator.ui.viewmodel.FoodListingViewModel
import com.example.fyp.menucreator.util.AddEditFoodEvent
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel : FoodListingViewModel by activityViewModels()
    private val accountViewModel by activityViewModels<MainAuthViewModel>()

    private lateinit var sortedByMap : Map<String,SortedBy>
    private var arrayAdapter : ArrayAdapter<String>? = null

    private lateinit var foodAdapter : ProductListItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.getSession {
            if (it?.accountType == AccountType.Admin || it?.accountType == AccountType.Manager)
                if (this.lifecycle.currentState >= Lifecycle.State.STARTED)
                    binding.fabAddFood.visibility = View.VISIBLE
        }

        binding.sortByEt.setText(viewModel.sortedBy.title)
        binding.sortByEt.doAfterTextChanged { text ->
            sortedByMap[text.toString()]?.let { viewModel.sortFood(it) }
        }

        sortedByMap = SortedBy.values().associateBy { it.title }

        foodAdapter = ProductListItemAdapter{
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(it.productId,ProductType.FoodAndBeverage)
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.sortedFoods.collect {
                when (it) {
                    is UiState.Success -> {
                        foodAdapter.submitList(it.data.toMutableList())
                        binding.progressBar.visibility = View.GONE
                    }
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

        binding.recyclerView.addOnScrollListener( object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0){
                    binding.fabAddFood.shrink()
                } else {
                    binding.fabAddFood.extend()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item,sortedByMap.keys.toList())
        binding.sortByEt.setAdapter(arrayAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}