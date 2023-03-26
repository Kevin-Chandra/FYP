package com.example.fyp.account_management.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.MainActivity
import com.example.fyp.R
import com.example.fyp.account_management.AuthActivity
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.adapter.PendingStaffAdapter
import com.example.fyp.account_management.ui.view_model.AdminViewModel
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.ui.view_model.StaffViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentMainAccountBinding
import com.example.fyp.databinding.FragmentManageStaffBinding
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random


@AndroidEntryPoint
class ManageStaffFragment : Fragment() {

    private var _binding: FragmentManageStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AdminViewModel>()
    private val staffViewModel by activityViewModels<StaffViewModel>()

    private var user: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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

            })

        viewLifecycleOwner.lifecycleScope.launchWhenStarted{
            staffViewModel.pendingAccounts.collect() {
                when (it) {
                    is Response.Success -> {
                        pendingStaffAdapter.submitList(it.data.toMutableList())
//                        binding.progressBar.visibility = View.GONE
                    }
                    is Response.Error ->{
//                        println(it.e)
                    }
                    is Response.Loading ->{
//                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.pendingStaffRv.adapter = pendingStaffAdapter

        onFinishEditTextView(binding.tokenEt,binding.tokenEtl,{
            binding.tokenEt.setText(generateRandomToken(10))
        },{
            viewModel.setToken(binding.tokenEt.text.toString())
        })
    }

    private fun generateRandomToken(length: Int): String{
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars[Random.nextInt(0, chars.length)] }
            .joinToString("")
    }

    private fun observeSetToken() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.setTokenResponse.collect() {
                when (it) {
                    is Response.Loading -> {
//                        binding.deleteBtn.isEnabled = false
//                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
//                        binding.progressBar.visibility = View.GONE
//                        binding.deleteBtn.isEnabled = true
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == "Token Updated")
                            successToast(it.data)
//                        if (it.data == "Account deleted successfully!"){
//                            binding.deleteBtn.isEnabled = true
//                            binding.progressBar.visibility = View.GONE
//                            successToast(it.data)
//                            navigateLogin()
//                        }
                    }
                }
            }
        }
    }

    private fun observeGetToken() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getTokenResponse.collect() {
                when (it) {
                    is Response.Loading -> {
//                        binding.deleteBtn.isEnabled = false
//                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
//                        binding.progressBar.visibility = View.GONE
//                        binding.deleteBtn.isEnabled = true
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        binding.tokenEt.setText(it.data)
//                        if (it.data == "Account deleted successfully!"){
//                            binding.deleteBtn.isEnabled = true
//                            binding.progressBar.visibility = View.GONE
//                            successToast(it.data)
//                            navigateLogin()
//                        }
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