package com.example.fyp.menucreator.domain

import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val imageRepository: ProductImageRepository
) {
    operator fun invoke(path: String,result: (UiState<Boolean>) -> Unit){
        imageRepository.deleteImage(path,result)
    }

}