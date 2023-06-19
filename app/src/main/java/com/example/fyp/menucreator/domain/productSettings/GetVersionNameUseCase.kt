package com.example.fyp.menucreator.domain.productSettings

import com.example.fyp.menucreator.data.repository.ProductSettingsRepository
import javax.inject.Inject

class GetVersionNameUseCase @Inject constructor(
    private val productSettingsRepository: ProductSettingsRepository,
) {

    suspend operator fun invoke() = productSettingsRepository.getVersionName()
}