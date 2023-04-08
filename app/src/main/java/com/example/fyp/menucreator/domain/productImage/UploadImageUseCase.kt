package com.example.fyp.menucreator.domain.productImage

import android.net.Uri
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val imageRepo: ProductImageRepository
) {
    suspend operator fun invoke(productType: ProductType, id: String, uri: Uri,result: (UiState<Boolean>) -> Unit): Pair<String,String>? {
        return imageRepo.uploadImage(productType,id, uri,result)
    }
}