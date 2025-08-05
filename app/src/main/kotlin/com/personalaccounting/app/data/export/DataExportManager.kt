package com.personalaccounting.app.data.export

import android.content.Context
import android.net.Uri
import com.personalaccounting.app.data.repository.TransactionRepository
import com.personalaccounting.app.data.repository.AccountRepository
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.domain.model.Transaction
import com.personalaccounting.app.domain.model.Account
import com.personalaccounting.app.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据导入导出管理器
 */
@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * 导出数据到JSON文件
     */
    suspend fun exportToJson(uri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getAllTransactions().first()
            val accounts = accountRepository.getAllAccounts().first()
            val categories = categoryRepository.getAllCategories().first()
            
            val exportData = JSONObject().apply {
                put("exportTime", dateFormat.format(Date()))
                put("version", "1.0")
                put("transactions", transactionsToJson(transactions))
                put("accounts", accountsToJson(accounts))
                put("categories", categoriesToJson(categories))
            }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    writer.write(exportData.toString(2))
                }
            }
            
            ExportResult(true, "数据导出成功", transactions.size)
        } catch (e: Exception) {
            ExportResult(false, "导出失败: ${e.message}", 0)
        }
    }
    
    /**
     * 导出数据到CSV文件
     */
    suspend fun exportToCsv(uri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getAllTransactions().first()
            val accounts = accountRepository.getAllAccounts().first()
            val categories = categoryRepository.getAllCategories().first()
            
            // 创建账户和分类的映射
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    // 写入CSV头部
                    writer.write("日期,类型,金额,账户,分类,备注\n")
                    
                    // 写入交易数据
                    transactions.forEach { transaction ->
                        val account = accountMap[transaction.accountId]?.name ?: "未知账户"
                        val category = categoryMap[transaction.categoryId]?.name ?: "未知分类"
                        val type = if (transaction.isIncome) "收入" else "支出"
                        
                        writer.write("${dateFormat.format(transaction.date)},$type,${transaction.amount},$account,$category,\"${transaction.note}\"\n")
                    }
                }
            }
            
            ExportResult(true, "CSV导出成功", transactions.size)
        } catch (e: Exception) {
            ExportResult(false, "CSV导出失败: ${e.message}", 0)
        }
    }
    
    /**
     * 从JSON文件导入数据
     */
    suspend fun importFromJson(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext ImportResult(false, "无法读取文件", 0)
            
            val jsonObject = JSONObject(jsonContent)
            
            // 验证数据格式
            if (!jsonObject.has("transactions") || !jsonObject.has("accounts") || !jsonObject.has("categories")) {
                return@withContext ImportResult(false, "数据格式不正确", 0)
            }
            
            var importedCount = 0
            
            // 导入分类
            val categoriesArray = jsonObject.getJSONArray("categories")
            for (i in 0 until categoriesArray.length()) {
                val categoryJson = categoriesArray.getJSONObject(i)
                val category = jsonToCategory(categoryJson)
                categoryRepository.insertCategory(category)
            }
            
            // 导入账户
            val accountsArray = jsonObject.getJSONArray("accounts")
            for (i in 0 until accountsArray.length()) {
                val accountJson = accountsArray.getJSONObject(i)
                val account = jsonToAccount(accountJson)
                accountRepository.insertAccount(account)
            }
            
            // 导入交易记录
            val transactionsArray = jsonObject.getJSONArray("transactions")
            for (i in 0 until transactionsArray.length()) {
                val transactionJson = transactionsArray.getJSONObject(i)
                val transaction = jsonToTransaction(transactionJson)
                transactionRepository.insertTransaction(transaction)
                importedCount++
            }
            
            ImportResult(true, "数据导入成功", importedCount)
        } catch (e: Exception) {
            ImportResult(false, "导入失败: ${e.message}", 0)
        }
    }
    
    /**
     * 从CSV文件导入数据
     */
    suspend fun importFromCsv(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val csvContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readLines()
                }
            } ?: return@withContext ImportResult(false, "无法读取文件", 0)
            
            if (csvContent.isEmpty()) {
                return@withContext ImportResult(false, "文件为空", 0)
            }
            
            // 跳过标题行
            val dataLines = csvContent.drop(1)
            var importedCount = 0
            
            // 获取默认账户和分类
            val defaultAccount = accountRepository.getAllAccounts().first().firstOrNull()
                ?: return@withContext ImportResult(false, "请先创建至少一个账户", 0)
            
            val defaultCategory = categoryRepository.getAllCategories().first().firstOrNull()
                ?: return@withContext ImportResult(false, "请先创建至少一个分类", 0)
            
            for (line in dataLines) {
                if (line.isBlank()) continue
                
                try {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 6) {
                        val date = dateFormat.parse(parts[0]) ?: Date()
                        val isIncome = parts[1] == "收入"
                        val amount = parts[2].toBigDecimal()
                        val note = parts[5].replace("\"", "")
                        
                        val transaction = Transaction(
                            id = 0,
                            amount = amount,
                            isIncome = isIncome,
                            accountId = defaultAccount.id,
                            categoryId = defaultCategory.id,
                            note = note,
                            date = date
                        )
                        
                        transactionRepository.insertTransaction(transaction)
                        importedCount++
                    }
                } catch (e: Exception) {
                    // 跳过解析失败的行
                    continue
                }
            }
            
            ImportResult(true, "CSV导入成功", importedCount)
        } catch (e: Exception) {
            ImportResult(false, "CSV导入失败: ${e.message}", 0)
        }
    }
    
    // 辅助方法
    private fun transactionsToJson(transactions: List<Transaction>): JSONArray {
        val array = JSONArray()
        transactions.forEach { transaction ->
            array.put(JSONObject().apply {
                put("id", transaction.id)
                put("amount", transaction.amount.toString())
                put("isIncome", transaction.isIncome)
                put("accountId", transaction.accountId)
                put("categoryId", transaction.categoryId)
                put("note", transaction.note)
                put("date", dateFormat.format(transaction.date))
            })
        }
        return array
    }
    
    private fun accountsToJson(accounts: List<Account>): JSONArray {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(JSONObject().apply {
                put("id", account.id)
                put("name", account.name)
                put("type", account.type)
                put("balance", account.balance.toString())
                put("color", account.color)
                put("icon", account.icon)
            })
        }
        return array
    }
    
    private fun categoriesToJson(categories: List<Category>): JSONArray {
        val array = JSONArray()
        categories.forEach { category ->
            array.put(JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("type", category.type)
                put("color", category.color)
                put("icon", category.icon)
            })
        }
        return array
    }
    
    private fun jsonToTransaction(json: JSONObject): Transaction {
        return Transaction(
            id = 0, // 让数据库自动分配ID
            amount = json.getString("amount").toBigDecimal(),
            isIncome = json.getBoolean("isIncome"),
            accountId = json.getLong("accountId"),
            categoryId = json.getLong("categoryId"),
            note = json.getString("note"),
            date = dateFormat.parse(json.getString("date")) ?: Date()
        )
    }
    
    private fun jsonToAccount(json: JSONObject): Account {
        return Account(
            id = 0, // 让数据库自动分配ID
            name = json.getString("name"),
            type = json.getString("type"),
            balance = json.getString("balance").toBigDecimal(),
            color = json.getString("color"),
            icon = json.getString("icon")
        )
    }
    
    private fun jsonToCategory(json: JSONObject): Category {
        return Category(
            id = 0, // 让数据库自动分配ID
            name = json.getString("name"),
            type = json.getString("type"),
            color = json.getString("color"),
            icon = json.getString("icon")
        )
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        
        return result
    }
}

/**
 * 导出结果
 */
data class ExportResult(
    val success: Boolean,
    val message: String,
    val recordCount: Int
)

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val message: String,
    val recordCount: Int
)