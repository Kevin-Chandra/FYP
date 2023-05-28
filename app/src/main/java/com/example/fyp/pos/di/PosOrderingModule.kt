package com.example.fyp.pos.di

import android.content.Context
import androidx.room.Room
import com.example.fyp.menucreator.data.repository.*
import com.example.fyp.menucreator.domain.*
import com.example.fyp.pos.data.repository.local.PosOrderingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PosOrderingModule  {

    @Singleton
    @Provides
    fun providePosOrderingDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
            app,
            PosOrderingDatabase::class.java,
            "pos_ordering_database")
            .build()

    @Singleton
    @Provides
    fun providePosOrderItemDao(db: PosOrderingDatabase) = db.orderItemDao()

}