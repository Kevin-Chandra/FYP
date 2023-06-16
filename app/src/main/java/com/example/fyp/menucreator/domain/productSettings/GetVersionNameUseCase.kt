package com.example.fyp.menucreator.domain.productSettings

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.ProductSettings
import com.example.fyp.menucreator.data.repository.ProductSettingsDao
import com.example.fyp.menucreator.data.repository.ProductSettingsRepository
import com.example.fyp.menucreator.util.FireStoreDocumentField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class GetVersionNameUseCase @Inject constructor(
    private val productSettingsRepository: ProductSettingsRepository,
) {

    suspend operator fun invoke() = productSettingsRepository.getVersionName()
}