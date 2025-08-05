package com.personalaccounting.app.data.dao

import androidx.room.*
import com.personalaccounting.app.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date

@Dao
interface AccountDao {
    
    @Query("SELECT * FROM accounts WHERE isDeleted = 0 ORDER BY isDefault DESC, createdAt ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE id = :id AND isDeleted = 0")
    suspend fun getAccountById(id: Long): AccountEntity?
    
    @Query("SELECT * FROM accounts WHERE isDefault = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getDefaultAccount(): AccountEntity?
    
    @Query("SELECT * FROM accounts WHERE type = :type AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getAccountsByType(type: Int): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE name LIKE '%' || :keyword || '%' AND isDeleted = 0 ORDER BY createdAt ASC")
    fun searchAccounts(keyword: String): Flow<List<AccountEntity>>
    
    @Query("UPDATE accounts SET balance = balance + :amount, updatedAt = :updatedAt WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: Long, amount: BigDecimal, updatedAt: Date = Date())
    
    @Query("UPDATE accounts SET isDefault = 0")
    suspend fun clearDefaultAccount()
    
    @Query("UPDATE accounts SET isDefault = 1, updatedAt = :updatedAt WHERE id = :accountId")
    suspend fun setDefaultAccount(accountId: Long, updatedAt: Date = Date())
    
    @Insert
    suspend fun insertAccount(account: AccountEntity): Long
    
    @Insert
    suspend fun insertAccounts(accounts: List<AccountEntity>)
    
    @Update
    suspend fun updateAccount(account: AccountEntity)
    
    @Query("UPDATE accounts SET isDeleted = 1, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteAccount(id: Long, deletedAt: Date = Date())
    
    @Delete
    suspend fun deleteAccount(account: AccountEntity)
    
    @Query("DELETE FROM accounts WHERE isDeleted = 1")
    suspend fun deleteAllSoftDeletedAccounts()
    
    @Query("SELECT COUNT(*) FROM accounts WHERE isDeleted = 0")
    suspend fun getAccountCount(): Int
    
    @Query("SELECT SUM(balance) FROM accounts WHERE isDeleted = 0")
    suspend fun getTotalBalance(): BigDecimal?
}