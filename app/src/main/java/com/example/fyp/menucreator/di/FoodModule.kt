package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.domain.food.*
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object FoodModule {

    @Provides
    @ViewModelScoped
    fun providesAddFoodUseCase(foodRepository: FoodRepository, imageUseCase: UploadImageUseCase, deleteFoodUseCase: DeleteFoodUseCase): AddFoodUseCase {
        return AddFoodUseCase(foodRepository,imageUseCase,deleteFoodUseCase)
    }

    @Provides
    @ViewModelScoped
    fun providesUpdateFoodUseCase(foodRepository: FoodRepository,imageUseCase: UploadImageUseCase): UpdateFoodUseCase {
        return UpdateFoodUseCase(foodRepository,imageUseCase)
    }

    @Provides
    @ViewModelScoped
    fun providesDeleteFoodUseCase(foodRepository: FoodRepository,getFoodUseCase: GetFoodUseCase,deleteImageUseCase: DeleteImageUseCase): DeleteFoodUseCase {
        return DeleteFoodUseCase(getFoodUseCase,foodRepository,deleteImageUseCase)
    }

    @Provides
    @ViewModelScoped
    fun providesGetFoodListUseCase(foodRepository: FoodRepository): GetFoodListUseCase {
        return GetFoodListUseCase(foodRepository)
    }

    @Provides
    @ViewModelScoped
    fun providesGetFoodUseCase(foodRepository: FoodRepository): GetFoodUseCase {
        return GetFoodUseCase(foodRepository)
    }
}