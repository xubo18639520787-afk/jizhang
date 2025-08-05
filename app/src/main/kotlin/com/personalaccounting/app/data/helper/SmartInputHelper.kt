package com.personalaccounting.app.data.helper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能输入助手
 * 提供OCR识别和语音输入的文本解析功能
 */
@Singleton
class SmartInputHelper @Inject constructor() {
    
    /**
     * 智能输入结果
     */
    data class SmartInputResult(
        val amount: BigDecimal? = null,
        val merchant: String? = null,
        val suggestedCategory: String? = null,
        val note: String? = null,
        val confidence: Float = 0.0f
    )
    
    /**
     * 从OCR识别的文本行中提取交易信息
     */
    suspend fun extractTransactionInfo(textLines: List<String>): SmartInputResult = withContext(Dispatchers.IO) {
        var amount: BigDecimal? = null
        var merchant: String? = null
        var suggestedCategory: String? = null
        var note: String? = null
        
        // 模拟OCR处理延迟
        delay(500)
        
        val fullText = textLines.joinToString(" ")
        
        // 提取金额
        amount = extractAmount(fullText)
        
        // 提取商家信息
        merchant = extractMerchant(textLines)
        
        // 根据关键词推测分类
        suggestedCategory = suggestCategory(fullText)
        
        // 生成备注
        note = generateNote(textLines)
        
        SmartInputResult(
            amount = amount,
            merchant = merchant,
            suggestedCategory = suggestedCategory,
            note = note,
            confidence = calculateConfidence(amount, merchant, suggestedCategory)
        )
    }
    
    /**
     * 解析语音输入文本
     */
    suspend fun parseVoiceInput(voiceText: String): SmartInputResult = withContext(Dispatchers.IO) {
        // 模拟语音处理延迟
        delay(300)
        
        var amount: BigDecimal? = null
        var merchant: String? = null
        var suggestedCategory: String? = null
        
        // 从语音文本中提取金额
        amount = extractAmountFromVoice(voiceText)
        
        // 提取商家信息
        merchant = extractMerchantFromVoice(voiceText)
        
        // 推测分类
        suggestedCategory = suggestCategory(voiceText)
        
        SmartInputResult(
            amount = amount,
            merchant = merchant,
            suggestedCategory = suggestedCategory,
            note = voiceText,
            confidence = calculateConfidence(amount, merchant, suggestedCategory)
        )
    }
    
