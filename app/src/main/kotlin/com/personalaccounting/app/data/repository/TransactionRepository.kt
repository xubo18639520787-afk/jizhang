package com.personalaccounting.app.data.repository

import com.personalaccounting.app.data.dao.TransactionDao
import com.personalaccounting.app.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
    
    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAccount(accountId)
    }
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(categoryId)
    }
    
    fun getTransactionsByType(type: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByType(type)
    }
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsByDateRangeAndType(startDate: Date, endDate: Date, type: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRangeAndType(startDate, endDate, type)
    }
    
    suspend fun getTotalAmountByType(type: Int): BigDecimal {
        return transactionDao.getTotalAmountByType(type) ?: BigDecimal.ZERO
    }
    
    suspend fun getTotalAmountByTypeAndDateRange(type: Int, startDate: Date, endDate: Date): BigDecimal {
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate) ?: BigDecimal.ZERO
    }
    
    suspend fun getTotalBalance(): BigDecimal {
        return transactionDao.getTotalBalance() ?: BigDecimal.ZERO
    }
    
    suspend fun getBalanceByDateRange(startDate: Date, endDate: Date): BigDecimal {
        return transactionDao.getBalanceByDateRange(startDate, endDate) ?: BigDecimal.ZERO
    }
    
    fun searchTransactions(keyword: String): Flow<List<TransactionEntity>> {
        return transactionDao.searchTransactions(keyword)
    }
    
    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }
    
    suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }
    
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction.copy(updatedAt = Date()))
    }
    
    suspend fun deleteTransaction(id: Long) {
        transactionDao.softDeleteTransaction(id)
    }
    
    suspend fun permanentDeleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun getTransactionCount(): Int {
        return transactionDao.getTransactionCount()
    }
    
    suspend fun getTransactionsPaged(limit: Int, offset: Int): List<TransactionEntity> {
        return transactionDao.getTransactionsPaged(limit, offset)
    }
    
    // 获取今日收支统计
    suspend fun getTodayStatistics(): Pair<BigDecimal, BigDecimal> {
        val today = Date()
        val startOfDay = Date(today.time - today.time % (24 * 60 * 60 * 1000))
        val endOfDay = Date(startOfDay.time + 24 * 60 * 60 * 1000 - 1)
        
        val income = getTotalAmountByTypeAndDateRange(TransactionEntity.TYPE_INCOME, startOfDay, endOfDay)
        val expense = getTotalAmountByTypeAndDateRange(TransactionEntity.TYPE_EXPENSE, startOfDay, endOfDay)
        
        return Pair(income, expense)
    }
    
    // 获取本月收支统计
    suspend fun getMonthlyStatistics(): Pair<BigDecimal, BigDecimal> {
        val now = Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = now
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time
        
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.time
        
        val income = getTotalAmountByTypeAndDateRange(TransactionEntity.TYPE_INCOME, startOfMonth, endOfMonth)
        val expense = getTotalAmountByTypeAndDateRange(TransactionEntity.TYPE_EXPENSE, startOfMonth, endOfMonth)
        
        return Pair(income, expense)
    }
}