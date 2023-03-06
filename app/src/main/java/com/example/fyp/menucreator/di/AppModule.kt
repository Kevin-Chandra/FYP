package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.UpdateFoodCategoryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule  {

    @Provides
    @Singleton
    fun provideFoodRepository(): FoodRepository{
        return FoodRepository()
    }

    @Provides
    @Singleton
    fun provideModifierRepository(): ModifierRepository{
        return ModifierRepository()
    }

    @Provides
    @Singleton
    fun provideModifierItemRepository(): ModifierItemRepository{
        return ModifierItemRepository()
    }

    @Provides
    @Singleton
    fun provideMenuSettingsRepository(): MenuSettingsRepository{
        return MenuSettingsRepository()
    }

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(repo : MenuSettingsRepository): AddFoodCategoryUseCase{
        return AddFoodCategoryUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideUpdateCategoryUseCase(repo : MenuSettingsRepository): UpdateFoodCategoryUseCase{
        return UpdateFoodCategoryUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(repo : MenuSettingsRepository): DeleteFoodCategoryUseCase{
        return DeleteFoodCategoryUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideCategoryUseCase(repo : MenuSettingsRepository): GetFoodCategoryUseCase{
        return GetFoodCategoryUseCase(repo)
    }
}