package com.example.fyp.account_management.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.databinding.RowPendingStaffBinding


class PendingStaffAdapter(
    private val onAcceptClicked: (Account) -> Unit,
    private val onRejectClicked: (Account) -> Unit,
    private val onItemClicked: (Account) -> Unit
) : ListAdapter<Account, PendingStaffAdapter.StaffViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Account>() {
            override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    inner class StaffViewHolder(private var binding : RowPendingStaffBinding, view: View) : RecyclerView.ViewHolder(view) {
        fun bind(account: Account){
            binding.apply {
                nameTv.text = account.first_name + " " + account.last_name
                emailTv.text = account.email
                acceptChip.setOnClickListener { onAcceptClicked.invoke(account) }
                rejectChip.setOnClickListener { onRejectClicked.invoke(account) }
                root.setOnClickListener{ onItemClicked.invoke(account)}
            }
            val myOptions = RequestOptions()
                .override(binding.imageView.width, binding.imageView.height)
                .centerCrop()
            if (account.profileUri != null){
                binding.imageView.setPadding(0)
                Glide.with(binding.imageView)
                    .load(account.profileUri.toUri())
                    .apply(myOptions)
                    .into(binding.imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = RowPendingStaffBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return StaffViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}