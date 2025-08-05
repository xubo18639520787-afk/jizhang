package com.personalaccounting.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.math.BigDecimal
import java.util.Date

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["type"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 账户名称
    val name: String,
    
    // 账户类型：0-现金，1-银行卡，2-支付宝，3-微信，4-信用卡
    val type: Int,
    
    // 账户余额
    val balance: BigDecimal = BigDecimal.ZERO,
    
    // 账户图标（资源名称）
    val icon: String = "",
    
    // 账户颜色（十六进制）
    val color: String = "#00C896",
    
    // 是否默认账户
    val isDefault: Boolean = false,
    
    // 创建时间
    val createdAt: Date = Date(),
    
    // 更新时间
    val updatedAt: Date = Date(),
    
    // 是否已删除（软删除）
    val isDeleted: Boolean = false
) {
    companion object {
        const val TYPE_CASH = 0         // 现金
        const val TYPE_BANK_CARD = 1    // 银行卡
        const val TYPE_ALIPAY = 2       // 支付宝
        const val TYPE_WECHAT = 3       // 微信
        const val TYPE_CREDIT_CARD = 4  // 信用卡
    }
}