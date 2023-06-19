package com.example.fyp

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.FragmentMainBinding
import com.example.fyp.databinding.FragmentOnboardingBinding

class OnboardFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by activityViewModels<MainAuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((activity as MainActivity).shouldSkip()){
            findNavController().navigate(OnboardFragmentDirections.actionOnboardFragmentToMainFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingLav.setAnimation(R.raw.loading)

        authViewModel.getSession(true) {
            if (it != null){
                findNavController().navigate(OnboardFragmentDirections.actionOnboardFragmentToMainFragment())
            }
        }
    }
}