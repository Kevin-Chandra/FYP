package com.example.fyp.menucreator.domain.productSettings

import com.example.fyp.menucreator.data.repository.ProductSettingsDao
import javax.inject.Inject

class GetTaxUseCase @Inject constructor(
    private val productSettingsDao: ProductSettingsDao,
) {
    suspend operator fun invoke() = productSettingsDao.getTax()
}