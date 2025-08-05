package com.personalaccounting.app.data.dao

import androidx.room.*
import com.personalaccounting.app.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE type = :type AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByType(type: Int): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = :type AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByDateRangeAndType(startDate: Date, endDate: Date, type: Int): Flow<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND isDeleted = 0")
    suspend fun getTotalAmountByType(type: Int): BigDecimal?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate AND isDeleted = 0")
    suspend fun getTotalAmountByTypeAndDateRange(type: Int, startDate: Date, endDate: Date): BigDecimal?
    
    @Query("SELECT SUM(CASE WHEN type = 1 THEN amount ELSE 0 END) - SUM(CASE WHEN type = 0 THEN amount ELSE 0 END) FROM transactions WHERE isDeleted = 0")
    suspend fun getTotalBalance(): BigDecimal?
    
    @Query("SELECT SUM(CASE WHEN type = 1 THEN amount ELSE 0 END) - SUM(CASE WHEN type = 0 THEN amount ELSE 0 END) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0")
    suspend fun getBalanceByDateRange(startDate: Date, endDate: Date): BigDecimal?
    
    @Query("SELECT * FROM transactions WHERE (note LIKE '%' || :keyword || '%' OR tags LIKE '%' || :keyword || '%') AND isDeleted = 0 ORDER BY date DESC")
    fun searchTransactions(keyword: String): Flow<List<TransactionEntity>>
    
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Insert
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long, deletedAt: Date = Date())
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE isDeleted = 1")
    suspend fun deleteAllSoftDeletedTransactions()
    
    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTransactionCount(): Int
    
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC, createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaged(limit: Int, offset: Int): List<TransactionEntity>
}