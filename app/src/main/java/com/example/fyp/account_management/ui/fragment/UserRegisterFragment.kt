package com.example.fyp.account_management.ui.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.MainActivity
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
        observeRegistrationState()
        setUpDatePicker()

        binding.backBtn.setOnClickListener {
            findNavController().navigate(UserRegisterFragmentDirections.actionUserRegisterFragmentToLoginFragment())
        }
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
                        binding.registerProgress.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.registerProgress.visibility = View.GONE
                        it.exception.message?.let { it1 -> errorToast(it1) }
//                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == Constants.AuthResult.SUCCESS_SIGNUP){
                            binding.registerProgress.visibility = View.GONE
                            successToast("Registration success!")
                            navigateMainPage()
                        }
                    }
                }
            }
        }
    }

    private fun observeRegistrationState() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.registerState.collect() {
                if (it.emailError != null){
                    errorView(binding.emailErrorTv)
                    binding.emailErrorTv.text = it.emailError.toString()
                } else {
                    disableError(binding.emailErrorTv)
                }
                if (it.passwordError != null){
                    errorView(binding.passwordErrorTv)
                    binding.passwordErrorTv.text = it.passwordError.toString()
                } else {
                    disableError(binding.passwordErrorTv)
                }
                if (it.fnameError != null){
                    errorView(binding.fnameErrorTv)
                    binding.fnameErrorTv.text = it.fnameError.toString()
                } else {
                    disableError(binding.fnameErrorTv)
                }
                if (it.lnameError != null){
                    errorView(binding.lnameErrorTv)
                    binding.lnameErrorTv.text = it.lnameError.toString()
                } else {
                    disableError(binding.lnameErrorTv)
                }
                if (it.phoneError != null){
                    errorView(binding.phoneErrorTv)
                    binding.phoneErrorTv.text = it.phoneError.toString()
                } else {
                    disableError(binding.phoneErrorTv)
                }
            }
        }
    }

    private fun disableError(view: View){
        view.visibility = View.GONE
    }
    private fun errorView(view: View){
        view.visibility = View.VISIBLE
    }

    private fun errorToast(msg : String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun successToast(msg : String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
    }

    private fun navigateMainPage() = startActivity(
        Intent(requireContext(),
            MainActivity::class.java)
    )

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}