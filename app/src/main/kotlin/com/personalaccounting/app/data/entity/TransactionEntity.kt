package com.personalaccounting.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.math.BigDecimal
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 交易金额
    val amount: BigDecimal,
    
    // 交易类型：0-支出，1-收入
    val type: Int,
    
    // 关联账户ID
    val accountId: Long,
    
    // 关联分类ID
    val categoryId: Long,
    
    // 交易日期
    val date: Date,
    
    // 备注
    val note: String = "",
    
    // 标签（用逗号分隔）
    val tags: String = "",
    
    // 创建时间
    val createdAt: Date = Date(),
    
    // 更新时间
    val updatedAt: Date = Date(),
    
    // 是否已删除（软删除）
    val isDeleted: Boolean = false
) {
    companion object {
        const val TYPE_EXPENSE = 0  // 支出
        const val TYPE_INCOME = 1   // 收入
    }
}