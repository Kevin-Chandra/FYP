package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
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
}