package com.example.fyp.ordering_system.di

import android.content.Context
import androidx.room.Room
import com.example.fyp.menucreator.data.repository.*
import com.example.fyp.menucreator.domain.*
import com.example.fyp.menucreator.domain.foodCategory.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.UpdateFoodCategoryUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.ordering_system.data.repository.local.OnlineOrderingDatabase
import com.example.fyp.ordering_system.domain.validation.ValidateModifierUseCase
import com.example.fyp.ordering_system.domain.validation.ValidateQuantityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnlineOrderingModule  {

    @Singleton
    @Provides
    fun provideOnlineOrderingDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
            app,
            OnlineOrderingDatabase::class.java,
            "online_ordering_database")
            .build()

    @Singleton
    @Provides
    fun provideOrderItemDao(db: OnlineOrderingDatabase) = db.orderItemDao()

    @Singleton
    @Provides
    fun provideOrderModifierValidation() = ValidateModifierUseCase()

    @Singleton
    @Provides
    fun provideOrderQuantityValidation() = ValidateQuantityUseCase()

}