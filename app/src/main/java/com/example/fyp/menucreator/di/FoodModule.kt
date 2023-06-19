package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.domain.food.*
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoodModule {

    @Provides
    @Singleton
    fun providesAddFoodUseCase(foodRepository: FoodRepository, imageUseCase: UploadImageUseCase, deleteFoodUseCase: DeleteFoodUseCase): AddFoodUseCase {
        return AddFoodUseCase(foodRepository,imageUseCase,deleteFoodUseCase)
    }

    @Provides
    @Singleton
    fun providesUpdateFoodUseCase(foodRepository: FoodRepository,imageUseCase: UploadImageUseCase): UpdateFoodUseCase {
        return UpdateFoodUseCase(foodRepository,imageUseCase)
    }

    @Provides
    @Singleton
    fun providesDeleteFoodUseCase(foodRepository: FoodRepository,getFoodUseCase: GetFoodUseCase,deleteImageUseCase: DeleteImageUseCase): DeleteFoodUseCase {
        return DeleteFoodUseCase(getFoodUseCase,foodRepository,deleteImageUseCase)
    }

    @Provides
    @Singleton
    fun providesGetFoodListUseCase(foodRepository: FoodRepository): GetFoodListUseCase {
        return GetFoodListUseCase(foodRepository)
    }

    @Provides
    @Singleton
    fun providesGetFoodUseCase(foodRepository: FoodRepository): GetFoodUseCase {
        return GetFoodUseCase(foodRepository)
    }
}