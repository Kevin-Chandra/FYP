package com.example.fyp.menucreator.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.fyp.databinding.FoodViewComponentBinding
import com.example.fyp.databinding.FragmentSecondBinding
import com.example.fyp.databinding.ModifierDetailedRowComponentBinding
import com.example.fyp.databinding.ModifierViewComponentBinding
import com.example.fyp.databinding.RowModifierItemBinding
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.viewmodel.FoodModifierDetailViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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
        println("Oncreate called")
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
            println("argument block passed")
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

    private fun deleteProduct() {
        observeFoodDeletion()
        viewModel.deleteProduct(productId!!)
        findNavController().navigate(SecondFragmentDirections.actionSecondFragmentToFirstFragment())
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
        binding.baseLayout.addView(modifierBinding.root)

        modifierBinding.modifierDetailedViewContainer.addView(_detailedModifierBinding?.root)

        detailedModifierBinding.apply {
            println(viewModel.modifier.multipleChoice)
            multipleChoiceValueTv.text = if (viewModel.modifier.multipleChoice) "Yes" else "No"
            requiredValueTv.text = if (viewModel.modifier.required) "Yes" else "No"
        }
        loadModifierItem(viewModel.modifier)

        modifierBinding.lastUpdatedTextView.text = "Last Updated ${viewModel.modifier.date.toString()}"
    }

    private fun loadFoodData(){
        //add food_view_component layout to the base layout
        binding.baseLayout.addView(foodBinding.root)
        foodBinding.productNameTextView.text = viewModel.food.name
        foodBinding.descriptionTextview.text = viewModel.food.description
        foodBinding.priceTextview.text = viewModel.food.price.toString()

        if (viewModel.food.modifiable && !viewModel.food.modifierList.isNullOrEmpty()) {
            for( modifierCode in viewModel.food.modifierList!!) {
                // inflate modifier view
                val eachModifierBinding = ModifierViewComponentBinding.inflate(layoutInflater,foodBinding.root,false)

                //add each modifier view to modifier container layout
                foodBinding.modifiersContainerLayout.addView(eachModifierBinding.root)

                viewModel.getModifier(modifierCode)?.let { loadModifierItem(it,eachModifierBinding) }
            }
        }

        foodBinding.lastUpdatedTextView.text = "Last Updated ${viewModel.food.date.toString()}"
//        Log.d(TAG,_food?.modifierList.toString())
    }

    //Only call this function after initializing modifier binding
    private fun loadModifierItem(modifier: Modifier, containerBinding: ModifierViewComponentBinding = modifierBinding){
        containerBinding.modifierNameTextview.text = modifier.name
        for (itemCode in modifier.modifierItemList) {
            val itemBinding = RowModifierItemBinding.inflate(layoutInflater,modifierBinding.root,false)
            val item = viewModel.getModifierItem(itemCode)
            itemBinding.modifierItemNameTextview.text = item?.name
            itemBinding.modifierItemPriceTextview.text = item?.price.toString()
            containerBinding.modifierItemContainerLayout.addView(itemBinding.root)
        }
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
                        binding.progressBar.visibility = View.GONE
                        println("Food deleted successfully")
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