package com.example.fyp.menucreator.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.databinding.RowAddEditModifierItemBinding


class ModifierItemAddEditAdapter(
    private val onItemRemoveClicked: (Triple<Pair<String,Boolean>,String,String>, position:Int) -> Unit,
    private val onNameChanged: (name: String, position:Int) -> Unit,
    private val onIdChanged: (id: String, position:Int) -> Unit,
    private val onPriceChanged: (price: String, position:Int) -> Unit,
) : ListAdapter<Triple<Pair<String,Boolean>,String,String>, ModifierItemAddEditAdapter.ItemViewHolder>(DiffCallback){


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Triple<Pair<String,Boolean>,String,String>>() {
            override fun areItemsTheSame(oldItem: Triple<Pair<String,Boolean>,String,String>, newItem: Triple<Pair<String,Boolean>,String,String>): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(oldItem: Triple<Pair<String,Boolean>,String,String>, newItem: Triple<Pair<String,Boolean>,String,String>): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    inner class ItemViewHolder(private var binding : RowAddEditModifierItemBinding, view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: Triple<Pair<String,Boolean>, String, String>){

            binding.apply {
                rowModifierItemId.isEnabled = !item.first.second
                rowModifierItemId.setText(item.first.first)
                modifierItemName.setText(item.second)
                modifierItemPrice.setText(item.third)
                rowModifierItemId.doAfterTextChanged {
                    onIdChanged.invoke(binding.rowModifierItemId.text.toString(),adapterPosition)
                }
                modifierItemName.doAfterTextChanged {
                    onNameChanged.invoke(binding.modifierItemName.text.toString(),adapterPosition)
                }
                modifierItemPrice.doAfterTextChanged {
                    onPriceChanged.invoke(binding.modifierItemPrice.text.toString(),adapterPosition)
                }

                removeButton.setOnClickListener{
                    removeButton.isEnabled = false
                    onItemRemoveClicked.invoke(item,adapterPosition)
                }
            }
        }

    }

    override fun submitList(list: MutableList<Triple<Pair<String, Boolean>, String, String>>?) {
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RowAddEditModifierItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(binding,binding.root)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}