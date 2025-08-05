package com.personalaccounting.app.domain.model

import java.math.BigDecimal
import java.util.Date

data class Transaction(
    val id: Long = 0,
    val amount: BigDecimal,
    val type: TransactionType,
    val account: Account,
    val category: Category,
    val date: Date,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class TransactionType(val value: Int, val displayName: String) {
    EXPENSE(0, "支出"),
    INCOME(1, "收入");
    
    companion object {
        fun fromValue(value: Int): TransactionType {
            return values().find { it.value == value } ?: EXPENSE
        }
    }
}