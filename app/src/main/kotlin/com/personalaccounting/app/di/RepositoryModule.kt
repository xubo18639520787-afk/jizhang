package com.personalaccounting.app.di

import com.personalaccounting.app.data.dao.AccountDao
import com.personalaccounting.app.data.dao.CategoryDao
import com.personalaccounting.app.data.dao.TransactionDao
import com.personalaccounting.app.data.repository.AccountRepository
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao)
    }
    
    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao
    ): AccountRepository {
        return AccountRepository(accountDao)
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
}