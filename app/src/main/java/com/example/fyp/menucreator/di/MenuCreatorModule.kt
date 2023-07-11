package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.domain.validation.ValidateDuplicateIdUseCase
import com.example.fyp.menucreator.domain.validation.ValidateModifierSelectionRangeUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductCategoryUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductNameUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductPriceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object MenuCreatorModule {

    @Provides
    @ViewModelScoped
    fun providesValidateNameUseCase(): ValidateProductNameUseCase {
        return ValidateProductNameUseCase()
    }

    @Provides
    @ViewModelScoped
    fun providesValidateCategoryUseCase(): ValidateProductCategoryUseCase {
        return ValidateProductCategoryUseCase()
    }

    @Provides
    @ViewModelScoped
    fun providesValidateProductPriceUseCase(): ValidateProductPriceUseCase {
        return ValidateProductPriceUseCase()
    }

    @Provides
    @ViewModelScoped
    fun providesValidateDuplicateIdUseCase(): ValidateDuplicateIdUseCase {
        return ValidateDuplicateIdUseCase()
    }

    @Provides
    @ViewModelScoped
    fun providesValidateModifierSelectionRangeUseCase(): ValidateModifierSelectionRangeUseCase {
        return ValidateModifierSelectionRangeUseCase()
    }
}