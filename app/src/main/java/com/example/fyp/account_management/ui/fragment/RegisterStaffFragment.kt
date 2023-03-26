package com.example.fyp.account_management.ui.fragment

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.fyp.account_management.ui.view_model.StaffViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import com.example.fyp.account_management.util.StaffRegistrationEvent
import com.example.fyp.databinding.FragmentRegisterStaffBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@AndroidEntryPoint
class RegisterStaffFragment : Fragment() {

    private var _binding: FragmentRegisterStaffBinding? = null
    private val binding get() = _binding!!

    private var cal: Calendar = Calendar.getInstance()

    private val viewModel by activityViewModels<StaffViewModel>()

    private var uri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null){
            uri = it
            setImage(it)
        }
    }

    private fun setImage(it: Uri) {
        binding.profileImgImageView.setImageURI(it)
//        viewModel.onEvent(RegistrationEvent.ImageChanged(it))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRegistration()
        observeRegistrationState()
        setUpDatePicker()

        binding.editImgBtn.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.registerBtn.setOnClickListener{
            viewModel.onEvent(StaffRegistrationEvent.Submit)
        }
        binding.emailEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.EmailChanged(binding.emailEditText.text.toString()))
        }
        binding.regTokenEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.TokenChanged(binding.regTokenEditText.text.toString()))
        }
        binding.passwordEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.PasswordChanged(binding.passwordEditText.text.toString()))
        }
        binding.fnameEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.FirstNameChanged(binding.fnameEditText.text.toString()))
        }
        binding.lnameEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.LastNameChanged(binding.lnameEditText.text.toString()))
        }
        binding.phoneEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.PhoneChanged(binding.phoneEditText.text.toString()))
        }
        binding.addressEditText.doAfterTextChanged {
            viewModel.onEvent(StaffRegistrationEvent.AddressChanged(binding.addressEditText.text.toString()))
        }
    }

    private fun setUpDatePicker(){
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            viewModel.onEvent(StaffRegistrationEvent.BirthdayChanged(cal.time))
            updateDateInView()
        }

        binding.birthdayEditText.setOnClickListener {
            val dateDialog = DatePickerDialog(
                requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
            val maxDate = LocalDate.now().minusYears(12)
            dateDialog.datePicker.maxDate = Date.from(maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).time
            dateDialog.show()
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
                            successToast("Staff successfully registered!")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun observeRegistrationState() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.registerState.collect() {
                if (it.tokenError != null){
                    binding.regTokenEditTextLayout.error = it.tokenError.toString()

                } else {
                    binding.regTokenEditTextLayout.error = null
                }
                if (it.emailError != null){
                    binding.emailEditTextLayout.error = it.emailError.toString()

                } else {
                    binding.emailEditTextLayout.error = null
                }
                if (it.passwordError != null){
                    binding.passwordEditTextLayout.error = it.passwordError.toString()

                } else {
                    binding.passwordEditTextLayout.error = null
                }
                if (it.fnameError != null){
                    binding.fnameEditTextLayout.error = it.fnameError.toString()

                } else {
                    binding.fnameEditTextLayout.error = null
                }
                if (it.lnameError != null){
                    binding.lnameEditTextLayout.error = it.lnameError.toString()

                } else {
                    binding.lnameEditTextLayout.error = null
                }
                if (it.phoneError != null){
                    binding.phoneEditTextLayout.error = it.phoneError.toString()
                } else {
                    binding.phoneEditTextLayout.error = null
                }
            }
        }
    }
    private fun errorToast(msg : String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
    }

    private fun successToast(msg : String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}