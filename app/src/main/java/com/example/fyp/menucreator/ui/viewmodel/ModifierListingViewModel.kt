package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModifierListingViewModel @Inject constructor(
    private val getModifierListUseCase: GetModifierListUseCase,
    private val getModifierItemListUseCase: GetModifierItemListUseCase
) : ViewModel(){

    private val _modifiers = MutableStateFlow<UiState<List<Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private val _modifierItems = MutableStateFlow<UiState<List<ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    private val modifierMap = mutableMapOf<String, Modifier>()

    private val itemMap = mutableMapOf<String, ModifierItem>()

    init{
        getModifierItemList()
        getModifierList()
    }

    private fun getModifierList() {
        _modifiers.value = UiState.Loading
        getModifierListUseCase.invoke {
            viewModelScope.launch() {
                it.collect() {
                    when (it) {
                        is UiState.Success -> {
                            modifierMap.clear()
                            for (modifier in it.data){
                                modifierMap[modifier.productId] = modifier
                            }
                            _modifiers.value = UiState.Success(it.data.sortedBy { it1 -> it1.name })
                        }
                        else -> {
                            _modifiers.value = it
                        }
                    }
                }
            }
        }
    }

    private fun getModifierItemList() {
        _modifierItems.value = UiState.Loading
        getModifierItemListUseCase.invoke {
            viewModelScope.launch() {
                it.collect() {
                    when (it) {
                        is UiState.Success -> {
                            itemMap.clear()
                            for (item in it.data){
                                itemMap[item.productId] = item
                            }
                            _modifierItems.value = UiState.Success(it.data.sortedBy { it1 -> it1.name })
                        }
                        else -> {
                            _modifierItems.value = it
                        }
                    }
                }
            }
        }
    }

    fun getModifier(id:String) : Modifier? {
        return modifierMap[id]
    }

    fun getModifierItem(id:String) : ModifierItem? {
        return itemMap[id]
    }

}