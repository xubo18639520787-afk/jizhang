package com.personalaccounting.app.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.personalaccounting.app.data.entity.AccountEntity
import com.personalaccounting.app.data.entity.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

class DatabaseCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // 插入默认账户
        db.execSQL("""
            INSERT INTO accounts (name, type, balance, icon, color, isDefault, createdAt, updatedAt, isDeleted) 
            VALUES 
            ('现金', 0, 0, 'ic_cash', '#00C896', 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('银行卡', 1, 0, 'ic_bank_card', '#2196F3', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('支付宝', 2, 0, 'ic_alipay', '#1677FF', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('微信', 3, 0, 'ic_wechat', '#07C160', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
        """)
        
        // 插入默认支出分类
        db.execSQL("""
            INSERT INTO categories (name, type, parentId, icon, color, sortOrder, isSystem, createdAt, updatedAt, isDeleted) 
            VALUES 
            ('餐饮', 0, 0, 'ic_food', '#FF6B35', 1, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('交通', 0, 0, 'ic_transport', '#9C27B0', 2, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('购物', 0, 0, 'ic_shopping', '#E91E63', 3, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('娱乐', 0, 0, 'ic_entertainment', '#FF9800', 4, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('医疗', 0, 0, 'ic_medical', '#F44336', 5, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('教育', 0, 0, 'ic_education', '#3F51B5', 6, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('住房', 0, 0, 'ic_housing', '#795548', 7, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('其他', 0, 0, 'ic_other', '#607D8B', 8, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
        """)
        
        // 插入默认收入分类
        db.execSQL("""
            INSERT INTO categories (name, type, parentId, icon, color, sortOrder, isSystem, createdAt, updatedAt, isDeleted) 
            VALUES 
            ('工资', 1, 0, 'ic_salary', '#4CAF50', 1, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('奖金', 1, 0, 'ic_bonus', '#8BC34A', 2, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('投资', 1, 0, 'ic_investment', '#CDDC39', 3, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('兼职', 1, 0, 'ic_part_time', '#FFC107', 4, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0),
            ('其他', 1, 0, 'ic_other', '#607D8B', 5, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
        """)
    }
}