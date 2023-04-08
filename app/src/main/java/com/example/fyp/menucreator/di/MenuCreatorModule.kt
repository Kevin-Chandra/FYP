package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.domain.validation.ValidateDuplicateIdUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductCategoryUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductNameUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductPriceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MenuCreatorModule {

    @Provides
    @Singleton
    fun providesValidateNameUseCase(): ValidateProductNameUseCase {
        return ValidateProductNameUseCase()
    }

    @Provides
    @Singleton
    fun providesValidateCategoryUseCase(): ValidateProductCategoryUseCase {
        return ValidateProductCategoryUseCase()
    }

    @Provides
    @Singleton
    fun providesValidateProductPriceUseCase(): ValidateProductPriceUseCase {
        return ValidateProductPriceUseCase()
    }

    @Provides
    @Singleton
    fun providesValidateDuplicateIdUseCase(): ValidateDuplicateIdUseCase {
        return ValidateDuplicateIdUseCase()
    }
}