    /**
     * 从文本中提取金额
     */
    private fun extractAmount(text: String): BigDecimal? {
        val patterns = listOf(
            "¥([0-9]+\\.?[0-9]*)",
            "([0-9]+\\.?[0-9]*)元",
            "金额[：:]\\s*([0-9]+\\.?[0-9]*)",
            "总计[：:]\\s*¥?([0-9]+\\.?[0-9]*)",
            "合计[：:]\\s*¥?([0-9]+\\.?[0-9]*)",
            "应付[：:]\\s*¥?([0-9]+\\.?[0-9]*)",
            "票价[：:]\\s*([0-9]+\\.?[0-9]*)元?"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern).matcher(text)
            if (matcher.find()) {
                try {
                    val amountStr = matcher.group(1) ?: continue
                    return try { BigDecimal(amountStr) } catch (e: NumberFormatException) { BigDecimal.ZERO }
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        }
        return null
    }
    
    /**
     * 从语音文本中提取金额
     */
    private fun extractAmountFromVoice(voiceText: String): BigDecimal? {
        try {
            // 匹配中文数字表达
            val chineseNumberPattern = "([一二三四五六七八九十百千万]+)块([一二三四五六七八九十]*)毛?"
            val matcher = Pattern.compile(chineseNumberPattern).matcher(voiceText)
            
            if (matcher.find()) {
                val yuan = convertChineseToNumber(matcher.group(1) ?: "")
                val jiao = if (matcher.group(2)?.isNotEmpty() == true) {
                    convertChineseToNumber(matcher.group(2) ?: "") * 0.1
                } else 0.0
                
                val totalAmount = yuan + jiao
                return try { BigDecimal(totalAmount.toString()) } catch (e: NumberFormatException) { BigDecimal.ZERO }
            }
            
            // 匹配阿拉伯数字
            val numberPattern = "([0-9]+\\.?[0-9]*)块钱?|([0-9]+\\.?[0-9]*)元"
            val numberMatcher = Pattern.compile(numberPattern).matcher(voiceText)
            if (numberMatcher.find()) {
                val amountMatch = numberMatcher.group(1) ?: numberMatcher.group(2)
                if (amountMatch != null) {
                    return try { BigDecimal(amountMatch) } catch (e: NumberFormatException) { BigDecimal.ZERO }
                }
            }
        } catch (e: Exception) {
            // 处理解析异常
        }
        
        return null
    }
    
    /**
     * 转换中文数字为阿拉伯数字
     */
    private fun convertChineseToNumber(chineseNumber: String): Double {
        val numberMap = mapOf(
            "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5,
            "六" to 6, "七" to 7, "八" to 8, "九" to 9, "十" to 10,
            "百" to 100, "千" to 1000, "万" to 10000
        )
        
        var result = 0.0
        var temp = 0.0
        var unit = 1.0
        
        for (char in chineseNumber.reversed()) {
            val charStr = char.toString()
            when {
                numberMap.containsKey(charStr) -> {
                    val value = numberMap[charStr]!!
                    if (value >= 10) {
                        if (value > unit) {
                            result += temp * unit
                            temp = 0.0
                            unit = value.toDouble()
                        } else {
                            unit = value.toDouble()
                        }
                    } else {
                        temp += value * unit
                    }
                }
            }
        }
        result += temp
        
        return result
    }
    
    /**
     * 从文本行中提取商家信息
     */
    private fun extractMerchant(textLines: List<String>): String? {
        val merchantKeywords = listOf(
            "超市", "商店", "店", "餐厅", "饭店", "火锅", "咖啡", "茶", "药店", "医院",
            "加油站", "影城", "电影院", "KTV", "健身", "美容", "理发", "洗车", "停车"
        )
        
        for (line in textLines) {
            for (keyword in merchantKeywords) {
                if (line.contains(keyword)) {
                    // 提取包含关键词的完整商家名称
                    val words = line.split("\\s+".toRegex())
                    for (word in words) {
                        if (word.contains(keyword) && word.length > keyword.length) {
                            return word
                        }
                    }
                    return line.trim()
                }
            }
        }
        
        return null
    }
    
    /**
     * 从语音文本中提取商家信息
     */
    private fun extractMerchantFromVoice(voiceText: String): String? {
        val merchantPatterns = listOf(
            "在(.+?)买", "在(.+?)花", "去(.+?)消费", "(.+?)店", "(.+?)超市", "(.+?)餐厅"
        )
        
        for (pattern in merchantPatterns) {
            val matcher = Pattern.compile(pattern).matcher(voiceText)
            if (matcher.find()) {
                return matcher.group(1)?.trim()
            }
        }
        
        return null
    }
    
    /**
     * 根据关键词推测分类
     */
    private fun suggestCategory(text: String): String? {
        val categoryKeywords = mapOf(
            "餐饮" to listOf("餐厅", "饭店", "火锅", "咖啡", "茶", "外卖", "吃", "喝"),
            "购物" to listOf("超市", "商店", "购物", "买"),
            "交通" to listOf("加油", "停车", "地铁", "公交", "出租", "滴滴", "油费"),
            "娱乐" to listOf("电影", "影城", "KTV", "游戏", "娱乐"),
            "医疗" to listOf("医院", "药店", "看病", "买药", "体检"),
            "日用品" to listOf("日用", "生活用品", "洗漱", "清洁"),
            "服装" to listOf("衣服", "鞋子", "包", "服装"),
            "美容" to listOf("美容", "理发", "化妆品", "护肤"),
            "健身" to listOf("健身", "运动", "游泳", "瑜伽"),
            "教育" to listOf("学费", "培训", "书籍", "教育")
        )
        
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (text.contains(keyword, ignoreCase = true)) {
                    return category
                }
            }
        }
        
        return null
    }
    
    /**
     * 生成备注信息
     */
    private fun generateNote(textLines: List<String>): String? {
        // 过滤掉金额和常见的无用信息
        val filteredLines = textLines.filter { line ->
            !line.matches(".*[0-9]+\\.?[0-9]*.*".toRegex()) && 
            line.length > 2 && 
            !line.contains("合计") && 
            !line.contains("总计") && 
            !line.contains("应付")
        }
        
        return if (filteredLines.isNotEmpty()) {
            filteredLines.joinToString(" ").take(50)
        } else null
    }
    
    /**
     * 计算识别置信度
     */
    private fun calculateConfidence(amount: BigDecimal?, merchant: String?, category: String?): Float {
        var confidence = 0.0f
        
        if (amount != null && amount > BigDecimal.ZERO) confidence += 0.4f
        if (merchant != null && merchant.isNotBlank()) confidence += 0.3f
        if (category != null && category.isNotBlank()) confidence += 0.3f
        
        return confidence
    }
}