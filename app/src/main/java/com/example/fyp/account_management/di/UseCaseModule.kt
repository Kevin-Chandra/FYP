package com.example.fyp.account_management.di

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(
        authRepo: AuthRepository
    ) : LoginUseCase {
        return LoginUseCase(authRepo)
    }

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(
        authRepo: AuthRepository
    ) : LogoutUseCase {
        return LogoutUseCase(authRepo)
    }

    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(
        authRepo: AuthRepository
    ) : RegisterUseCase {
        return RegisterUseCase(authRepo)
    }

    @Provides
    @ViewModelScoped
    fun provideEditAccountUseCase(
        authRepo: AuthRepository
    ) : EditAccountUseCase {
        return EditAccountUseCase(authRepo)
    }

    @Provides
    @ViewModelScoped
    fun provideResetPasswordUseCase(
        authRepo: AuthRepository
    ) : ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepo)
    }

    @Provides
    @ViewModelScoped
    fun provideValidateEmailUseCase() = ValidateEmailUseCase()

    @Provides
    @ViewModelScoped
    fun provideValidatePasswordUseCase() = ValidatePasswordUseCase()

    @Provides
    @ViewModelScoped
    fun provideValidatePhoneUseCase() = ValidatePhoneUseCase()

    @Provides
    @ViewModelScoped
    fun provideValidateNameUseCase() = ValidateNameUseCase()

    @Provides
    @ViewModelScoped
    fun provideUpdatePasswordUseCase(
        authRepo: AuthRepository
    ) = ChangePasswordUseCase(authRepo)

}