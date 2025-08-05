package com.personalaccounting.app.di

import android.content.Context
import androidx.room.Room
import com.personalaccounting.app.data.dao.AccountDao
import com.personalaccounting.app.data.dao.CategoryDao
import com.personalaccounting.app.data.dao.TransactionDao
import com.personalaccounting.app.data.database.AccountingDatabase
import com.personalaccounting.app.data.database.DatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAccountingDatabase(
        @ApplicationContext context: Context
    ): AccountingDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AccountingDatabase::class.java,
            AccountingDatabase.DATABASE_NAME
        )
        .addCallback(DatabaseCallback())
        .build()
    }
    
    @Provides
    fun provideTransactionDao(database: AccountingDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideAccountDao(database: AccountingDatabase): AccountDao {
        return database.accountDao()
    }
    
    @Provides
    fun provideCategoryDao(database: AccountingDatabase): CategoryDao {
        return database.categoryDao()
    }
}