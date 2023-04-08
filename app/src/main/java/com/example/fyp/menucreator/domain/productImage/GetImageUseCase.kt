package com.example.fyp.menucreator.domain.productImage

import com.example.fyp.menucreator.data.repository.ProductImageRepository
import javax.inject.Inject

class GetImageUseCase @Inject constructor(
    private val imageRepository: ProductImageRepository
) {
    suspend operator fun invoke(path: String) = imageRepository.getImage(path)

}