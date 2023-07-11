package com.example.fyp.menucreator.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.databinding.RowFoodCategoryBinding
import com.example.fyp.menucreator.data.model.FoodCategory

class FoodCategoryAdapter (private val onItemClicked: (FoodCategory) -> Unit) :
    ListAdapter<FoodCategory, FoodCategoryAdapter.ItemViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FoodCategory>() {
            override fun areItemsTheSame(oldItem: FoodCategory, newItem: FoodCategory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FoodCategory, newItem: FoodCategory): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    inner class ItemViewHolder(private var binding : RowFoodCategoryBinding, view: View) : RecyclerView.ViewHolder(view) {
        fun bind(category: FoodCategory){
            binding.categoryNameTv.text = category.name
            binding.deleteBtn.setOnClickListener{ onItemClicked.invoke(category)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RowFoodCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ItemViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
