package com.example.fyp.account_management.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.account_management.ui.adapter.PendingStaffAdapter
import com.example.fyp.account_management.ui.adapter.StaffListAdapter
import com.example.fyp.account_management.ui.view_model.AdminViewModel
import com.example.fyp.account_management.ui.view_model.StaffViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentManageStaffBinding
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class ManageStaffFragment : Fragment() {

    private var _binding: FragmentManageStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AdminViewModel>()
    private val staffViewModel by activityViewModels<StaffViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentManageStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSetToken()
        observeGetToken()
        viewModel.getToken()

        val pendingStaffAdapter = PendingStaffAdapter({
            staffViewModel.acceptPendingStaff(it)
        },{
            staffViewModel.rejectPendingStaff(it)
          },{
            staffViewModel.loadStaff(it)
            findNavController().navigate(ManageStaffFragmentDirections.actionManageStaffFragmentToStaffDetailsFragment(Constants.Command.ADD))
            })

        val staffListAdapter = StaffListAdapter{
            staffViewModel.loadStaff(it)
            findNavController().navigate(ManageStaffFragmentDirections.actionManageStaffFragmentToStaffDetailsFragment(Constants.Command.EDIT))
        }

        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffViewModel.pendingAccounts.collect {
                    when (it) {
                        is Response.Success -> {
                            pendingStaffAdapter.submitList(it.data.toMutableList())
                            if (it.data.isEmpty())
                                showEmptyPendingText(true)
                            else
                                showEmptyPendingText(false)
                        }
                        is Response.Error -> {
                            Log.d("ManageStaffFragment", "onViewCreated: ${it.exception.message}")
                        }
                        is Response.Loading -> {

                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffViewModel.staffAccounts.collect { it ->
                    when (it) {
                        is Response.Success -> {
                            staffListAdapter.submitList(it.data.toMutableList().sortedBy { it1 -> it1.staffPosition })
                            if (it.data.isEmpty()){
                                showEmptyStaffText(true)
                            } else {
                                showEmptyStaffText(false)
                            }
                        }
                        is Response.Error -> {
                            Log.d("ManageStaffFragment", "onViewCreated: ${it.exception.message}")
                        }
                        is Response.Loading -> {

                        }
                    }
                }
            }
        }

        binding.filterCg.setOnCheckedStateChangeListener { group, checkedIds ->
            var text : String? = null
            checkedIds.forEach{
                text = group.findViewById<Chip>(it).text.toString()
            }
            staffListAdapter.filter.filter(text)
        }

        binding.pendingStaffRv.adapter = pendingStaffAdapter
        binding.staffListRv.adapter = staffListAdapter

        onFinishEditTextView(binding.tokenEt,binding.tokenEtl,{
            binding.tokenEt.setText(generateRandomToken(10))
        },{
            viewModel.setToken(binding.tokenEt.text.toString())
        })
    }

    private fun showEmptyPendingText(boolean: Boolean) {
        binding.apply {
            if (boolean){
                pendingStaffRv.visibility = View.GONE
                emptyPendingTv.visibility = View.VISIBLE
            }
            else {
                pendingStaffRv.visibility = View.VISIBLE
                emptyPendingTv.visibility = View.GONE
            }
        }
    }

    private fun showEmptyStaffText(boolean: Boolean) {
        binding.apply {
            if (boolean){
                staffListRv.visibility = View.GONE
                emptyStaffTv.visibility = View.VISIBLE
            }
            else {
                staffListRv.visibility = View.VISIBLE
                emptyStaffTv.visibility = View.GONE
            }
        }
    }

    private fun generateRandomToken(length: Int): String{
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars[Random.nextInt(0, chars.length)] }
            .joinToString("")
    }

    private fun observeSetToken() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.setTokenResponse.collect {
                when (it) {
                    is Response.Loading -> {
                    }
                    is Response.Error -> {
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == "Token Updated")
                            successToast(it.data)
                    }
                }
            }
        }
    }

    private fun observeGetToken() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getTokenResponse.collect {
                when (it) {
                    is Response.Loading -> {
                    }
                    is Response.Error -> {
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        binding.tokenEt.setText(it.data)
                    }
                }
            }
        }
    }

    private fun successToast(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(msg: String) {
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun onEditTextView(tv: EditText, textInputLayout: TextInputLayout, startFunction:(()->Unit)?,endFunction:(()->Unit)?){
        tv.isEnabled = true
        textInputLayout.setEndIconDrawable(R.drawable.ic_done)
        textInputLayout.setStartIconOnClickListener {
            startFunction?.invoke()
        }
        textInputLayout.setEndIconOnClickListener {
            endFunction?.invoke()
            onFinishEditTextView(tv,textInputLayout,startFunction, endFunction)
        }
    }

    private fun onFinishEditTextView(tv: EditText, textInputLayout: TextInputLayout, startFunction: (() -> Unit)?,endFunction:(()->Unit)?){
        tv.isEnabled = false
        textInputLayout.setEndIconDrawable(R.drawable.ic_edit)
        textInputLayout.setStartIconOnClickListener {
        }
        textInputLayout.setEndIconOnClickListener {
            onEditTextView(tv,textInputLayout,startFunction, endFunction)
        }
    }
}