package com.example.fyp.account_management.ui.fragment

import android.content.Intent
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
import com.example.fyp.MainActivity
import com.example.fyp.R
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentFirstBinding
import com.example.fyp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MainAuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLogin()

        binding.registerBtn.setOnClickListener{
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToUserRegisterFragment())
        }

        binding.loginBtn.setOnClickListener {
            viewModel.login(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
        }
        binding.forgotPasswordBtn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
        }
    }

    private fun observeLogin() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.loginState.collect() {
                when (it) {
                    is Response.Loading -> {
                        binding.loginBtn.isEnabled = false
                        binding.loginProgress.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.loginBtn.isEnabled = true
                        binding.loginProgress.visibility = View.GONE
                        it.exception.message?.let { it1 -> toast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data){
                            binding.loginProgress.visibility = View.GONE
                            binding.loginBtn.isEnabled = true
                            navigateMainPage()
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }

    private fun navigateMainPage() {
        val i = Intent(requireContext(), MainActivity::class.java)
        // set the new task and clear flags
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
    }

}