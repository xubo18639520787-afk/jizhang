package com.personalaccounting.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "type"], unique = true),
        Index(value = ["type"]),
        Index(value = ["parentId"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 分类名称
    val name: String,
    
    // 分类类型：0-支出，1-收入
    val type: Int,
    
    // 父分类ID（用于二级分类，0表示一级分类）
    val parentId: Long = 0,
    
    // 分类图标（资源名称）
    val icon: String = "",
    
    // 分类颜色（十六进制）
    val color: String = "#FF6B35",
    
    // 排序权重
    val sortOrder: Int = 0,
    
    // 是否系统预设分类
    val isSystem: Boolean = false,
    
    // 创建时间
    val createdAt: Date = Date(),
    
    // 更新时间
    val updatedAt: Date = Date(),
    
    // 是否已删除（软删除）
    val isDeleted: Boolean = false
) {
    companion object {
        const val TYPE_EXPENSE = 0  // 支出分类
        const val TYPE_INCOME = 1   // 收入分类
    }
}