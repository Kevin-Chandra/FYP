package com.example.fyp.account_management.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.databinding.RowStaffBinding


class StaffListAdapter(
    private val onItemClicked: (Account) -> Unit
) : ListAdapter<Account, StaffListAdapter.StaffViewHolder>(DiffCallback),Filterable{

    private var originalList: List<Account> = currentList.toList()

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

    inner class StaffViewHolder(private var binding : RowStaffBinding,private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(account: Account){
            binding.apply {
                nameTv.text = account.first_name + " " + account.last_name
                emailTv.text = account.email
                when (account.staffPosition){
                    StaffPosition.Disabled -> {
                        statusChip.text = "Disabled"
                        statusChip.setTextColor(Color.RED)
                        positionTv.visibility = View.INVISIBLE
                    }
                    StaffPosition.Pending -> {
                        statusChip.text = "Pending"
                        statusChip.setTextColor(Color.YELLOW)
                        positionTv.visibility = View.INVISIBLE
                    }
                    else -> {
                        statusChip.text = "Active"
                        statusChip.setTextColor(Color.GREEN)
                        positionTv.visibility = View.VISIBLE
                        positionTv.text = "Position: ${account.staffPosition}"
                    }
                }
                root.setOnClickListener{ onItemClicked.invoke(account)}
            }
            val myOptions = RequestOptions()
                .override(binding.imageView.width, binding.imageView.height)
                .centerCrop()
            if (account.profileUri != null){
                Glide.with(binding.imageView)
                    .load(account.profileUri.toUri())
                    .apply(myOptions)
                    .skipMemoryCache(true)
                    .into(binding.imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = RowStaffBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return StaffViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<Account>?) {
        submitList(list, false)
    }

    private fun submitList(list: List<Account>?, filtered: Boolean) {
        if (!filtered)
            originalList = list ?: listOf()

        super.submitList(list)
    }

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = if (constraint.isNullOrEmpty())
                        originalList
                    else
                        originalList.filter {
                            it.staffPosition?.name == constraint
                        }
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                submitList(results?.values as? List<Account>,true)
            }

        }
    }

    fun onFilter(list: List<Account>, constraint: String): List<Account>{
        val filteredList = mutableListOf<Account>()
        list.filter {
            it.staffPosition?.name == constraint
        }.onEach {
            filteredList.add(it)
        }
        return filteredList
    }

}