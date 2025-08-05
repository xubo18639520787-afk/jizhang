package com.personalaccounting.app.data.helper

import com.personalaccounting.app.data.api.BaiduApiService
import com.personalaccounting.app.data.api.OcrResult
import com.personalaccounting.app.data.api.SpeechResult
import java.math.BigDecimal
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能输入助手
 * 处理OCR和语音识别结果，提取有用信息
 */
@Singleton
class SmartInputHelper @Inject constructor(
    private val baiduApiService: BaiduApiService
) {
    
    // 金额匹配正则表达式
    private val amountPatterns = listOf(
        Pattern.compile("([0-9]+\\.?[0-9]*)元"),
        Pattern.compile("¥([0-9]+\\.?[0-9]*)"),
        Pattern.compile("([0-9]+\\.?[0-9]*)"),
        Pattern.compile("金额[：:]?([0-9]+\\.?[0-9]*)"),
        Pattern.compile("总计[：:]?([0-9]+\\.?[0-9]*)")
    )
    
    // 商家名称匹配正则表达式
    private val merchantPatterns = listOf(
        Pattern.compile("商户[：:]?(.+)"),
        Pattern.compile("商家[：:]?(.+)"),
        Pattern.compile("收款方[：:]?(.+)"),
        Pattern.compile("付款给[：:]?(.+)")
    )
    
    // 时间匹配正则表达式
    private val timePatterns = listOf(
        Pattern.compile("(\\d{4}-\\d{2}-\\d{2})"),
        Pattern.compile("(\\d{4}/\\d{2}/\\d{2})"),
        Pattern.compile("(\\d{2}-\\d{2} \\d{2}:\\d{2})")
    )
    
    /**
     * 处理OCR识别结果
     */
    suspend fun processOcrResult(imageBase64: String): SmartInputResult {
        val ocrResult = baiduApiService.recognizeText(imageBase64)
        
        if (!ocrResult.success) {
            return SmartInputResult(
                success = false,
                message = ocrResult.message,
                amount = null,
                merchant = null,
                note = null,
                extractedText = emptyList()
            )
        }
        
        return extractTransactionInfo(ocrResult.textList)
    }
    
    /**
     * 处理语音识别结果
     */
    suspend fun processSpeechResult(audioData: String): SmartInputResult {
        val speechResult = baiduApiService.recognizeSpeech(audioData)
        
        if (!speechResult.success) {
            return SmartInputResult(
                success = false,
                message = speechResult.message,
                amount = null,
                merchant = null,
                note = speechResult.text,
                extractedText = emptyList()
            )
        }
        
        // 将语音识别结果作为单行文本处理
        return extractTransactionInfo(listOf(speechResult.text))
    }
    
    /**
     * 从文本列表中提取交易信息
     */
    private fun extractTransactionInfo(textList: List<String>): SmartInputResult {
        var amount: BigDecimal? = null
        var merchant: String? = null
        val notes = mutableListOf<String>()
        
        for (text in textList) {
            // 提取金额
            if (amount == null) {
                amount = extractAmount(text)
            }
            
            // 提取商家名称
            if (merchant == null) {
                merchant = extractMerchant(text)
            }
            
            // 收集所有文本作为备注
            if (text.isNotBlank()) {
                notes.add(text.trim())
            }
        }
        
        return SmartInputResult(
            success = true,
            message = "信息提取完成",
            amount = amount,
            merchant = merchant,
            note = notes.joinToString(" | "),
            extractedText = textList
        )
    }
    
    /**
     * 提取金额
     */
    private fun extractAmount(text: String): BigDecimal? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(text)
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
     * 提取商家名称
     */
    private fun extractMerchant(text: String): String? {
        for (pattern in merchantPatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                if (!merchant.isNullOrBlank()) {
                    return merchant
                }
            }
        }
        return null
    }
    
    /**
     * 智能解析语音输入
     * 例如："在超市花了50块钱买菜" -> 金额50，商家"超市"，备注"买菜"
     */
    fun parseVoiceInput(voiceText: String): SmartInputResult {
        var amount: BigDecimal? = null
        var merchant: String? = null
        var category: String? = null
        
        // 解析金额
        val amountRegex = Regex("([0-9]+\\.?[0-9]*)[块元钱]")
        val amountMatch = amountRegex.find(voiceText)
        if (amountMatch != null) {
            try {
                amount = try { BigDecimal(amountMatch.groupValues[1]) } catch (e: NumberFormatException) { BigDecimal.ZERO }
            } catch (e: NumberFormatException) {
                // 忽略解析错误
            }
        }
        
        // 解析地点/商家
        val merchantRegex = Regex("在(.{1,10}?)[花费用买]")
        val merchantMatch = merchantRegex.find(voiceText)
        if (merchantMatch != null) {
            merchant = merchantMatch.groupValues[1].trim()
        }
        
        // 解析分类
        val categoryKeywords = mapOf(
            "吃饭" to "餐饮",
            "买菜" to "食材",
            "购物" to "购物",
            "打车" to "交通",
            "加油" to "交通",
            "看电影" to "娱乐",
            "买书" to "教育",
            "看病" to "医疗"
        )
        
        for ((keyword, cat) in categoryKeywords) {
            if (voiceText.contains(keyword)) {
                category = cat
                break
            }
        }
        
        return SmartInputResult(
            success = true,
            message = "语音解析完成",
            amount = amount,
            merchant = merchant,
            note = voiceText,
            extractedText = listOf(voiceText),
            suggestedCategory = category
        )
    }
}

/**
 * 智能输入结果
 */
data class SmartInputResult(
    val success: Boolean,
    val message: String,
    val amount: BigDecimal?,
    val merchant: String?,
    val note: String?,
    val extractedText: List<String>,
    val suggestedCategory: String? = null
)