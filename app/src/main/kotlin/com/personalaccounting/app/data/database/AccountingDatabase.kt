package com.personalaccounting.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.personalaccounting.app.data.converter.Converters
import com.personalaccounting.app.data.dao.AccountDao
import com.personalaccounting.app.data.dao.CategoryDao
import com.personalaccounting.app.data.dao.TransactionDao
import com.personalaccounting.app.data.entity.AccountEntity
import com.personalaccounting.app.data.entity.CategoryEntity
import com.personalaccounting.app.data.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AccountingDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        const val DATABASE_NAME = "accounting_database"
        
        @Volatile
        private var INSTANCE: AccountingDatabase? = null
        
        fun getDatabase(context: Context): AccountingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccountingDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}