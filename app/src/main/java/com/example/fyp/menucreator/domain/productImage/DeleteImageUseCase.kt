package com.example.fyp.menucreator.domain.productImage

import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val imageRepository: ProductImageRepository
) {
    operator fun invoke(path: String,result: (UiState<String>) -> Unit){
        imageRepository.deleteImage(path,result)
    }

}