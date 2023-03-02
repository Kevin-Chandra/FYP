package com.example.fyp.account_management.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.account_management.ui.view_model.AuthViewModel
import com.example.fyp.account_management.ui.view_model.UpdatePasswordViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentEditAccountBinding
import com.example.fyp.databinding.FragmentMainAccountBinding
import com.example.fyp.databinding.FragmentUpdatePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import org.checkerframework.checker.units.qual.Length

@AndroidEntryPoint
class UpdatePasswordFragment : Fragment() {

    private var _binding: FragmentUpdatePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<UpdatePasswordViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSession()
        observeChangePass()

        binding.saveBtn.setOnClickListener {
            viewModel.updatePassword(
                binding.oldPasswordEt.text.toString(),
                binding.newPasswordEt.text.toString()
            )
        }

        binding.cancelBtn.setOnClickListener {
            navigateBack()
        }
    }

    private fun observeChangePass() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.changePassState.collect() {
                when (it) {
                    is Response.Loading -> {
                        binding.saveBtn.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.saveBtn.isEnabled = true
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == Constants.AuthResult.SUCCESS_UPDATE_PASSWORD){
                            binding.progressBar.visibility = View.GONE
                            binding.saveBtn.isEnabled = true
                            successToast(it.data)
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun navigateBack() = findNavController().navigate(UpdatePasswordFragmentDirections.actionUpdatePasswordFragmentToMainAccountFragment())

    private fun errorToast(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_LONG).show()
    }

    private fun successToast(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_SHORT).show()
    }

}