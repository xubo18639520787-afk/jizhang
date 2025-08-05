package com.personalaccounting.app.data.repository

import com.personalaccounting.app.data.dao.AccountDao
import com.personalaccounting.app.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    
    fun getAllAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAllAccounts()
    }
    
    suspend fun getAccountById(id: Long): AccountEntity? {
        return accountDao.getAccountById(id)
    }
    
    suspend fun getDefaultAccount(): AccountEntity? {
        return accountDao.getDefaultAccount()
    }
    
    fun getAccountsByType(type: Int): Flow<List<AccountEntity>> {
        return accountDao.getAccountsByType(type)
    }
    
    fun searchAccounts(keyword: String): Flow<List<AccountEntity>> {
        return accountDao.searchAccounts(keyword)
    }
    
    suspend fun updateAccountBalance(accountId: Long, amount: BigDecimal) {
        accountDao.updateAccountBalance(accountId, amount)
    }
    
    suspend fun setDefaultAccount(accountId: Long) {
        accountDao.clearDefaultAccount()
        accountDao.setDefaultAccount(accountId)
    }
    
    suspend fun insertAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }
    
    suspend fun insertAccounts(accounts: List<AccountEntity>) {
        accountDao.insertAccounts(accounts)
    }
    
    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account.copy(updatedAt = Date()))
    }
    
    suspend fun deleteAccount(id: Long) {
        accountDao.softDeleteAccount(id)
    }
    
    suspend fun permanentDeleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }
    
    suspend fun getAccountCount(): Int {
        return accountDao.getAccountCount()
    }
    
    suspend fun getTotalBalance(): BigDecimal {
        return accountDao.getTotalBalance() ?: BigDecimal.ZERO
    }
}