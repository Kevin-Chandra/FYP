package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.*
import com.example.fyp.menucreator.domain.*
import com.example.fyp.menucreator.domain.foodCategory.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.UpdateFoodCategoryUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AppModule  {

    @Provides
    @ViewModelScoped
    fun provideFoodRepository(): FoodRepository{
        return FoodRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideModifierRepository(): ModifierRepository{
        return ModifierRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideModifierItemRepository(): ModifierItemRepository{
        return ModifierItemRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideMenuSettingsRepository(): MenuSettingsRepository{
        return MenuSettingsRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideAddCategoryUseCase(repo : MenuSettingsRepository): AddFoodCategoryUseCase {
        return AddFoodCategoryUseCase(repo)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateCategoryUseCase(repo : MenuSettingsRepository): UpdateFoodCategoryUseCase {
        return UpdateFoodCategoryUseCase(repo)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteCategoryUseCase(repo : MenuSettingsRepository): DeleteFoodCategoryUseCase {
        return DeleteFoodCategoryUseCase(repo)
    }

    @Provides
    @ViewModelScoped
    fun provideCategoryUseCase(repo : MenuSettingsRepository): GetFoodCategoryUseCase {
        return GetFoodCategoryUseCase(repo)
    }

    @Provides
    @ViewModelScoped
    fun provideImageRepo(): ProductImageRepository{
        return ProductImageRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideUploadImageUseCase(repo : ProductImageRepository): UploadImageUseCase {
        return UploadImageUseCase(repo)
    }
}