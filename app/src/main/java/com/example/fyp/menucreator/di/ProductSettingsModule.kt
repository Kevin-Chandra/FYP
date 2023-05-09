package com.example.fyp.menucreator.di

import android.content.Context
import androidx.room.Room
import com.example.fyp.menucreator.data.repository.ProductSettingsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ProductSettingsModule  {

    @Singleton
    @Provides
    fun providesProductSettingsDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        ProductSettingsDatabase::class.java,
        "product_settings_database")
        .build()

    @Singleton
    @Provides
    fun providesProductSettingsDao(db: ProductSettingsDatabase) = db.productSettingsDao()
}