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
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentMainAccountBinding
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainAccountFragment : Fragment() {

    private var _binding: FragmentMainAccountBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by activityViewModels<MainAuthViewModel>()

    private var user: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMainAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDelete()

        authViewModel.getSession {
            user = it
            binding.accountNameTv.text = user?.first_name + " " +  user?.last_name
            if (user?.profileUri != null){
                Glide.with(requireContext())
                    .load(user?.profileUri)
                    .placeholder(R.drawable.ic_profile_placeholder_24)
                    .centerCrop()
                    .into(binding.profileIv)
            }
            if (user?.accountType == AccountType.Admin || user?.accountType == AccountType.Manager){
                binding.registerStaffBtn.visibility = View.VISIBLE
            } else {
                binding.registerStaffBtn.visibility = View.GONE
            }
        }

        binding.deleteBtn.setOnClickListener {
            val input = LayoutInflater.from(requireContext()).inflate(R.layout.password_input_dialog,getView() as ViewGroup,false)
            AlertDialog.Builder(requireContext())
                .setTitle("Enter password to confirm")
                .setView(input)
                .setPositiveButton("Submit") { _, _ ->
                    authViewModel.deleteAccount(input.findViewById<TextInputEditText>(R.id.password_et).text.toString())
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        binding.registerStaffBtn.setOnClickListener {
            println(user?.accountType)
            if (user!!.accountType != AccountType.Admin && user!!.accountType != AccountType.Manager){
                errorDialog("You have no permission!")
                return@setOnClickListener
            }
            findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToManageStaffFragment())
        }
        binding.editAccountBtn.setOnClickListener {
            when (user!!.accountType){
                AccountType.Customer -> findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToEditAccountFragment())
                else -> findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToEditAccountFragment())
            }
        }

        binding.changePasswordBtn.setOnClickListener {
            findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToUpdatePasswordFragment())
        }

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout {
                val i = Intent(requireContext(), AuthActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                activity?.finish()
                Toast.makeText(requireContext(),"Logged Out!" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeDelete() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            authViewModel.deleteAccountState.collect() {
                when (it) {
                    is Response.Loading -> {
                        binding.deleteBtn.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.deleteBtn.isEnabled = true
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == "Account deleted successfully!"){
                            binding.deleteBtn.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                            successToast(it.data)
                            navigateLogin()
                        }
                    }
                }
            }
        }
    }

    private fun navigateLogin() {
        val i = Intent(requireContext(), AuthActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun successToast(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(msg: String) {
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun errorDialog(msg: String){
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}