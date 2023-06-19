package com.example.fyp.menucreator.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.example.fyp.account_management.data.model.AccountType.*
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

    private val viewModel: FoodModifierDetailViewModel by viewModels()
    private val foodListViewModel: FoodListingViewModel by activityViewModels()
    private val modifierListViewModel: ModifierListingViewModel by activityViewModels()
    private val authViewModel: MainAuthViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    private var _foodBinding: FoodViewComponentBinding? = null
    private val foodBinding get() = _foodBinding!!

    private lateinit var bottomSheetBinding: SetAvailabilityLayoutBinding

    private var _modifierBinding: ModifierViewComponentBinding? = null
    private val modifierBinding get() = _modifierBinding!!

    private var _detailedModifierBinding: ModifierDetailedRowComponentBinding? = null
    private val detailedModifierBinding get() = _detailedModifierBinding!!

    private var _creatorInfoBinding: CreatorInfoLayoutBinding? = null
    private val creatorInfoBinding get() = _creatorInfoBinding!!

    private lateinit var bottomSheet: BottomSheetDialog

    private var productId: String? = null
    private var type: ProductType? = null

    private var action: NavDirections? = null

    private var account: Account? = null

    private var isObservingLoadData = true

    private var allowRefresh = true

    private var fabClicked = false

    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_open_anim) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_close_anim) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_bottom_anim) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.to_bottom_anim) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        arguments?.let {
            productId = SecondFragmentArgs.fromBundle(it).productId
            type = SecondFragmentArgs.fromBundle(it).type
            viewModel.initialize(
                SecondFragmentArgs.fromBundle(it).productId,
                SecondFragmentArgs.fromBundle(it).type
            )
        }

        bottomSheetBinding = SetAvailabilityLayoutBinding.inflate(inflater, container, false)

        if (viewModel.type == ProductType.FoodAndBeverage) {
            _foodBinding = FoodViewComponentBinding.inflate(inflater, container, false)
        } else {
            //inflate detailed view container
            _detailedModifierBinding = ModifierDetailedRowComponentBinding.inflate(inflater, container, false)
        }
        _creatorInfoBinding = CreatorInfoLayoutBinding.inflate(inflater,container,false)
        _modifierBinding = ModifierViewComponentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.getSession {
            account = it ?: return@getSession

            if (this.lifecycle.currentState >= Lifecycle.State.STARTED) {
                when (it.accountType) {
                    Customer -> { binding.fab.visibility = View.GONE }
                    Admin,Manager -> {
                        binding.fab.visibility = View.VISIBLE
                    }
                    Staff -> {
                        if (it.staffPosition != StaffPosition.Disabled && it.staffPosition != StaffPosition.Pending) {
                            binding.fab.visibility = View.VISIBLE
                        } else {
                            binding.fab.visibility = View.GONE
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
            authViewModel.getSession {
                if (it == null) {
                    errorDialog("Account not yet loaded!")
                    return@getSession
                }
                viewModel.onEvent(SetAvailabilityEvent.Save(it))
            }
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

        binding.deleteFab.setOnClickListener {
            deleteDialog()
        }
        binding.editFab.setOnClickListener {
            action?.let { it1 -> findNavController().navigate(it1) }
        }
        binding.fab.setOnClickListener {
            onAddButtonClicked()
        }
    }

    private fun onAddButtonClicked(){
        setVisibility(fabClicked)
        setAnimation(fabClicked)
        fabClicked = !fabClicked
    }

    private fun setVisibility(clicked: Boolean){
        if (!clicked){
            when (account?.accountType) {
                Customer, null -> {}
                Admin, Manager -> {
                    binding.deleteFab.visibility = View.VISIBLE
                    binding.editFab.visibility = View.VISIBLE
                    binding.setAvailabilityFab.visibility = View.VISIBLE
                }
                Staff -> {
                    if (account?.staffPosition != StaffPosition.Pending && account?.staffPosition != StaffPosition.Disabled)
                        binding.setAvailabilityFab.visibility = View.VISIBLE
                }
            }
        } else {
            binding.deleteFab.visibility = View.GONE
            binding.editFab.visibility = View.GONE
            binding.setAvailabilityFab.visibility = View.GONE
        }
    }

    private fun setAnimation(clicked: Boolean){
        if (!clicked){
            binding.deleteFab.animation = fromBottom
            binding.editFab.animation = fromBottom
            binding.setAvailabilityFab.animation = fromBottom
            binding.fab.animation = rotateOpen
        } else {
            binding.deleteFab.animation = toBottom
            binding.editFab.animation = toBottom
            binding.setAvailabilityFab.animation = toBottom
            binding.fab.animation = rotateClose
        }

    }

    private fun setShimmerCreatorLayout(b: Boolean) {
        if (b) {
            creatorInfoBinding.apply {
                shimmerLayout.showShimmer(true)
                createdByTv4.visibility = View.INVISIBLE
                createdByCv.visibility = View.INVISIBLE
                lastUpdatedByTv4.visibility = View.INVISIBLE
                lastUpdatedCv.visibility = View.INVISIBLE
            }
        } else {
            creatorInfoBinding.shimmerLayout.hideShimmer()
            creatorInfoBinding.cvShimmer1.visibility = View.GONE
            creatorInfoBinding.cvShimmer2.visibility = View.GONE
            creatorInfoBinding.shimmerTv1.visibility = View.GONE
            creatorInfoBinding.shimmerTv2.visibility = View.GONE
            creatorInfoBinding.createdByTv4.visibility = View.VISIBLE
            creatorInfoBinding.createdByCv.visibility = View.VISIBLE
            creatorInfoBinding.lastUpdatedByTv4.visibility = View.VISIBLE
            creatorInfoBinding.lastUpdatedCv.visibility = View.VISIBLE
        }
    }

    private fun deleteDialog() {
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
        authViewModel.getSession {
            it ?: return@getSession
            isObservingLoadData = false
            viewModel.deleteProduct(it, productId!!)
        }
    }

    private fun loadData() {
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
                viewModel.productId
            )
            loadModifierData()
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadModifierData() {
        productId ?: return
        val modifier = modifierListViewModel.getModifier(productId!!) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    accountViewModel.getAccount(modifier.lastUpdatedBy) {
                        when (it) {
                            is Response.Error -> {
                                creatorInfoBinding.lastUpdatedByTv4.text =
                                    "Last Updated by [DELETED ACCOUNT]"
                                setShimmerCreatorLayout(false)
                            }

                            Response.Loading -> {
                                setShimmerCreatorLayout(true)
                            }

                            is Response.Success -> {
                                val acc = it.data
                                if (acc == null) {
                                    creatorInfoBinding.lastUpdatedByTv4.text =
                                        "Last Updated by [DELETED ACCOUNT]"
                                } else {
                                    creatorInfoBinding.lastUpdatedByTv4.text =
                                        "Last Updated by ${acc.first_name} ${acc.last_name}"
                                    acc.profileUri?.let { uri ->
                                        Glide.with(requireContext())
                                            .load(uri)
                                            .centerCrop()
                                            .into(creatorInfoBinding.lastUpdatedIv)
                                    }
                                }
                                setShimmerCreatorLayout(false)
                            }
                        }
                    }
                }
                launch {
                    accountViewModel.getAccount(modifier.createdBy) {
                        when (it) {
                            is Response.Error -> {
                                creatorInfoBinding.createdByTv4.text = "Created by [DELETED ACCOUNT]"
                                setShimmerCreatorLayout(false)
                            }

                            Response.Loading -> {
                                setShimmerCreatorLayout(true)
                            }

                            is Response.Success -> {
                                val acc = it.data
                                if (acc == null) {
                                    creatorInfoBinding.createdByTv4.text =
                                        "Created by [DELETED ACCOUNT]"
                                } else {
                                    creatorInfoBinding.createdByTv4.text =
                                        "Created by ${acc.first_name} ${acc.last_name}"
                                    acc.profileUri?.let { uri ->
                                        Glide.with(requireContext())
                                            .load(uri)
                                            .centerCrop()
                                            .into(creatorInfoBinding.createdByIv)
                                    }
                                }
                                setShimmerCreatorLayout(false)
                            }
                        }
                    }
                }
            }
        }

        //add modifier_view_component to the base layout
        if (modifierBinding.root.parent == null)
            binding.baseLayout.addView(modifierBinding.root)

        if (detailedModifierBinding.root.parent != null)
            (detailedModifierBinding.root.parent as ViewGroup).removeView(detailedModifierBinding.root)
        modifierBinding.modifierDetailedViewContainer.addView(_detailedModifierBinding?.root)

        if (creatorInfoBinding.root.parent == null)
            modifierBinding.creatorCl.addView(creatorInfoBinding.root)

        detailedModifierBinding.apply {
            modifier.imageUri?.let{
                Glide.with(requireContext())
                    .load(it.toUri())
                    .centerCrop()
                    .into(detailedModifierBinding.modifierIv)
            }
            multipleChoiceValueTv.text = if (modifier.multipleChoice) "Yes" else "No"
            requiredValueTv.text = if (modifier.required) "Yes" else "No"
            if (modifier.multipleChoice){
                minSelectionValueTv.visibility = View.VISIBLE
                minSelectionNameTv.visibility = View.VISIBLE
                maxSelectionValueTv.visibility = View.VISIBLE
                maxSelectionNameTv.visibility = View.VISIBLE
                divider9.visibility = View.VISIBLE
                divider8.visibility = View.VISIBLE
                minSelectionValueTv.text = modifier.minItem.toString()
                maxSelectionValueTv.text = modifier.maxItem.toString()
            }
        }

        loadModifierItem(modifier)
        loadBottomSheet()

        creatorInfoBinding.lastUpdatedTv.text = "Last Updated ${modifier.lastUpdated}"
    }

    private fun loadBottomSheet(){
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
        creatorInfoBinding.lastUpdatedTv.text = "Last Updated ${food.lastUpdated}"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    accountViewModel.getAccount(food.lastUpdatedBy) {
                        when (it) {
                            is Response.Error -> {
                                creatorInfoBinding.lastUpdatedByTv4.text =
                                    "Last Updated by [DELETED ACCOUNT]"
                                setShimmerCreatorLayout(false)
                            }

                            Response.Loading -> {
                                setShimmerCreatorLayout(true)
                            }

                            is Response.Success -> {
                                val acc = it.data
                                if (acc == null) {
                                    creatorInfoBinding.lastUpdatedByTv4.text =
                                        "Last Updated by [DELETED ACCOUNT]"
                                } else {
                                    creatorInfoBinding.lastUpdatedByTv4.text =
                                        "Last Updated by ${acc.first_name} ${acc.last_name}"
                                    acc.profileUri?.let { uri ->
                                        Glide.with(requireContext())
                                            .load(uri)
                                            .centerCrop()
                                            .into(creatorInfoBinding.lastUpdatedIv)
                                    }
                                }
                                setShimmerCreatorLayout(false)
                            }
                        }
                    }
                }
                launch {
                    accountViewModel.getAccount(food.createdBy) {
                        when (it) {
                            is Response.Error -> {
                                creatorInfoBinding.createdByTv4.text = "Created by [DELETED ACCOUNT]"
                                setShimmerCreatorLayout(false)
                            }

                            Response.Loading -> {
                                setShimmerCreatorLayout(true)
                            }

                            is Response.Success -> {
                                val acc = it.data
                                if (acc == null) {
                                    creatorInfoBinding.createdByTv4.text = "Created by [DELETED ACCOUNT]"
                                } else {
                                    creatorInfoBinding.createdByTv4.text =
                                        "Created by ${acc.first_name} ${acc.last_name}"
                                    acc.profileUri?.let { uri ->
                                        Glide.with(requireContext())
                                            .load(uri)
                                            .centerCrop()
                                            .into(creatorInfoBinding.createdByIv)
                                    }
                                }
                                setShimmerCreatorLayout(false)
                            }
                        }
                    }
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

        if (creatorInfoBinding.root.parent == null)
            foodBinding.creatorLayout.addView(creatorInfoBinding.root)

        loadBottomSheet()

    }

    //true if the id is to be removed
    private fun errorModifierDialog(msg: String,id:String,food:Food){
        val list = food.modifierList as MutableList
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exception occurred!")
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exception occurred!")
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
            viewModel.deleteResponse.collect {
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
            viewModel.updateAvailabilityResponse.collect {
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
                            loadBottomSheet()
                        }
                        if (it.data == "Updated Availability!"){
                            successToast(it.data)
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
        _binding = null
        _foodBinding = null
        _modifierBinding = null
    }
}