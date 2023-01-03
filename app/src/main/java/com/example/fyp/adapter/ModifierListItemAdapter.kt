package com.example.fyp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.menucreator.fragments.FirstFragmentDirections
import com.example.fyp.databinding.RowProductItemBinding
import com.example.fyp.menucreator.Modifier

class ModifierListItemAdapter(private val context: Context,
                             private val dataset: List<Modifier>) : RecyclerView.Adapter<ModifierListItemAdapter.ItemViewHolder>(){

    class ItemViewHolder(private var binding : RowProductItemBinding,private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(modifier: Modifier, context: Context){
            binding.productIdTextView.text = modifier.productId
            binding.productNameTextView.text = modifier.name
//            binding.productDescriptionTextView.text = food.description
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