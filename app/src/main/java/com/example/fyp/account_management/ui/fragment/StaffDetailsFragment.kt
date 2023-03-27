package com.example.fyp.account_management.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.ui.view_model.AuthViewModel
import com.example.fyp.account_management.ui.view_model.StaffViewModel
import com.example.fyp.databinding.FragmentRegisterStaffBinding
import com.example.fyp.databinding.FragmentStaffDetailsBinding
import com.example.fyp.menucreator.ui.fragments.AddEditFoodFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class StaffDetailsFragment : Fragment() {

    private var _binding: FragmentStaffDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<StaffViewModel>()

    private val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

    private var staffPosition : StaffPosition? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStaffDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = mutableListOf<StaffPosition>()
        for (i in StaffPosition.values()){
            if (i == StaffPosition.Pending || i == StaffPosition.Disabled)
                continue
            list.add(i)
        }
        val arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item,list)

        binding.apply {
            nameTv.text = viewModel.user.first_name + " " + viewModel.user.last_name
            emailTv.text = viewModel.user.email
            phoneTv.text = viewModel.user.phone
            addressTv.text = viewModel.user.address
            birthdayTv.text = viewModel.user.birthday?.let { sdf.format(it) }
            categoryEt.setAdapter(arrayAdapter)

            if (viewModel.user.profileUri != null){
                Glide.with(requireContext())
                    .load(viewModel.user.profileUri!!.toUri())
                    .into(this.profileIv)
            }

            categoryEt.setOnItemClickListener{ adapter, _, position, _ ->
                staffPosition = adapter.getItemAtPosition(position) as StaffPosition
            }
            acceptChip.setOnClickListener {
                if (staffPosition == null){
                    errorToast("Please select staff position")
                    return@setOnClickListener
                }
                viewModel.acceptPendingStaff(viewModel.user,staffPosition?: StaffPosition.Regular)
                navigateBack()
            }
            rejectChip.setOnClickListener {
                viewModel.rejectPendingStaff(viewModel.user)
                navigateBack()
            }
        }
    }

    private fun errorToast(msg: String){
        Toast.makeText(requireContext(),msg, Toast.LENGTH_LONG).show()
    }

    private fun navigateBack() = findNavController().navigateUp()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}