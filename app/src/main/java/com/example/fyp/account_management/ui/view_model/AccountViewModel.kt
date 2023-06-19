package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val getAccountByReturnUseCase: GetAccountByReturnUseCase,
) : ViewModel() {

    suspend fun getAccount( id:String , result: (Response<Account?>) -> Unit) {
        getAccountUseCase(id){
            result.invoke(it)
        }
    }

    suspend fun getAccount(id :String) = getAccountByReturnUseCase(id)
}