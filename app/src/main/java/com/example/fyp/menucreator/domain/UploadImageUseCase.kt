package com.example.fyp.menucreator.domain

import android.net.Uri
import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val imageRepo: ProductImageRepository
) {
    suspend operator fun invoke(uri: Uri,result: (UiState<Pair<String,String>>) -> Unit){
        imageRepo.uploadImage(uri,result)
    }
}