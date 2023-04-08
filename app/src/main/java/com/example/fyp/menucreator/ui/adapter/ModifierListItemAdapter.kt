package com.example.fyp.menucreator.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.databinding.RowProductItemBinding
import com.example.fyp.menucreator.data.model.Modifier

class ModifierListItemAdapter(
    private val onItemClicked: (Modifier) -> Unit
) :  ListAdapter<Modifier, ModifierListItemAdapter.ItemViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Modifier>() {
            override fun areItemsTheSame(oldItem: Modifier, newItem: Modifier): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: Modifier, newItem: Modifier): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }
    inner class ItemViewHolder(private var binding : RowProductItemBinding,private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(modifier: Modifier){
            binding.productIdTextView.text = modifier.productId
            binding.productNameTextView.text = modifier.name
            binding.chip.visibility = View.GONE
            binding.root.setOnClickListener{ onItemClicked.invoke(modifier)}

            if (modifier.imagePath != null){
                Glide.with(binding.imageView)
                    .load(modifier.imageUri?.toUri())
                    .centerCrop()
                    .into(binding.imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RowProductItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ItemViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}