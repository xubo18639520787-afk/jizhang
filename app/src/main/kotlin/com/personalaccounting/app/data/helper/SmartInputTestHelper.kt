package com.personalaccounting.app.data.helper

import kotlinx.coroutines.delay
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能输入测试助手
 * 用于测试OCR和语音识别的准确率和用户体验
 */
@Singleton
class SmartInputTestHelper @Inject constructor(
    private val smartInputHelper: SmartInputHelper
) {
    
    /**
     * 测试用例数据
     */
    data class TestCase(
        val input: String,
        val expectedAmount: BigDecimal?,
        val expectedMerchant: String?,
        val expectedCategory: String?,
        val description: String
    )
    
    /**
     * 测试结果
     */
    data class TestResult(
        val testCase: TestCase,
        val actualResult: SmartInputResult,
        val isAmountCorrect: Boolean,
        val isMerchantCorrect: Boolean,
        val isCategoryCorrect: Boolean,
        val overallScore: Float
    )
    
    /**
     * OCR测试用例
     */
    private val ocrTestCases = listOf(
        TestCase(
            input = "超市购物小票\n合计: ¥128.50\n沃尔玛超市",
            expectedAmount = try { BigDecimal("128.50") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "沃尔玛超市",
            expectedCategory = "日用品",
            description = "超市购物小票识别"
        ),
        TestCase(
            input = "餐厅账单\n总计: 89.00元\n海底捞火锅",
            expectedAmount = try { BigDecimal("89.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "海底捞火锅",
            expectedCategory = "餐饮",
            description = "餐厅账单识别"
        ),
        TestCase(
            input = "加油站发票\n金额: 300.00\n中石化加油站",
            expectedAmount = try { BigDecimal("300.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "中石化加油站",
            expectedCategory = "交通",
            description = "加油站发票识别"
        ),
        TestCase(
            input = "药店购药\n应付: ¥45.80\n同仁堂药店",
            expectedAmount = try { BigDecimal("45.80") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "同仁堂药店",
            expectedCategory = "医疗",
            description = "药店购药识别"
        ),
        TestCase(
            input = "电影票\n票价: 58元\n万达影城",
            expectedAmount = try { BigDecimal("58.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "万达影城",
            expectedCategory = "娱乐",
            description = "电影票识别"
        )
    )
    
    /**
     * 语音输入测试用例
     */
    private val voiceTestCases = listOf(
        TestCase(
            input = "在超市花了一百二十八块五毛钱买日用品",
            expectedAmount = try { BigDecimal("128.50") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "超市",
            expectedCategory = "日用品",
            description = "超市购物语音输入"
        ),
        TestCase(
            input = "今天在海底捞吃火锅花了八十九块钱",
            expectedAmount = try { BigDecimal("89.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "海底捞",
            expectedCategory = "餐饮",
            description = "餐厅消费语音输入"
        ),
        TestCase(
            input = "加油站加油三百块钱",
            expectedAmount = try { BigDecimal("300.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "加油站",
            expectedCategory = "交通",
            description = "加油消费语音输入"
        ),
        TestCase(
            input = "在药店买药花了四十五块八毛",
            expectedAmount = try { BigDecimal("45.80") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "药店",
            expectedCategory = "医疗",
            description = "药店购药语音输入"
        ),
        TestCase(
            input = "看电影票价五十八元",
            expectedAmount = try { BigDecimal("58.00") } catch (e: NumberFormatException) { BigDecimal.ZERO },
            expectedMerchant = "电影院",
            expectedCategory = "娱乐",
            description = "电影消费语音输入"
        )
    )
    
    /**
     * 运行OCR识别准确率测试
     */
    suspend fun runOcrAccuracyTest(): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        for (testCase in ocrTestCases) {
            // 模拟OCR识别过程
            delay(500) // 模拟网络请求延迟
            
            val actualResult = smartInputHelper.extractTransactionInfo(
                testCase.input.split("\n")
            )
            
            val testResult = evaluateResult(testCase, actualResult)
            results.add(testResult)
        }
        
        return results
    }
    
    /**
     * 运行语音识别准确率测试
     */
    suspend fun runVoiceAccuracyTest(): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        for (testCase in voiceTestCases) {
            // 模拟语音识别过程
            delay(300) // 模拟语音处理延迟
            
            val actualResult = smartInputHelper.parseVoiceInput(testCase.input)
            
            val testResult = evaluateResult(testCase, actualResult)
            results.add(testResult)
        }
        
        return results
    }
    
    /**
     * 评估测试结果
     */
    private fun evaluateResult(testCase: TestCase, actualResult: SmartInputResult): TestResult {
        val isAmountCorrect = testCase.expectedAmount?.let { expected ->
            actualResult.amount?.let { actual ->
                expected.compareTo(actual) == 0
            } ?: false
        } ?: (actualResult.amount == null)
        
        val isMerchantCorrect = testCase.expectedMerchant?.let { expected ->
            actualResult.merchant?.contains(expected, ignoreCase = true) ?: false
        } ?: (actualResult.merchant == null)
        
        val isCategoryCorrect = testCase.expectedCategory?.let { expected ->
            actualResult.suggestedCategory?.equals(expected, ignoreCase = true) ?: false
        } ?: (actualResult.suggestedCategory == null)
        
        // 计算总体得分
        val scores = listOf(
            if (isAmountCorrect) 1.0f else 0.0f,
            if (isMerchantCorrect) 1.0f else 0.0f,
            if (isCategoryCorrect) 1.0f else 0.0f
        )
        val overallScore = scores.average().toFloat()
        
        return TestResult(
            testCase = testCase,
            actualResult = actualResult,
            isAmountCorrect = isAmountCorrect,
            isMerchantCorrect = isMerchantCorrect,
            isCategoryCorrect = isCategoryCorrect,
            overallScore = overallScore
        )
    }
    
    /**
     * 生成测试报告
     */
    fun generateTestReport(ocrResults: List<TestResult>, voiceResults: List<TestResult>): String {
        val sb = StringBuilder()
        
        sb.appendLine("# 智能输入功能测试报告")
        sb.appendLine()
        
        // OCR测试结果
        sb.appendLine("## OCR识别测试结果")
        sb.appendLine()
        val ocrAccuracy = ocrResults.map { it.overallScore }.average()
        sb.appendLine("**总体准确率**: ${String.format("%.1f", ocrAccuracy * 100)}%")
        sb.appendLine()
        
        ocrResults.forEach { result ->
            sb.appendLine("### ${result.testCase.description}")
            sb.appendLine("- **输入**: ${result.testCase.input.replace("\n", " | ")}")
            sb.appendLine("- **期望金额**: ${result.testCase.expectedAmount}")
            sb.appendLine("- **实际金额**: ${result.actualResult.amount}")
            sb.appendLine("- **期望商家**: ${result.testCase.expectedMerchant}")
            sb.appendLine("- **实际商家**: ${result.actualResult.merchant}")
            sb.appendLine("- **期望分类**: ${result.testCase.expectedCategory}")
            sb.appendLine("- **实际分类**: ${result.actualResult.suggestedCategory}")
            sb.appendLine("- **准确率**: ${String.format("%.1f", result.overallScore * 100)}%")
            sb.appendLine()
        }
        
        // 语音测试结果
        sb.appendLine("## 语音识别测试结果")
        sb.appendLine()
        val voiceAccuracy = voiceResults.map { it.overallScore }.average()
        sb.appendLine("**总体准确率**: ${String.format("%.1f", voiceAccuracy * 100)}%")
        sb.appendLine()
        
        voiceResults.forEach { result ->
            sb.appendLine("### ${result.testCase.description}")
            sb.appendLine("- **输入**: ${result.testCase.input}")
            sb.appendLine("- **期望金额**: ${result.testCase.expectedAmount}")
            sb.appendLine("- **实际金额**: ${result.actualResult.amount}")
            sb.appendLine("- **期望商家**: ${result.testCase.expectedMerchant}")
            sb.appendLine("- **实际商家**: ${result.actualResult.merchant}")
            sb.appendLine("- **期望分类**: ${result.testCase.expectedCategory}")
            sb.appendLine("- **实际分类**: ${result.actualResult.suggestedCategory}")
            sb.appendLine("- **准确率**: ${String.format("%.1f", result.overallScore * 100)}%")
            sb.appendLine()
        }
        
        // 总结
        sb.appendLine("## 测试总结")
        sb.appendLine()
        sb.appendLine("- **OCR识别总体准确率**: ${String.format("%.1f", ocrAccuracy * 100)}%")
        sb.appendLine("- **语音识别总体准确率**: ${String.format("%.1f", voiceAccuracy * 100)}%")
        sb.appendLine("- **综合准确率**: ${String.format("%.1f", (ocrAccuracy + voiceAccuracy) / 2 * 100)}%")
        sb.appendLine()
        
        // 优化建议
        sb.appendLine("## 优化建议")
        sb.appendLine()
        if (ocrAccuracy < 0.8) {
            sb.appendLine("- OCR识别准确率较低，建议优化文字提取算法和关键词匹配规则")
        }
        if (voiceAccuracy < 0.8) {
            sb.appendLine("- 语音识别准确率较低，建议优化语音解析逻辑和中文数字转换")
        }
        sb.appendLine("- 可以考虑增加更多的商家关键词和分类规则")
        sb.appendLine("- 建议收集用户反馈，持续优化识别算法")
        
        return sb.toString()
    }
}