package com.example.fyp.menucreator.ui.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fyp.R
import com.example.fyp.databinding.*
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.viewmodel.FoodModifierDetailViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "Second Fragment"

@AndroidEntryPoint
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

//    private val viewModel =

//    private lateinit var productId:String
//    private val menu = ProductDatabase

//    private var _productId : String? = null
    private var productId: String? = null
    private var type: ProductType? = null

//    private var _food: Food? = null
//    private val food : Food get() = _food?: viewModel.getFood(productId)!!

    private var _modifier:Modifier? = null
    private val modifier get() = _modifier!!

    private val viewModel : FoodModifierDetailViewModel by viewModels()

    private var _foodBinding: FoodViewComponentBinding? = null
    private val foodBinding get() = _foodBinding!!

    private var _modifierBinding: ModifierViewComponentBinding? = null
    private val modifierBinding get() = _modifierBinding!!

    private var _detailedModifierBinding: ModifierDetailedRowComponentBinding? = null
    private val detailedModifierBinding get() = _detailedModifierBinding!!

    private var action : NavDirections? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            foodObserver()
            _foodBinding = FoodViewComponentBinding.inflate(inflater,container,false)
        } else {
            modifierObserver()
            //inflate detailed view container
            _detailedModifierBinding = ModifierDetailedRowComponentBinding.inflate(inflater,container,false)
        }
        _modifierBinding = ModifierViewComponentBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteFab.setOnClickListener{
            deleteProduct()
        }
        binding.editFab.setOnClickListener{
            action?.let { it1 -> findNavController().navigate(it1) }
        }
    }

    private fun navigateBack() = findNavController().navigate(SecondFragmentDirections.actionSecondFragmentToFirstFragment())

    private fun deleteProduct() {
        observeFoodDeletion()
        viewModel.deleteProduct(productId!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadData(){
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadModifierData() {
        //add modifier_view_component to the base layout
        if (modifierBinding.root.parent == null)
            binding.baseLayout.addView(modifierBinding.root)

        if (detailedModifierBinding.root.parent != null)
            (detailedModifierBinding.root.parent as ViewGroup).removeView(detailedModifierBinding.root)
        modifierBinding.modifierDetailedViewContainer.addView(_detailedModifierBinding?.root)

        detailedModifierBinding.apply {
            println(viewModel.modifier.multipleChoice)
            multipleChoiceValueTv.text = if (viewModel.modifier.multipleChoice) "Yes" else "No"
            requiredValueTv.text = if (viewModel.modifier.required) "Yes" else "No"
        }
        loadModifierItem(viewModel.modifier)

        modifierBinding.lastUpdatedTextView.text = "Last Updated ${viewModel.modifier.date.toString()}"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadFoodData(){

        foodBinding.apply {
            shimmerImage.startShimmer()
        }
        //add food_view_component layout to the base layout
        if (foodBinding.root.parent == null)
            binding.baseLayout.addView(foodBinding.root)
        foodBinding.productNameTextView.text = viewModel.food.name
        foodBinding.descriptionTextview.text = viewModel.food.description
        foodBinding.categoryValueTv.text = viewModel.food.category
        foodBinding.priceTextview.text = viewModel.food.price.toString()
        foodBinding.lastUpdatedTextView.text = "Last Updated ${viewModel.food.lastUpdated.toString()}"
        if (viewModel.food.imageUri != null){
            loadImage()
        } else {
            foodBinding.imageView.setImageResource(R.drawable.ic_image)
        }
        if (viewModel.food.modifiable &&
            viewModel.food.modifierList.isNotEmpty() &&
            foodBinding.modifiersContainerLayout.childCount < viewModel.food.modifierList.size) {

            val list = viewModel.food.modifierList
            val iterator = list.iterator()
            var toUpdate = false
            while (iterator.hasNext()) {
                val id = iterator.next()
                val modifier = viewModel.getModifier(id)
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
                    errorDialog("[$id] is not available. Removing from food modifier list ")
                    iterator.remove()
                    toUpdate = true
                }
            }
            if (toUpdate){
                updateFood(list)
            }

        }


    }

    private fun updateFood(list: ArrayList<String>) {
        viewModel.removeModifierFromFoodAndUpdate(list)
    }

    //Only call this function after initializing modifier binding
    private fun loadModifierItem(modifier: Modifier, containerBinding: ModifierViewComponentBinding = modifierBinding){
        containerBinding.modifierNameTextview.text = modifier.name
        for (itemCode in modifier.modifierItemList) {
            if (containerBinding.modifierItemContainerLayout.childCount < modifier.modifierItemList.size) {
                val itemBinding =
                    RowModifierItemBinding.inflate(layoutInflater, modifierBinding.root, false)
                val item = viewModel.getModifierItem(itemCode)
                itemBinding.modifierItemNameTextview.text = item?.name
                itemBinding.modifierItemPriceTextview.text = item?.price.toString()
                containerBinding.modifierItemContainerLayout.addView(itemBinding.root)
            }
        }
    }

    private fun loadImage() = lifecycleScope.launch(Dispatchers.Main){
        Glide.with(requireContext())
            .load(viewModel.food.imageUri)
            .into(foodBinding.imageView)
        foodBinding.shimmerImage.stopShimmer()
        foodBinding.shimmerImage.visibility = View.GONE
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

    private fun foodObserver() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.foodLoaded.collect() {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
//                    binding.progressBar.hide()
//                    it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        loadData()
//                    it.data.first?.let { it1 -> successToast(it1.name + it.data.second) }
//                    binding.progressBar.hide()
//                    objNote = state.data.first
//                    isMakeEnableUI(false)
//                    binding.done.hide()
//                    binding.delete.show()
//                    binding.edit.show()
                    }
                }
            }
        }
    }

    private fun modifierObserver() = viewLifecycleOwner.lifecycleScope.launchWhenStarted{
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.modifierLoaded.collect() {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
//                    binding.progressBar.hide()
//                    it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        loadData()
//                    it.data.first?.let { it1 -> successToast(it1.name + it.data.second) }
//                    binding.progressBar.hide()
//                    objNote = state.data.first
//                    isMakeEnableUI(false)
//                    binding.done.hide()
//                    binding.delete.show()
//                    binding.edit.show()
                    }
                }
            }
        }
    }

    private fun observeFoodDeletion() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.deleteResponse.collect() {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
//                    binding.progressBar.hide()
//                    it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        if (it.data){
                            binding.progressBar.visibility = View.GONE
                            println("Product deleted successfully")
                            navigateBack()
                        }
//                    it.data.first?.let { it1 -> successToast(it1.name + it.data.second) }
//                    binding.progressBar.hide()
//                    objNote = state.data.first
//                    isMakeEnableUI(false)
//                    binding.done.hide()
//                    binding.delete.show()
//                    binding.edit.show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("OnDestroy Called")
        _binding = null
        _foodBinding = null
        _modifierBinding = null
    }
}