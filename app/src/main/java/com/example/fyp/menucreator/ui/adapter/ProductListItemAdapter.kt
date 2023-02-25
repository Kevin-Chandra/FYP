package com.example.fyp.menucreator.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.databinding.RowProductItemBinding
import com.example.fyp.menucreator.data.model.Food

class ProductListItemAdapter(
    private val onItemClicked: (Food) -> Unit
) : ListAdapter<Food, ProductListItemAdapter.FoodViewHolder>(DiffCallback){

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    inner class FoodViewHolder(private var binding : RowProductItemBinding,private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(food: Food){
            binding.productIdTextView.text = food.productId
            binding.productNameTextView.text = food.name
            binding.productDescriptionTextView.text = food.description

            binding.root.setOnClickListener{ onItemClicked.invoke(food)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = RowProductItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return FoodViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}