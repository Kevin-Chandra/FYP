package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fyp.ordering_system.domain.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.GetCartUseCase
import com.example.fyp.ordering_system.domain.UpsertToCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val upsertToCartUseCase: UpsertToCartUseCase,
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    private val getCartUseCase: GetCartUseCase,
) : ViewModel(){

}