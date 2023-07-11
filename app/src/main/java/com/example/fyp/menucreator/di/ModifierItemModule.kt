package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.domain.food.*
import com.example.fyp.menucreator.domain.modifierItem.AddModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.DeleteModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ModifierItemModule {

    @Provides
    @ViewModelScoped
    fun providesGetModifierItemListUseCase(repository: ModifierItemRepository): GetModifierItemListUseCase {
        return GetModifierItemListUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun providesGetModifierItemUseCase(repository: ModifierItemRepository): GetModifierItemUseCase {
        return GetModifierItemUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun providesDeleteModifierItemUseCase(repository: ModifierItemRepository): DeleteModifierItemUseCase {
        return DeleteModifierItemUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun providesAddModifierItemUseCase(repository: ModifierItemRepository): AddModifierItemUseCase {
        return AddModifierItemUseCase(repository)
    }

}