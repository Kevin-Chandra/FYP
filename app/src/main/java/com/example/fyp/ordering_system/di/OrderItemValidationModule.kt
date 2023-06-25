package com.example.fyp.ordering_system.di

import com.example.fyp.menucreator.data.repository.*
import com.example.fyp.menucreator.domain.*
import com.example.fyp.ordering_system.domain.validation.ValidateModifierUseCase
import com.example.fyp.ordering_system.domain.validation.ValidateQuantityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object OrderItemValidationModule  {

    @ViewModelScoped
    @Provides
    fun provideOrderModifierValidation() = ValidateModifierUseCase()

    @ViewModelScoped
    @Provides
    fun provideOrderQuantityValidation() = ValidateQuantityUseCase()

}