package com.example.fyp.account_management.ui.fragment

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.ui.view_model.AuthViewModel
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.RegistrationEvent
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.FragmentEditAccountBinding
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@AndroidEntryPoint
class EditAccountFragment : Fragment() {

    private var _binding: FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    private var cal: Calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var callback: OnBackPressedCallback

    private var uri : Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null){
            uri = it
            setImage(it)
        }
    }

    private fun setImage(it: Uri) {
        binding.profileImgImageView.setImageURI(it)
        viewModel.onEvent(RegistrationEvent.ImageChanged(it))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this) {}
        callback.isEnabled = false

        viewModel.getSession()
        observeLoading()
        observeUpdate()
        observeRegistrationState()
        setUpDatePicker()

        binding.editImgBtn.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.cancelBtn.setOnClickListener {
            navigateBack()
        }
        binding.saveBtn.setOnClickListener {
            viewModel.onEvent(RegistrationEvent.Submit)
        }
        onFinishEditTextView(binding.firstNameEt,binding.firstNameEtl)
        binding.firstNameEt.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.FirstNameChanged(binding.firstNameEt.text.toString()))
        }
        onFinishEditTextView(binding.lastNameEt,binding.lastNameEtl)
        binding.lastNameEt.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.LastNameChanged(binding.lastNameEt.text.toString()))
        }
        onFinishEditTextView(binding.phoneEt,binding.phoneEtl)
        binding.phoneEt.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.PhoneChanged(binding.phoneEt.text.toString()))
        }
        onFinishEditTextView(binding.addressEt,binding.addressEtl)
        binding.addressEt.doAfterTextChanged {
            viewModel.onEvent(RegistrationEvent.AddressChanged(binding.addressEt.text.toString()))
        }
        binding.birthdayEt.isEnabled = false
        binding.birthdayEtl.setEndIconOnClickListener {
            val dateDialog = DatePickerDialog(
                requireContext(),
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
            val maxDate = LocalDate.now().minusYears(12)
            dateDialog.datePicker.maxDate = Date.from(maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).time
            dateDialog.show()
        }
    }

    private fun onEditTextView(tv: EditText, textInputLayout: TextInputLayout){
        tv.isEnabled = true
        textInputLayout.setEndIconDrawable(R.drawable.ic_done)
        textInputLayout.setEndIconOnClickListener {
            onFinishEditTextView(tv,textInputLayout)
        }
    }

    private fun onFinishEditTextView(tv: EditText, textInputLayout: TextInputLayout){
        tv.isEnabled = false
        textInputLayout.setEndIconDrawable(R.drawable.ic_edit)
        textInputLayout.setEndIconOnClickListener {
            onEditTextView(tv,textInputLayout)
        }
    }

    private fun setUpDatePicker(){
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.onEvent(RegistrationEvent.BirthdayChanged(cal.time))
                binding.birthdayEt.setText(sdf.format(cal.time))
            }

    }

    private fun observeLoading() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.loadingState.collect() {
                when (it) {
                    is Response.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        binding.progressBar.visibility = View.GONE
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (it.data){
                            loadData()
                        }
                    }
                }
            }
        }
    }

    private fun observeUpdate() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.updateResponse.collect() {
                when (it) {
                    is Response.Loading -> {
                        callback.isEnabled = true
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Response.Error -> {
                        callback.isEnabled = false
                        binding.progressBar.visibility = View.GONE
                        it.exception.message?.let { it1 -> errorToast(it1) }
                    }
                    is Response.Success -> {
                        if (it.data == Constants.AuthResult.SUCCESS_UPDATE){
                            callback.isEnabled = false
                            binding.progressBar.visibility = View.GONE
                            successToast("Account Information Updated!")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun successToast(s: String) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show()
    }

    private fun errorToast(msg: String) {
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show()
    }

    private fun observeRegistrationState() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.registerState.collect() {
                if (it.fnameError != null){
                    binding.firstNameEt.isEnabled = true
                    binding.firstNameEtl.error = it.fnameError
                } else {
                    binding.firstNameEtl.error = null
                }
                if (it.lnameError != null){
                    binding.lastNameEt.isEnabled = true
                    binding.lastNameEtl.error = it.lnameError
                } else {
                    binding.lastNameEtl.error = null
                }
                if (it.phoneError != null){
                    binding.phoneEt.isEnabled = true
                    binding.phoneEtl.error = it.phoneError
                } else {
                    binding.phoneEtl.error = null
                }
            }
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun loadData() {
        binding.apply {
            firstNameEt.setText(viewModel.user.first_name)
            lastNameEt.setText(viewModel.user.last_name)
            phoneEt.setText(viewModel.user.phone)
            addressEt.setText(viewModel.user.address)
            birthdayEt.setText(
                if (viewModel.user.birthday != null)
                    sdf.format(((viewModel.user.birthday) as Date).time)
                else
                    null
            )
            if (viewModel.user.profileUri != null){
                Glide.with(requireContext())
                    .load(viewModel.user.profileUri?.toUri())
                    .centerCrop()
                    .override(binding.profileImgImageView.width,binding.profileImgImageView.height)
                    .into(binding.profileImgImageView)
            }
        }
    }



}