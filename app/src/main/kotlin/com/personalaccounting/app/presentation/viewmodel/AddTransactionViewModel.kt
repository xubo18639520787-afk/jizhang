package com.personalaccounting.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalaccounting.app.data.repository.AccountRepository
import com.personalaccounting.app.data.repository.CategoryRepository
import com.personalaccounting.app.data.repository.TransactionRepository
import com.personalaccounting.app.data.helper.SmartInputHelper
import com.personalaccounting.app.domain.mapper.toDomain
import com.personalaccounting.app.domain.mapper.toEntity
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
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val smartInputHelper: SmartInputHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                combine(
                    accountRepository.getAllAccounts(),
                    categoryRepository.getAllCategories()
                ) { accounts, categories ->
                    val accountList = accounts.map { it.toDomain() }
                    val categoryList = categories.map { it.toDomain() }
                    
                    _uiState.value = _uiState.value.copy(
                        accounts = accountList,
                        categories = categoryList,
                        selectedAccount = accountList.firstOrNull { it.isDefault } ?: accountList.firstOrNull(),
                        isLoading = false
                    )
                }.collect { /* Data updated in combine block */ }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载数据失败"
                )
            }
        }
    }

    fun updateAmount(amount: String) {
        try {
            val bigDecimalAmount = if (amount.isBlank()) BigDecimal.ZERO else try { BigDecimal(amount) } catch (e: NumberFormatException) { BigDecimal.ZERO }
            _uiState.value = _uiState.value.copy(amount = bigDecimalAmount, amountError = null)
        } catch (e: NumberFormatException) {
            _uiState.value = _uiState.value.copy(amountError = "请输入有效的金额")
        }
    }

    fun updateTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            transactionType = type,
            selectedCategory = null // 重置分类选择
        )
    }

    fun updateSelectedAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
    }

    fun updateSelectedCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateDate(date: Date) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun updateTags(tags: List<String>) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    fun saveTransaction() {
        val state = _uiState.value
        
        // 验证输入
        if (state.amount <= BigDecimal.ZERO) {
            _uiState.value = state.copy(amountError = "金额必须大于0")
            return
        }
        
        if (state.selectedAccount == null) {
            _uiState.value = state.copy(error = "请选择账户")
            return
        }
        
        if (state.selectedCategory == null) {
            _uiState.value = state.copy(error = "请选择分类")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true)
                
                val transaction = Transaction(
                    amount = state.amount,
                    type = state.transactionType,
                    account = state.selectedAccount,
                    category = state.selectedCategory,
                    date = state.date,
                    note = state.note,
                    tags = state.tags
                )
                
                transactionRepository.insertTransaction(transaction.toEntity())
                
                _uiState.value = state.copy(
                    isSaving = false,
                    isSaved = true
                )
                
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            amount = BigDecimal.ZERO,
            transactionType = TransactionType.EXPENSE,
            selectedCategory = null,
            note = "",
            date = Date(),
            tags = emptyList(),
            isSaved = false,
            amountError = null,
            error = null
        )
    }

    // 智能输入功能
    fun startOcrCapture() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                // TODO: 实现相机拍照和OCR识别
                // 这里需要与Activity交互来启动相机
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "OCR功能需要相机权限，请在设置中开启"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "OCR识别失败: ${e.message}"
                )
            }
        }
    }

    fun startVoiceInput() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                // TODO: 实现语音录制和识别
                // 这里需要与Activity交互来启动录音
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "语音输入功能需要麦克风权限，请在设置中开启"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "语音识别失败: ${e.message}"
                )
            }
        }
    }

    fun processOcrResult(imageBase64: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = smartInputHelper.processOcrResult(imageBase64)
                
                if (result.success) {
                    // 应用OCR识别结果
                    result.amount?.let { updateAmount(it.toString()) }
                    result.note?.let { updateNote(it) }
                    
                    // 根据建议的分类自动选择
                    result.suggestedCategory?.let { suggestedCat ->
                        val matchingCategory = _uiState.value.categories.find { 
                            it.name == suggestedCat && it.type == _uiState.value.transactionType 
                        }
                        matchingCategory?.let { updateSelectedCategory(it) }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "处理OCR结果失败: ${e.message}"
                )
            }
        }
    }

    fun processVoiceResult(audioData: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = smartInputHelper.processSpeechResult(audioData)
                
                if (result.success) {
                    // 应用语音识别结果
                    result.amount?.let { updateAmount(it.toString()) }
                    result.note?.let { updateNote(it) }
                    
                    // 根据建议的分类自动选择
                    result.suggestedCategory?.let { suggestedCat ->
                        val matchingCategory = _uiState.value.categories.find { 
                            it.name == suggestedCat && it.type == _uiState.value.transactionType 
                        }
                        matchingCategory?.let { updateSelectedCategory(it) }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "处理语音结果失败: ${e.message}"
                )
            }
        }
    }
}

data class AddTransactionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val amount: BigDecimal = BigDecimal.ZERO,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedAccount: Account? = null,
    val selectedCategory: Category? = null,
    val note: String = "",
    val date: Date = Date(),
    val tags: List<String> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val amountError: String? = null,
    val error: String? = null
) {
    val filteredCategories: List<Category>
        get() = categories.filter { it.type == transactionType }
}