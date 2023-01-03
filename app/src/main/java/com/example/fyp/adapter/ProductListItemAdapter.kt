package com.example.fyp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.menucreator.fragments.FirstFragmentDirections
import com.example.fyp.R
import com.example.fyp.databinding.RowProductItemBinding
import com.example.fyp.menucreator.Food

class ProductListItemAdapter(private val context: Context,
                             private val dataset: List<Food>) : RecyclerView.Adapter<ProductListItemAdapter.ItemViewHolder>(){

    class ItemViewHolder(private var binding : RowProductItemBinding,private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(food: Food , context: Context){
            binding.productIdTextView.text = food.productId
            binding.productNameTextView.text = food.name
            binding.productDescriptionTextView.text = food.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RowProductItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = dataset[position]
        holder.bind(current,context)
        holder.itemView.setOnClickListener{
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(current.productId,current.productType.type)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}