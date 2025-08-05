package com.personalaccounting.app.domain.model

import java.math.BigDecimal
import java.util.Date

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: BigDecimal = BigDecimal.ZERO,
    val color: String = "#00C896",
    val icon: String = "account_balance_wallet",
    val isDefault: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class AccountType(val value: Int, val displayName: String) {
    CASH(0, "现金"),
    BANK_CARD(1, "银行卡"),
    CREDIT_CARD(2, "信用卡"),
    ALIPAY(3, "支付宝"),
    WECHAT(4, "微信"),
    OTHER(5, "其他");
    
    companion object {
        fun fromValue(value: Int): AccountType {
            return values().find { it.value == value } ?: CASH
        }
    }
}