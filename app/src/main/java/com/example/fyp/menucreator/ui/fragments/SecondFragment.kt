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
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.ui.view_model.AccountViewModel
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.databinding.*
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.ui.viewmodel.FoodListingViewModel
import com.example.fyp.menucreator.ui.viewmodel.FoodModifierDetailViewModel
import com.example.fyp.menucreator.ui.viewmodel.ModifierListingViewModel
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.SetAvailabilityEvent
import com.example.fyp.menucreator.util.UiState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
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
    private val authViewModel : MainAuthViewModel by activityViewModels()
    private val accountViewModel : AccountViewModel by viewModels()

    private var _foodBinding: FoodViewComponentBinding? = null
    private val foodBinding get() = _foodBinding!!

    private lateinit var bottomSheetBinding: SetAvailabilityLayoutBinding

    private var _modifierBinding: ModifierViewComponentBinding? = null
    private val modifierBinding get() = _modifierBinding!!

    private var _detailedModifierBinding: ModifierDetailedRowComponentBinding? = null
    private val detailedModifierBinding get() = _detailedModifierBinding!!

    private lateinit var bottomSheet : BottomSheetDialog

    private var productId: String? = null
    private var type: ProductType? = null

    private var action : NavDirections? = null

    private var account : Account? = null

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

        bottomSheetBinding = SetAvailabilityLayoutBinding.inflate(inflater,container,false)

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

        authViewModel.getSession {
            account = it
            if (it != null){
                when (it.accountType){
                    AccountType.Customer -> {}
                    AccountType.Admin -> {
                        setAdminAccess()
                        setStaffAccess()
                    }
                    AccountType.Manager -> {
                        setAdminAccess()
                        setStaffAccess()
                    }
                    AccountType.Staff -> {
                        if (it.staffPosition != StaffPosition.Disabled && it.staffPosition != StaffPosition.Pending){
                            setStaffAccess()
                        }
                    }
                }
            }
        }

        observeDeletion()
        observeUpdateAvailability()
        productObserver()

        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(bottomSheetBinding.root)
        bottomSheet.behavior.apply {
            isDraggable = true
        }

        bottomSheetBinding.dismissButton.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheetBinding.saveButton.setOnClickListener {
            if (account == null){
                errorDialog("Account not yet loaded!")
                return@setOnClickListener
            }
            viewModel.onEvent(SetAvailabilityEvent.Save(account!!))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (allowRefresh)
                loadData()
            else
                binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.setAvailabilityFab.setOnClickListener {
            bottomSheet.show()
        }

        binding.deleteFab.setOnClickListener{
            deleteDialog()
        }
        binding.editFab.setOnClickListener{
            action?.let { it1 -> findNavController().navigate(it1) }
        }
    }

    private fun setShimmerFood(b: Boolean) {
        if (b) {
            foodBinding.shimmerLayout.showShimmer(true)
            foodBinding.createdByTv .visibility = View.INVISIBLE
            foodBinding.createdByCv .visibility = View.INVISIBLE
            foodBinding.lastUpdatedByTv .visibility = View.INVISIBLE
            foodBinding.lastUpdatedCv .visibility = View.INVISIBLE
        } else {
            foodBinding.shimmerLayout.hideShimmer()
            foodBinding.cvShimmer1.visibility = View.GONE
            foodBinding.cvShimmer2.visibility = View.GONE
            foodBinding.shimmerTv1.visibility = View.GONE
            foodBinding.shimmerTv2.visibility = View.GONE
            foodBinding.createdByTv .visibility = View.VISIBLE
            foodBinding.createdByCv .visibility = View.VISIBLE
            foodBinding.lastUpdatedByTv .visibility = View.VISIBLE
            foodBinding.lastUpdatedCv .visibility = View.VISIBLE
        }
    }

    private fun setShimmerModifier(b: Boolean) {
        if (b) {
            modifierBinding.shimmerLayout.showShimmer(true)
            modifierBinding.createdByTv2.visibility = View.INVISIBLE
            modifierBinding.createdByCv.visibility = View.INVISIBLE
            modifierBinding.lastUpdatedByTv2.visibility = View.INVISIBLE
            modifierBinding.lastUpdatedCv .visibility = View.INVISIBLE
        } else {
            modifierBinding.shimmerLayout.hideShimmer()
            modifierBinding.cvShimmer1.visibility = View.GONE
            modifierBinding.cvShimmer2.visibility = View.GONE
            modifierBinding.shimmerTv1.visibility = View.GONE
            modifierBinding.shimmerTv2.visibility = View.GONE
            modifierBinding.createdByTv2.visibility = View.VISIBLE
            modifierBinding.createdByCv .visibility = View.VISIBLE
            modifierBinding.lastUpdatedByTv2.visibility = View.VISIBLE
            modifierBinding.lastUpdatedCv.visibility = View.VISIBLE
        }
    }

    private fun setAdminAccess() {
        binding.deleteFab.visibility = View.VISIBLE
        binding.editFab.visibility = View.VISIBLE
    }

    private fun setStaffAccess(){
        binding.setAvailabilityFab.visibility = View.VISIBLE
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
        account?:return
        isObservingLoadData = false
        viewModel.deleteProduct(account!!, productId!!)
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

        accountViewModel.getAccount(modifier.lastUpdatedBy){
            when (it){
                is Response.Error -> {
                    modifierBinding.lastUpdatedByTv2.text = "Last Updated by [DELETED ACCOUNT]"
                    setShimmerModifier(false)
                }
                Response.Loading -> {
                    setShimmerModifier(true)
                }
                is Response.Success ->{
                    val acc = it.data
                    if (acc == null){
                        modifierBinding.lastUpdatedByTv2.text = "Last Updated by [DELETED ACCOUNT]"
                    } else {
                        modifierBinding.lastUpdatedByTv2.text = "Last Updated by ${acc.first_name} ${acc.last_name}"
                        acc.profileUri?.let { uri ->
                            Glide.with(requireContext())
                                .load(uri)
                                .centerCrop()
                                .into(modifierBinding.lastUpdatedIv)
                        }
                    }
                    setShimmerModifier(false)
                }
            }
        }

        accountViewModel.getAccount(modifier.createdBy){
            when (it){
                is Response.Error -> {
                    modifierBinding.createdByTv2.text = "Created by [DELETED ACCOUNT]"
                    setShimmerModifier(false)
                }
                Response.Loading -> {
                    setShimmerModifier(true)
                }
                is Response.Success -> {
                    val acc = it.data
                    if (acc == null){
                        modifierBinding.createdByTv2.text = "Created by [DELETED ACCOUNT]"
                    } else {
                        modifierBinding.createdByTv2.text = "Created by ${acc.first_name} ${acc.last_name}"
                        acc.profileUri?.let { uri ->
                            Glide.with(requireContext())
                                .load(uri)
                                .centerCrop()
                                .into(modifierBinding.createdByIv)
                        }
                    }
                    setShimmerModifier(false)
                }
            }
        }

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
        loadBottomSheet()

        modifierBinding.lastUpdatedTextView.text = "Last Updated ${modifier.lastUpdated.toString()}"
    }

    private fun loadBottomSheet(){
        println("load bs")
        if (type == ProductType.Modifier){
            bottomSheetBinding.availableItemLayout.removeAllViews()
            viewModel.availabilityState.value.modifierItemAvailabilityMap?.forEach {
                val item = viewModel.getModifierItem(it.key) ?: return@forEach
                val switch = SwitchMaterial(requireContext())
                switch.text = "[${item.name}] ${item.name}"
                switch.isChecked = it.value
                switch.setOnCheckedChangeListener { _, bool ->
                    viewModel.onEvent(
                        SetAvailabilityEvent.ModifierItemAvailabilityChanged(
                            Pair(
                                it.key,
                                bool
                            )
                        )
                    )
                }
                bottomSheetBinding.availableItemLayout.addView(switch)
            }
        } else {
            val switch = SwitchMaterial(requireContext())
            val food = viewModel.food
            switch.text = "[${food?.productId}] ${food?.name}"
            switch.isChecked = viewModel.availabilityState.value.foodAvailability
            switch.setOnCheckedChangeListener{ _, bool ->
                viewModel.onEvent(SetAvailabilityEvent.FoodAvailabilityChanged(bool))
            }
            bottomSheetBinding.availableItemLayout.removeAllViews()
            bottomSheetBinding.availableItemLayout.addView(switch)
        }
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
        foodBinding.availabilityChip.text = if (food.availability) "Available" else "Unavailable"
        foodBinding.lastUpdatedTextView.text = "Last Updated ${food.lastUpdated.toString()}"

        accountViewModel.getAccount(food.lastUpdatedBy){
            when (it){
                is Response.Error -> {
                    foodBinding.lastUpdatedByTv.text = "Last Updated by [DELETED ACCOUNT]"
                    setShimmerFood(false)
                }
                Response.Loading -> {
                    setShimmerFood(true)
                }
                is Response.Success ->{
                    val acc = it.data
                    if (acc == null){
                        foodBinding.lastUpdatedByTv.text = "Last Updated by [DELETED ACCOUNT]"
                    } else {
                        foodBinding.lastUpdatedByTv.text = "Last Updated by ${acc.first_name} ${acc.last_name}"
                        acc.profileUri?.let { uri ->
                            Glide.with(requireContext())
                                .load(uri)
                                .centerCrop()
                                .into(foodBinding.lastUpdatedIv)
                        }
                    }
                    setShimmerFood(false)
                }
            }
        }

        accountViewModel.getAccount(food.createdBy){
            when (it){
                is Response.Error -> {
                    foodBinding.createdByTv.text = "Created by [DELETED ACCOUNT]"
                    setShimmerFood(false)
                }
                Response.Loading -> {
                    setShimmerFood(true)
                }
                is Response.Success -> {
                    val acc = it.data
                    if (acc == null){
                        foodBinding.createdByTv.text = "Created by [DELETED ACCOUNT]"
                    } else {
                        foodBinding.createdByTv.text = "Created by ${acc.first_name} ${acc.last_name}"
                        acc.profileUri?.let { uri ->
                            Glide.with(requireContext())
                                .load(uri)
                                .centerCrop()
                                .into(foodBinding.createdByIv)
                        }
                    }
                    setShimmerFood(false)
                }
            }
        }

        if (food.imageUri != null){
            loadImage(food.imageUri.toUri())
        } else {
            foodBinding.imageView.setImageResource(R.mipmap.ic_launcher)
        }

        if (!food.modifiable){
            foodBinding.isModifiableValueTv.text = "No"
            foodBinding.modifiersTitleTextview.visibility = View.GONE
            foodBinding.modifiersContainerLayout.visibility = View.GONE
        } else {
            foodBinding.isModifiableValueTv.text = "Yes"
            foodBinding.modifiersTitleTextview.visibility = View.VISIBLE
            foodBinding.modifiersContainerLayout.visibility = View.VISIBLE
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

        loadBottomSheet()

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
        viewModel.removeModifierFromFoodAndUpdate(account?:return, food,list)
    }

    //Only call this function after initializing modifier binding
    private fun loadModifierItem(modifier: Modifier, containerBinding: ModifierViewComponentBinding = modifierBinding){
        containerBinding.modifierNameTextview.text = modifier.name
        containerBinding.modifierItemContainerLayout.removeAllViews()
        for (itemCode in modifier.modifierItemList) {
            val itemBinding = RowModifierItemBinding.inflate(layoutInflater, modifierBinding.root, false)
            val item = modifierListViewModel.getModifierItem(itemCode)
            itemBinding.modifierItemNameTextview.text = item?.name
            itemBinding.modifierItemPriceTextview.text = item?.price.toString()
            itemBinding.modifierItemAvailabilityTextview.text = if (item?.availability == true) "Available" else "Not Available"
            containerBinding.modifierItemContainerLayout.addView(itemBinding.root)
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
            .setMessage("Reason: $msg")
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

    private fun observeUpdateAvailability() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.updateAvailabilityResponse.collect() {
                when (it) {
                    is UiState.Loading -> {
                        bottomSheetBinding.saveButton.isEnabled = false
                        bottomSheetBinding.progressBarBottomSheet.visibility = View.VISIBLE
                    }
                    is UiState.Failure -> {
                        bottomSheetBinding.saveButton.isEnabled = true
                        bottomSheetBinding.progressBarBottomSheet.visibility = View.GONE
                        it.e?.message?.let { it1 -> errorDialog(it1) }
                    }
                    is UiState.Success -> {
                        bottomSheetBinding.saveButton.isEnabled = true
                        bottomSheetBinding.progressBarBottomSheet.visibility = View.GONE
                        if (it.data == "Loaded"){
                            println(it.data)
                            loadBottomSheet()
                        }
                        if (it.data == "Updated Availability!"){
                            successToast(it.data)
//                            binding.deleteFab.isEnabled = true
//                            binding.editFab.isEnabled = true
//                            allowRefresh = true
//                            successToast("Product deleted successfully")
//                            navigateBack()
                            bottomSheet.dismiss()
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