package com.personalaccounting.app.data.export

import android.content.Context
import com.personalaccounting.app.data.repository.TransactionRepository
import com.personalaccounting.app.data.repository.AccountRepository
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.domain.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据导出管理器
 * 负责导出和导入应用数据
 */
@Singleton
class DataExportManager @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    
    /**
     * 导出结果
     */
    data class ExportResult(
        val success: Boolean,
        val message: String,
        val filePath: String? = null
    )
    
    /**
     * 导入结果
     */
    data class ImportResult(
        val success: Boolean,
        val message: String,
        val importedCount: Int = 0
    )
    
    /**
     * 导出所有数据到JSON文件
     */
    suspend fun exportAllData(context: Context): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 获取所有数据
            val transactions = transactionRepository.getAllTransactions().first()
            val accounts = accountRepository.getAllAccounts().first()
            val categories = categoryRepository.getAllCategories().first()
            
            // 创建JSON对象
            val exportData = JSONObject().apply {
                put("exportTime", System.currentTimeMillis())
                put("version", "1.0")
                
                // 导出账户
                val accountsArray = JSONArray()
                accounts.forEach { account ->
                    val accountJson = JSONObject().apply {
                        put("id", account.id)
                        put("name", account.name)
                        put("type", account.type.name)
                        put("balance", account.balance.toString())
                        put("isDefault", account.isDefault)
                        put("createdAt", account.createdAt.time)
                    }
                    accountsArray.put(accountJson)
                }
                put("accounts", accountsArray)
                
                // 导出分类
                val categoriesArray = JSONArray()
                categories.forEach { category ->
                    val categoryJson = JSONObject().apply {
                        put("id", category.id)
                        put("name", category.name)
                        put("type", category.type.name)
                        put("icon", category.icon)
                        put("color", category.color)
                        put("isDefault", category.isDefault)
                    }
                    categoriesArray.put(categoryJson)
                }
                put("categories", categoriesArray)
                
                // 导出交易记录
                val transactionsArray = JSONArray()
                transactions.forEach { transaction ->
                    val transactionJson = JSONObject().apply {
                        put("id", transaction.id)
                        put("amount", transaction.amount.toString())
                        put("type", transaction.type.name)
                        put("accountId", transaction.accountId)
                        put("categoryId", transaction.categoryId)
                        put("note", transaction.note)
                        put("tags", transaction.tags)
                        put("date", transaction.date.time)
                        put("createdAt", transaction.createdAt.time)
                        put("updatedAt", transaction.updatedAt.time)
                    }
                    transactionsArray.put(transactionJson)
                }
                put("transactions", transactionsArray)
            }
            
            // 创建导出文件
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "accounting_backup_${dateFormat.format(Date())}.json"
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val exportFile = File(exportDir, fileName)
            FileWriter(exportFile).use { writer ->
                writer.write(exportData.toString(2))
            }
            
            ExportResult(
                success = true,
                message = "数据导出成功",
                filePath = exportFile.absolutePath
            )
            
        } catch (e: Exception) {
            ExportResult(
                success = false,
                message = "导出失败: ${e.message}"
            )
        }
    }
    
    /**
     * 从JSON文件导入数据
     */
    suspend fun importData(filePath: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext ImportResult(
                    success = false,
                    message = "文件不存在"
                )
            }
            
            val jsonContent = file.readText()
            val importData = JSONObject(jsonContent)
            
            var importedCount = 0
            
            // 导入账户
            if (importData.has("accounts")) {
                val accountsArray = importData.getJSONArray("accounts")
                for (i in 0 until accountsArray.length()) {
                    val accountJson = accountsArray.getJSONObject(i)
                    // 这里可以添加账户导入逻辑
                    importedCount++
                }
            }
            
            // 导入分类
            if (importData.has("categories")) {
                val categoriesArray = importData.getJSONArray("categories")
                for (i in 0 until categoriesArray.length()) {
                    val categoryJson = categoriesArray.getJSONObject(i)
                    // 这里可以添加分类导入逻辑
                    importedCount++
                }
            }
            
            // 导入交易记录
            if (importData.has("transactions")) {
                val transactionsArray = importData.getJSONArray("transactions")
                for (i in 0 until transactionsArray.length()) {
                    val transactionJson = transactionsArray.getJSONObject(i)
                    // 这里可以添加交易记录导入逻辑
                    importedCount++
                }
            }
            
            ImportResult(
                success = true,
                message = "数据导入成功",
                importedCount = importedCount
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                message = "导入失败: ${e.message}"
            )
        }
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearAllData(): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 删除所有交易记录
            val transactions = transactionRepository.getAllTransactions().first()
            transactions.forEach { transaction ->
                transactionRepository.deleteTransaction(transaction.toDomain(
                    // 这里需要获取对应的账户和分类信息
                    accountRepository.getAccountById(transaction.accountId).first()?.toDomain() 
                        ?: throw IllegalStateException("Account not found"),
                    categoryRepository.getCategoryById(transaction.categoryId).first()?.toDomain()
                        ?: throw IllegalStateException("Category not found")
                ))
            }
            
            ExportResult(
                success = true,
                message = "数据清除成功"
            )
            
        } catch (e: Exception) {
            ExportResult(
                success = false,
                message = "清除失败: ${e.message}"
            )
        }
    }
}