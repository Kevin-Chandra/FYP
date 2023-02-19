package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModifierListingViewModel(
    private val modifierRepo: ModifierRepository,
    private val itemRepo: ModifierItemRepository
) : ViewModel(){

    private val _modifiers = MutableStateFlow<UiState<Map<String,Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private val _modifierItems = MutableStateFlow<UiState<Map<String,ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    private val _addModifierResponse = MutableStateFlow<UiState<Pair<Modifier?,String>>>(UiState.Loading)
    val addModifierResponse = _addModifierResponse.asStateFlow()

    init{
        getModifierItemList()
        getModifierList()
    }

    private fun getModifierList() = viewModelScope.launch{
        _modifiers.value = UiState.Loading
        modifierRepo.subscribeModifierUpdates().collect { result ->
            _modifiers.value = result
        }
    }

    private fun getModifierItemList() = viewModelScope.launch{
        _modifierItems.value = UiState.Loading
        itemRepo.subscribeModifierItemUpdates().collect { result ->
            _modifierItems.value = result
        }
    }
}