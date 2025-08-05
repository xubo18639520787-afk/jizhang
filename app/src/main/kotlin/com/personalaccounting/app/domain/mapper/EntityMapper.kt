package com.personalaccounting.app.domain.mapper

import com.personalaccounting.app.data.entity.AccountEntity
import com.personalaccounting.app.data.entity.CategoryEntity
import com.personalaccounting.app.data.entity.TransactionEntity
import com.personalaccounting.app.domain.model.Account
import com.personalaccounting.app.domain.model.AccountType
import com.personalaccounting.app.domain.model.Category
import com.personalaccounting.app.domain.model.Transaction
import com.personalaccounting.app.domain.model.TransactionType

// Transaction映射
fun TransactionEntity.toDomain(account: Account, category: Category): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = TransactionType.fromValue(type),
        account = account,
        category = category,
        date = date,
        note = note,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type.value,
        accountId = account.id,
        categoryId = category.id,
        date = date,
        note = note,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Account映射
fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.fromValue(type),
        balance = balance,
        color = color,
        icon = icon,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type.value,
        balance = balance,
        color = color,
        icon = icon,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Category映射
fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = TransactionType.fromValue(type),
        color = color,
        icon = icon,
        parentId = parentId,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.value,
        color = color,
        icon = icon,
        parentId = parentId,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}