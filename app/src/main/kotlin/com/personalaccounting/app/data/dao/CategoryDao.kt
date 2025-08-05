package com.personalaccounting.app.data.dao

import androidx.room.*
import com.personalaccounting.app.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY type ASC, sortOrder ASC, createdAt ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :id AND isDeleted = 0")
    suspend fun getCategoryById(id: Long): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE type = :type AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getCategoriesByType(type: Int): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getCategoriesByParent(parentId: Long): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE parentId = 0 AND type = :type AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getTopLevelCategoriesByType(type: Int): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :keyword || '%' AND isDeleted = 0 ORDER BY type ASC, sortOrder ASC")
    fun searchCategories(keyword: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE isSystem = 1 AND isDeleted = 0 ORDER BY type ASC, sortOrder ASC")
    fun getSystemCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE isSystem = 0 AND isDeleted = 0 ORDER BY type ASC, sortOrder ASC")
    fun getUserCategories(): Flow<List<CategoryEntity>>
    
    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long
    
    @Insert
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Query("UPDATE categories SET sortOrder = :sortOrder, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCategorySortOrder(id: Long, sortOrder: Int, updatedAt: Date = Date())
    
    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteCategory(id: Long, deletedAt: Date = Date())
    
    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :deletedAt WHERE parentId = :parentId")
    suspend fun softDeleteCategoriesByParent(parentId: Long, deletedAt: Date = Date())
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE isDeleted = 1")
    suspend fun deleteAllSoftDeletedCategories()
    
    @Query("SELECT COUNT(*) FROM categories WHERE isDeleted = 0")
    suspend fun getCategoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM categories WHERE parentId = :parentId AND isDeleted = 0")
    suspend fun getSubCategoryCount(parentId: Long): Int
}