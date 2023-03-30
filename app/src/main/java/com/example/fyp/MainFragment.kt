package com.example.fyp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.AccountActivity
import com.example.fyp.account_management.AuthActivity
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.FragmentMainBinding
import com.example.fyp.menucreator.ui.activity.MenuCreatorActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by viewModels<MainAuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSession()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.helloTv.visibility = View.INVISIBLE
            binding.shimmerHello.startShimmer()
            binding.shimmerHello.visibility = View.VISIBLE
            getSession()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.accountBtn.setOnClickListener {
            startActivity(Intent(requireContext(),AccountActivity::class.java))
        }

        binding.menuCreatorButton.setOnClickListener {
            val i = Intent(requireContext(), MenuCreatorActivity::class.java)
            startActivity(i)
        }


    }

    private fun getSession() = authViewModel.getSession {
        if (it != null){
            binding.shimmerHello.stopShimmer()
            binding.shimmerHello.visibility = View.GONE
            binding.helloTv.visibility = View.VISIBLE
            binding.helloTv.text = "Hello, ${it.first_name}"
        }
    }
}