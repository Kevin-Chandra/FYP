package com.example.fyp.account_management.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentFirstBinding
import com.example.fyp.databinding.FragmentForgotPasswordBinding
import com.example.fyp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MainAuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeReset()

        binding.submitBtn.setOnClickListener {
            viewModel.resetPassword(binding.emailEditText.text.toString())
        }

    }

    private fun observeReset() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.resetState.collect() {
                when (it) {
                    is Response.Loading -> {
//                        println("Login response loading")
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.progressBar.visibility = View.GONE
                        it.exception.message?.let { it1 -> toast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == Constants.AuthResult.SUCCESS_EMAIL_SENT){
                            binding.progressBar.visibility = View.GONE
                            toast("Email sent!")
                            navigateLoginPage()
                        }
                    }
                }
            }
        }
    }

    private fun navigateLoginPage() = findNavController().navigate(ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment())

    private fun toast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

}