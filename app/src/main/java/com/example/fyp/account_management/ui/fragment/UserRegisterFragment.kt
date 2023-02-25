package com.example.fyp.account_management.ui.fragment

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.ui.view_model.AuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.RegistrationEvent
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentUserRegisterBinding
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.firebase.database.collection.LLRBNode
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class UserRegisterFragment : Fragment() {

    private var _binding: FragmentUserRegisterBinding? = null
    private val binding get() = _binding!!

    private var cal: Calendar = Calendar.getInstance()

    private val viewModel by activityViewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRegistration()
        setUpDatePicker()


        binding.registerBtn.setOnClickListener{
            viewModel.onEvent(RegistrationEvent.Submit)
        }
        if (viewModel.registerState.value.emailError != null){
            errorView(binding.emailEditTextLayout)
        }
        binding.emailEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.EmailChanged(binding.emailEditText.text.toString()))
        }
        binding.passwordEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.PasswordChanged(binding.passwordEditText.text.toString()))
        }
        binding.fnameEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.FirstNameChanged(binding.fnameEditText.text.toString()))
        }
        binding.lnameEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.LastNameChanged(binding.lnameEditText.text.toString()))
        }
        binding.phoneEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.PhoneChanged(binding.phoneEditText.text.toString()))
        }
        binding.addressEditText.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.AddressChanged(binding.addressEditText.text.toString()))
        }
    }

    private fun setUpDatePicker(){
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.onEvent(RegistrationEvent.BirthdayChanged(cal.time))
                updateDateInView()
            }

        binding.birthdayEditText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

    }
    private fun updateDateInView() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        binding.birthdayEditText.text = "Birthday : ${sdf.format(cal.time)}"
    }

    private fun observeRegistration() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.registerResponse.collect() {
                when (it) {
                    is Response.Loading -> {
                        println("Registration response loading")
                        binding.registerProgress.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        println("Add food response encountered error")
                        binding.registerProgress.visibility = View.GONE
                        println(it)
//                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is Response.Success -> {
                        println("Registration success data ${it.data}")
                        if (it.data == Constants.AuthResult.SUCCESS_SIGNUP){
                            binding.registerProgress.visibility = View.GONE
                            println("Registration successfully")
                            navigateMainPage()
                        }
                    }
                }
            }
        }
    }

    private fun errorView(view: View){
        view.setBackgroundColor(Color.RED)
    }

    private fun navigateMainPage() = findNavController().navigate(UserRegisterFragmentDirections.actionUserRegisterFragmentToMainFragment())

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}