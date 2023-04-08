package com.example.fyp.menucreator.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.databinding.*
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.viewmodel.FoodListingViewModel
import com.example.fyp.menucreator.ui.viewmodel.FoodModifierDetailViewModel
import com.example.fyp.menucreator.ui.viewmodel.ModifierListingViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel : FoodModifierDetailViewModel by viewModels()
    private val foodListViewModel : FoodListingViewModel by activityViewModels()
    private val modifierListViewModel : ModifierListingViewModel by activityViewModels()

    private var _foodBinding: FoodViewComponentBinding? = null
    private val foodBinding get() = _foodBinding!!

    private var _modifierBinding: ModifierViewComponentBinding? = null
    private val modifierBinding get() = _modifierBinding!!

    private var _detailedModifierBinding: ModifierDetailedRowComponentBinding? = null
    private val detailedModifierBinding get() = _detailedModifierBinding!!

    private var productId: String? = null
    private var type: ProductType? = null

    private var action : NavDirections? = null

    private var isObservingLoadData = true

    private var allowRefresh = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        arguments?.let {
            productId = SecondFragmentArgs.fromBundle(it).productId
            type = SecondFragmentArgs.fromBundle(it).type
            viewModel.initialize(SecondFragmentArgs.fromBundle(it).productId,SecondFragmentArgs.fromBundle(it).type)
        }

        if (viewModel.type == ProductType.FoodAndBeverage) {
            _foodBinding = FoodViewComponentBinding.inflate(inflater,container,false)
        } else {
            //inflate detailed view container
            _detailedModifierBinding = ModifierDetailedRowComponentBinding.inflate(inflater,container,false)
        }
        _modifierBinding = ModifierViewComponentBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDeletion()
        productObserver()

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (allowRefresh)
                loadData()
            else
                binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.deleteFab.setOnClickListener{
            deleteDialog()
        }
        binding.editFab.setOnClickListener{
            action?.let { it1 -> findNavController().navigate(it1) }
        }
    }

    private fun deleteDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure deleting this product?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun navigateBack() = findNavController().navigateUp()
    private fun deleteProduct() {
        isObservingLoadData = false
        viewModel.deleteProduct(productId!!)
    }

    private fun loadData(){
        binding.swipeRefreshLayout.isRefreshing = true
        //Base layout is fragment_second constraint layout
        if (viewModel.type == ProductType.FoodAndBeverage) {
            action = SecondFragmentDirections.actionSecondFragmentToAddEditFoodFragment(
                NavigationCommand.EDIT,
                viewModel.productId
            )
            loadFoodData()
        } else {
            action = SecondFragmentDirections.actionSecondFragmentToAddEditModifierFragment(
                NavigationCommand.EDIT,
                viewModel.productId)
            loadModifierData()
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadModifierData() {
        productId?:return
        val modifier = modifierListViewModel.getModifier(productId!!) ?: return

        //add modifier_view_component to the base layout
        if (modifierBinding.root.parent == null)
            binding.baseLayout.addView(modifierBinding.root)

        if (detailedModifierBinding.root.parent != null)
            (detailedModifierBinding.root.parent as ViewGroup).removeView(detailedModifierBinding.root)
        modifierBinding.modifierDetailedViewContainer.addView(_detailedModifierBinding?.root)

        detailedModifierBinding.apply {
            modifier.imageUri?.let{
                Glide.with(requireContext())
                    .load(it.toUri())
                    .centerCrop()
                    .into(detailedModifierBinding.modifierIv)
            }
            multipleChoiceValueTv.text = if (modifier.multipleChoice) "Yes" else "No"
            requiredValueTv.text = if (modifier.required) "Yes" else "No"
        }
        loadModifierItem(modifier)

        modifierBinding.lastUpdatedTextView.text = "Last Updated ${modifier.lastUpdated.toString()}"
    }

    private fun loadFoodData(){
        productId?: return
        val food = foodListViewModel.getFood(productId!!) ?: return

        //add food_view_component layout to the base layout
        if (foodBinding.root.parent == null)
            binding.baseLayout.addView(foodBinding.root)
        foodBinding.productNameTextView.text = food.name
        foodBinding.descriptionTextview.text = food.description
        foodBinding.categoryValueTv.text = food.category
        foodBinding.priceTextview.text = food.price.toString()
        foodBinding.lastUpdatedTextView.text = "Last Updated ${food.lastUpdated.toString()}"
        if (food.imageUri != null){
            loadImage(food.imageUri.toUri())
        } else {
            foodBinding.imageView.setImageResource(R.mipmap.ic_launcher)
        }
        foodBinding.modifiersContainerLayout.removeAllViews()
        if (food.modifiable &&
            food.modifierList.isNotEmpty() &&
            foodBinding.modifiersContainerLayout.childCount < food.modifierList.size) {

            val list = food.modifierList as MutableList
            val iterator = list.iterator()
//            var toUpdate = false
            while (iterator.hasNext()) {
                val id = iterator.next()
                val modifier = modifierListViewModel.getModifier(id)
                if (modifier != null) {
                    val eachModifierBinding = ModifierViewComponentBinding.inflate(
                        layoutInflater,
                        foodBinding.root,
                        false
                    )
                    //add each modifier view to modifier container layout
                    foodBinding.modifiersContainerLayout.addView(eachModifierBinding.root)
                    loadModifierItem(modifier, eachModifierBinding)
                } else {
                    errorModifierDialog("[$id] is not available. Do you want to remove from food modifier list?",id,food)
                }
            }

        }

    }

    //true if the id is to be removed
    private fun errorModifierDialog(msg: String,id:String,food:Food){
        val list = food.modifierList as MutableList
        AlertDialog.Builder(context)
            .setTitle("Exception occured")
            .setMessage("Modifier Unavailable\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Remove") {dialog, _ ->
                list.remove(id)
                updateFoodModifierList(food, list)
                dialog.dismiss()
            }
            .setNegativeButton("Don't remove"){dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun updateFoodModifierList(food: Food, list: List<String>) {
        viewModel.removeModifierFromFoodAndUpdate(food,list)
    }

    //Only call this function after initializing modifier binding
    private fun loadModifierItem(modifier: Modifier, containerBinding: ModifierViewComponentBinding = modifierBinding){
        containerBinding.modifierNameTextview.text = modifier.name
        for (itemCode in modifier.modifierItemList) {
            if (containerBinding.modifierItemContainerLayout.childCount < modifier.modifierItemList.size) {
                val itemBinding =
                    RowModifierItemBinding.inflate(layoutInflater, modifierBinding.root, false)
                val item = modifierListViewModel.getModifierItem(itemCode)
                itemBinding.modifierItemNameTextview.text = item?.name
                itemBinding.modifierItemPriceTextview.text = item?.price.toString()
                containerBinding.modifierItemContainerLayout.addView(itemBinding.root)
            }
        }
    }

    private fun loadImage(uri: Uri) = lifecycleScope.launch(Dispatchers.Main){
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(foodBinding.imageView)
    }

    private fun errorDialog(msg: String){
        AlertDialog.Builder(context)
            .setTitle("Exception occured")
            .setMessage("Modifier Unavailable\nReason: $msg")
            .setCancelable(false)
            .setPositiveButton("Ok") {
                    dialog, _ -> dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun productObserver() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            combine(
                foodListViewModel.foods,
                modifierListViewModel.modifiers,
                modifierListViewModel.modifierItems
            ) { foods, modifiers, items ->
                if (isObservingLoadData && foods is UiState.Success && modifiers is UiState.Success && items is UiState.Success)
                    loadData()
            }.stateIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun observeDeletion() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.deleteResponse.collect() {
                when (it) {
                    is UiState.Loading -> {
                        binding.deleteFab.isEnabled = false
                        binding.editFab.isEnabled = false
                        allowRefresh = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        binding.deleteFab.isEnabled = true
                        binding.editFab.isEnabled = true
                        allowRefresh = true
                        binding.progressBar.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data){
                            binding.deleteFab.isEnabled = true
                            binding.editFab.isEnabled = true
                            allowRefresh = true
                            binding.progressBar.visibility = View.GONE
                            successToast("Product deleted successfully")
                            navigateBack()
                        }
                    }
                }
            }
        }
    }

    private fun successToast(msg: String){
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("OnDestroy Called")
        _binding = null
        _foodBinding = null
        _modifierBinding = null
    }
}