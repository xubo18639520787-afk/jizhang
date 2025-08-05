package com.personalaccounting.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalaccounting.app.data.repository.AccountRepository
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.data.repository.TransactionRepository
import com.personalaccounting.app.domain.mapper.toDomain
import com.personalaccounting.app.domain.model.Account
import com.personalaccounting.app.domain.model.Category
import com.personalaccounting.app.domain.model.Transaction
import com.personalaccounting.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 获取当月收支统计
                val calendar = Calendar.getInstance()
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time

                // 获取数据流
                combine(
                    transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth),
                    transactionRepository.getRecentTransactions(10),
                    accountRepository.getAllAccounts(),
                    categoryRepository.getAllCategories()
                ) { monthlyTransactions, recentTransactions, accounts, categories ->
                    
                    val accountMap = accounts.associate { it.id to it.toDomain() }
                    val categoryMap = categories.associate { it.id to it.toDomain() }
                    
                    // 转换最近交易
                    val recentTransactionsList = recentTransactions.mapNotNull { transaction ->
                        val account = accountMap[transaction.accountId]
                        val category = categoryMap[transaction.categoryId]
                        if (account != null && category != null) {
                            transaction.toDomain(account, category)
                        } else null
                    }
                    
                    // 计算月度统计
                    var monthlyIncome = BigDecimal.ZERO
                    var monthlyExpense = BigDecimal.ZERO
                    
                    monthlyTransactions.forEach { transaction ->
                        val account = accountMap[transaction.accountId]
                        val category = categoryMap[transaction.categoryId]
                        if (account != null && category != null) {
                            val domainTransaction = transaction.toDomain(account, category)
                            when (domainTransaction.type) {
                                TransactionType.INCOME -> monthlyIncome = monthlyIncome.add(domainTransaction.amount)
                                TransactionType.EXPENSE -> monthlyExpense = monthlyExpense.add(domainTransaction.amount)
                            }
                        }
                    }
                    
                    HomeUiState(
                        isLoading = false,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        monthlyBalance = monthlyIncome.subtract(monthlyExpense),
                        recentTransactions = recentTransactionsList,
                        accounts = accountMap.values.toList(),
                        categories = categoryMap.values.toList()
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载数据失败"
                )
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val monthlyIncome: BigDecimal = BigDecimal.ZERO,
    val monthlyExpense: BigDecimal = BigDecimal.ZERO,
    val monthlyBalance: BigDecimal = BigDecimal.ZERO,
    val recentTransactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null
)