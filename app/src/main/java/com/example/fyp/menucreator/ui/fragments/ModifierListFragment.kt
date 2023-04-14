package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.menucreator.ui.adapter.ModifierListItemAdapter
import com.example.fyp.databinding.FragmentModifierListBinding
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.ui.viewmodel.ModifierListingViewModel
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ModifierListFragment : Fragment() {

    private var _binding: FragmentModifierListBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<ModifierListingViewModel>()
    private val accountViewModel by activityViewModels<MainAuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentModifierListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.getSession {
            if (it != null && (it.accountType == AccountType.Admin || it.accountType == AccountType.Manager))
                if (this.lifecycle.currentState >= Lifecycle.State.STARTED)
                    binding.fabAddModifier.visibility = View.VISIBLE
        }

        val modifierAdapter = ModifierListItemAdapter(onItemClicked = {
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(
                it.productId,
                ProductType.Modifier
            )
            findNavController().navigate(action)
        })

        binding.recyclerView.adapter = modifierAdapter

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.modifiers.collect() {
                when (it) {
                    is UiState.Success -> {modifierAdapter.submitList(it.data.toMutableList())
                                            binding.progressBar.visibility = View.GONE}
                    is UiState.Failure -> println(it.e)
                    is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

//        binding.recyclerView.adapter = context?.let { ModifierListItemAdapter(it,menu.getModifierList()) }
        binding.fabAddModifier.setOnClickListener{
            val action = FirstFragmentDirections.actionFirstFragmentToAddEditModifierFragment("add",null)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}