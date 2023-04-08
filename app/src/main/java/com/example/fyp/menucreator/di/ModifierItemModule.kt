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
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModifierItemModule {

    @Provides
    @Singleton
    fun providesGetModifierItemListUseCase(repository: ModifierItemRepository): GetModifierItemListUseCase {
        return GetModifierItemListUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesGetModifierItemUseCase(repository: ModifierItemRepository): GetModifierItemUseCase {
        return GetModifierItemUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesDeleteModifierItemUseCase(repository: ModifierItemRepository): DeleteModifierItemUseCase {
        return DeleteModifierItemUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesAddModifierItemUseCase(repository: ModifierItemRepository): AddModifierItemUseCase {
        return AddModifierItemUseCase(repository)
    }

}