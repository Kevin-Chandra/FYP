package com.example.fyp.account_management.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fyp.R
import com.example.fyp.account_management.AuthActivity
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.ui.view_model.AuthViewModel
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.FragmentLoginBinding
import com.example.fyp.databinding.FragmentMainAccountBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainAccountFragment : Fragment() {

    private var _binding: FragmentMainAccountBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by activityViewModels<MainAuthViewModel>()

    private var user: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.getSession {
            user = it
            binding.accountNameTv.text = user?.first_name + " " +  user?.last_name
        }

        binding.editAccountBtn.setOnClickListener {
            findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToEditAccountFragment())
        }

        binding.changePasswordBtn.setOnClickListener {
            findNavController().navigate(MainAccountFragmentDirections.actionMainAccountFragmentToUpdatePasswordFragment())
        }

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                activity?.finish()
                Toast.makeText(requireContext(),"Logged Out!" , Toast.LENGTH_SHORT).show()
            }
        }
    }

}