package com.example.fyp

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.fyp.account_management.AccountActivity
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType.Admin
import com.example.fyp.account_management.data.model.AccountType.Customer
import com.example.fyp.account_management.data.model.AccountType.Manager
import com.example.fyp.account_management.data.model.AccountType.Staff
import com.example.fyp.account_management.data.model.StaffPosition.Disabled
import com.example.fyp.account_management.data.model.StaffPosition.Pending
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.FeedbackDialogBinding
import com.example.fyp.databinding.FragmentMainBinding
import com.example.fyp.menucreator.ui.activity.MenuCreatorActivity
import com.example.fyp.ordering_system.ui.OnlineOrderingActivity
import com.example.fyp.pos.PosActivity
import com.example.fyp.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private var _dialogBinding: FeedbackDialogBinding? = null
    private val binding get() = _binding!!
    private val dialogBinding get() = _dialogBinding!!

    private val authViewModel by viewModels<MainAuthViewModel>()

    private val productSettingsViewModel by activityViewModels<ProductSettingsViewModel>()

    private lateinit var account: Account

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        _dialogBinding = FeedbackDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSession()
        initProductSettings()

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

        binding.onlineOrderingBtn.setOnClickListener {
            val i = Intent(requireContext(), OnlineOrderingActivity::class.java)
            i.putExtra("accountType", account.accountType)
            startActivity(i)
        }

        binding.posBtn.setOnClickListener {
            startActivity(Intent(requireContext(),PosActivity::class.java))
        }

        binding.menuCreatorButton.setOnClickListener {
            val i = Intent(requireContext(), MenuCreatorActivity::class.java)
            startActivity(i)
        }

        binding.feedbackFab.setOnClickListener {
            if (dialogBinding.root.parent != null){
                (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)

            }
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setCancelable(true)
                .show()
        }

        dialogBinding.bugReportBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.Url.bugReportUrl)))
        }

        dialogBinding.feedbackBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.Url.feedbackUrl)))
        }
    }

    private fun getSession() = authViewModel.getSession(true) {
        binding.onlineOrderingBtn.isEnabled = false
        if (it != null){
            account = it
            binding.shimmerHello.stopShimmer()
            binding.shimmerHello.visibility = View.GONE
            binding.helloTv.visibility = View.VISIBLE
            binding.helloTv.text = "Hello, ${account.first_name}"

            when(it.accountType){
                Customer -> {
                    binding.posBtn.visibility = View.GONE
                    binding.onlineOrderingBtn.visibility = View.VISIBLE
                    binding.onlineOrderingBtn.isEnabled = true
                }
                Admin,Manager -> {
                    binding.posBtn.visibility = View.VISIBLE
                    binding.onlineOrderingBtn.visibility = View.VISIBLE
                    binding.onlineOrderingBtn.isEnabled = true
                }
                Staff -> {
                    if (it.staffPosition != Disabled && it.staffPosition != Pending && it.staffPosition != null){
                        binding.infoTv.visibility = View.GONE
                        binding.posBtn.visibility = View.VISIBLE
                        binding.onlineOrderingBtn.visibility = View.VISIBLE
                        binding.onlineOrderingBtn.isEnabled = true
                    } else {
                        binding.infoTv.text = "Your staff account is currently ${it.staffPosition}. Please contact admin for more details"
                        binding.infoTv.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun initProductSettings(){
        productSettingsViewModel.insertSettingToDatabase()
    }
}