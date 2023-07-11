package com.example.fyp.menucreator.di

import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.food.*
import com.example.fyp.menucreator.domain.modifier.AddModifierUseCase
import com.example.fyp.menucreator.domain.modifier.DeleteModifierUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierUseCase
import com.example.fyp.menucreator.domain.modifierItem.AddModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.DeleteModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ModifierModule {

    @Provides
    @ViewModelScoped
    fun providesGetModifierListUseCase(repository: ModifierRepository): GetModifierListUseCase {
        return GetModifierListUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun providesGetModifierUseCase(repository: ModifierRepository): GetModifierUseCase {
        return GetModifierUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun providesDeleteModifierUseCase(repository: ModifierRepository,getModifierUseCase: GetModifierUseCase,deleteModifierItemUseCase: DeleteModifierItemUseCase,deleteImageUseCase: DeleteImageUseCase): DeleteModifierUseCase {
        return DeleteModifierUseCase(repository,getModifierUseCase,deleteModifierItemUseCase,deleteImageUseCase)
    }

    @Provides
    @ViewModelScoped
    fun providesAddModifierUseCase(repository: ModifierRepository,deleteModifierUseCase: DeleteModifierUseCase,uploadImageUseCase: UploadImageUseCase,addModifierItemUseCase: AddModifierItemUseCase): AddModifierUseCase {
        return AddModifierUseCase(repository,uploadImageUseCase ,deleteModifierUseCase,addModifierItemUseCase)
    }

}