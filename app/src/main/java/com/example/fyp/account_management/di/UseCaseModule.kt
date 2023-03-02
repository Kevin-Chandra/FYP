package com.example.fyp.account_management.di

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.domain.use_case.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepo: AuthRepository
    ) : LoginUseCase {
        return LoginUseCase(authRepo)
    }

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepo: AuthRepository
    ) : LogoutUseCase {
        return LogoutUseCase(authRepo)
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(
        authRepo: AuthRepository
    ) : RegisterUseCase {
        return RegisterUseCase(authRepo)
    }

    @Provides
    @Singleton
    fun provideEditAccountUseCase(
        authRepo: AuthRepository
    ) : EditAccountUseCase {
        return EditAccountUseCase(authRepo)
    }

    @Provides
    @Singleton
    fun provideResetPasswordUseCase(
        authRepo: AuthRepository
    ) : ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepo)
    }

    @Provides
    @Singleton
    fun provideValidateEmailUseCase() = ValidateEmailUseCase()

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase() = ValidatePasswordUseCase()

    @Provides
    @Singleton
    fun provideValidatePhoneUseCase() = ValidatePhoneUseCase()

    @Provides
    @Singleton
    fun provideValidateNameUseCase() = ValidateNameUseCase()

    @Provides
    @Singleton
    fun provideUpdatePasswordUseCase(
        authRepo: AuthRepository
    ) = ChangePasswordUseCase(authRepo)

}