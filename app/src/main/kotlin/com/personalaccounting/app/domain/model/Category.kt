package com.personalaccounting.app.domain.model

import java.util.Date

data class Category(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val color: String = "#FF6B35",
    val icon: String = "category",
    val parentId: Long? = null,
    val isDefault: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)