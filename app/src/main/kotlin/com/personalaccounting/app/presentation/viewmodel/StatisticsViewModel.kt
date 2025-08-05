package com.personalaccounting.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.data.repository.TransactionRepository
import com.personalaccounting.app.domain.mapper.toDomain
import com.personalaccounting.app.domain.model.Category
import com.personalaccounting.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 获取当月数据
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

                combine(
                    transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth),
                    categoryRepository.getAllCategories()
                ) { transactions, categories ->
                    
                    val categoryMap = categories.associate { it.id to it.toDomain() }
                    
                    // 按分类统计
                    val categoryStats = mutableMapOf<Category, BigDecimal>()
                    var totalIncome = BigDecimal.ZERO
                    var totalExpense = BigDecimal.ZERO
                    
                    transactions.forEach { transaction ->
                        val category = categoryMap[transaction.categoryId]
                        if (category != null) {
                            categoryStats[category] = categoryStats.getOrDefault(category, BigDecimal.ZERO)
                                .add(transaction.amount)
                            
                            when (category.type) {
                                TransactionType.INCOME -> totalIncome = totalIncome.add(transaction.amount)
                                TransactionType.EXPENSE -> totalExpense = totalExpense.add(transaction.amount)
                            }
                        }
                    }
                    
                    // 生成分类统计列表
                    val categoryStatsList = categoryStats.map { (category, amount) ->
                        CategoryStatistic(
                            category = category,
                            amount = amount,
                            percentage = if (category.type == TransactionType.INCOME && totalIncome > BigDecimal.ZERO) {
                                amount.divide(totalIncome, 4, BigDecimal.ROUND_HALF_UP)
                                    .multiply(BigDecimal(100))
                            } else if (category.type == TransactionType.EXPENSE && totalExpense > BigDecimal.ZERO) {
                                amount.divide(totalExpense, 4, BigDecimal.ROUND_HALF_UP)
                                    .multiply(BigDecimal(100))
                            } else {
                                BigDecimal.ZERO
                            }
                        )
                    }.sortedByDescending { it.amount }
                    
                    StatisticsUiState(
                        isLoading = false,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        categoryStatistics = categoryStatsList
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载统计数据失败"
                )
            }
        }
    }

    fun refreshData() {
        loadStatistics()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val categoryStatistics: List<CategoryStatistic> = emptyList(),
    val error: String? = null
)

data class CategoryStatistic(
    val category: Category,
    val amount: BigDecimal,
    val percentage: BigDecimal
)