package com.personalaccounting.app.data.repository

import com.personalaccounting.app.data.dao.CategoryDao
import com.personalaccounting.app.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }
    
    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }
    
    fun getCategoriesByType(type: Int): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }
    
    fun getCategoriesByParent(parentId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByParent(parentId)
    }
    
    fun getTopLevelCategoriesByType(type: Int): Flow<List<CategoryEntity>> {
        return categoryDao.getTopLevelCategoriesByType(type)
    }
    
    fun searchCategories(keyword: String): Flow<List<CategoryEntity>> {
        return categoryDao.searchCategories(keyword)
    }
    
    fun getSystemCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getSystemCategories()
    }
    
    fun getUserCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getUserCategories()
    }
    
    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }
    
    suspend fun insertCategories(categories: List<CategoryEntity>) {
        categoryDao.insertCategories(categories)
    }
    
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category.copy(updatedAt = Date()))
    }
    
    suspend fun updateCategorySortOrder(id: Long, sortOrder: Int) {
        categoryDao.updateCategorySortOrder(id, sortOrder)
    }
    
    suspend fun deleteCategory(id: Long) {
        categoryDao.softDeleteCategory(id)
    }
    
    suspend fun deleteCategoriesByParent(parentId: Long) {
        categoryDao.softDeleteCategoriesByParent(parentId)
    }
    
    suspend fun permanentDeleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }
    
    suspend fun getSubCategoryCount(parentId: Long): Int {
        return categoryDao.getSubCategoryCount(parentId)
    }
    
    // 获取支出分类
    fun getExpenseCategories(): Flow<List<CategoryEntity>> {
        return getCategoriesByType(CategoryEntity.TYPE_EXPENSE)
    }
    
    // 获取收入分类
    fun getIncomeCategories(): Flow<List<CategoryEntity>> {
        return getCategoriesByType(CategoryEntity.TYPE_INCOME)
    }
    
    // 获取顶级支出分类
    fun getTopLevelExpenseCategories(): Flow<List<CategoryEntity>> {
        return getTopLevelCategoriesByType(CategoryEntity.TYPE_EXPENSE)
    }
    
    // 获取顶级收入分类
    fun getTopLevelIncomeCategories(): Flow<List<CategoryEntity>> {
        return getTopLevelCategoriesByType(CategoryEntity.TYPE_INCOME)
    }
}