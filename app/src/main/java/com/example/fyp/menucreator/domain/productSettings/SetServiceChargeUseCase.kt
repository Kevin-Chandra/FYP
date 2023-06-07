package com.example.fyp.menucreator.domain.productSettings

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.repository.ProductSettingsDao
import com.example.fyp.menucreator.data.repository.ProductSettingsRepository
import java.lang.Exception
import javax.inject.Inject

class SetServiceChargeUseCase @Inject constructor(
    private val productSettingsDao: ProductSettingsDao,
    private val productSettingsRepository: ProductSettingsRepository,
) {

    suspend operator fun invoke(account: Account, percentage: Double, result: (Response<String>) -> Unit){
        println(account)
        if (account.accountType !in listOf(AccountType.Admin,AccountType.Manager)){
            result.invoke(Response.Error(Exception("Your account has no permission!")))
            return
        }
        if (percentage in 0.0..100.0){
            val serviceCharge = percentage/100.0
            val settings = productSettingsDao.getSettings()
            productSettingsDao.insertSettings(settings.copy( serviceCharge = serviceCharge ))
            productSettingsRepository.setServiceCharge(serviceCharge,result)
        } else {
            result.invoke(Response.Error(Exception("Invalid service charge percentage!")))
            return
        }
    }
}