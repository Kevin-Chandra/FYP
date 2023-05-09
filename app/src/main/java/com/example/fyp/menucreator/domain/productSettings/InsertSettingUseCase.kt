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

class InsertSettingUseCase @Inject constructor(
    private val productSettingsDao: ProductSettingsDao,
    private val productSettingsRepository: ProductSettingsRepository,
) {

    suspend fun invoke( result: (Response<String>) -> Unit ) = CoroutineScope(Dispatchers.IO).launch{
        var settings = ProductSettings(id = FireStoreDocumentField.PRODUCT_SETTINGS)

        val serviceCharge = async { productSettingsRepository.getServiceCharge() }
        val tax = async { productSettingsRepository.getTax() }

        val a = serviceCharge.await()
        if (a is Response.Success){
            settings = settings.copy( serviceCharge = a.data)
        }
        val b = tax.await()
        if (b is Response.Success){
            settings = settings.copy( tax = b.data)
        }
        productSettingsDao.insertSettings(settings)
        result.invoke(Response.Success("Success inserting setting to database"))
    }
}