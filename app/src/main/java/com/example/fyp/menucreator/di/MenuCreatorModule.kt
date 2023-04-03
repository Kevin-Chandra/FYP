package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.UploadImageUseCase
import com.example.fyp.menucreator.domain.ValidateProductCategoryUseCase
import com.example.fyp.menucreator.domain.ValidateProductNameUseCase
import com.example.fyp.menucreator.domain.ValidateProductPriceUseCase
import com.example.fyp.menucreator.domain.food.UpdateFoodUseCase
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
}