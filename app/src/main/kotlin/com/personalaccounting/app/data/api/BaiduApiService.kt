package com.personalaccounting.app.data.api

import com.personalaccounting.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 百度API服务类 - 免费版本
 * 移除了付费API调用，提供模拟功能用于演示
 * 如需使用真实API，请配置密钥并启用相关功能
 */
@Singleton
class BaiduApiService @Inject constructor() {
    
    // 是否启用百度API功能（设为false使用免费模拟功能）
    private val enableBaiduApi = false
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // 从BuildConfig获取API密钥（仅在enableBaiduApi为true时使用）
    private val ocrApiKey = if (enableBaiduApi) BuildConfig.BAIDU_OCR_API_KEY else ""
    private val ocrSecretKey = if (enableBaiduApi) BuildConfig.BAIDU_OCR_SECRET_KEY else ""
    private val speechAppId = if (enableBaiduApi) BuildConfig.BAIDU_SPEECH_APP_ID else ""
    private val speechApiKey = if (enableBaiduApi) BuildConfig.BAIDU_SPEECH_API_KEY else ""
    private val speechSecretKey = if (enableBaiduApi) BuildConfig.BAIDU_SPEECH_SECRET_KEY else ""
    
    private var accessToken: String? = null
    private var tokenExpireTime: Long = 0
    
    /**
     * 获取OCR访问令牌
     */
    private suspend fun getOcrAccessToken(): String? = withContext(Dispatchers.IO) {
        // 检查token是否过期
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return@withContext accessToken
        }
        
        try {
            val url = "https://aip.baidubce.com/oauth/2.0/token"
            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", ocrApiKey)
                .add("client_secret", ocrSecretKey)
                .build()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                accessToken = jsonObject.getString("access_token")
                val expiresIn = jsonObject.getInt("expires_in")
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000 // 提前5分钟过期
                return@withContext accessToken
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
    
    /**
     * 获取语音识别访问令牌
     */
    private suspend fun getSpeechAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://aip.baidubce.com/oauth/2.0/token"
            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", speechApiKey)
                .add("client_secret", speechSecretKey)
                .build()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                return@withContext jsonObject.getString("access_token")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
    
    /**
     * OCR通用文字识别
     * @param imageBase64 图片的Base64编码
     * @return 识别结果
     */
    suspend fun recognizeText(imageBase64: String): OcrResult = withContext(Dispatchers.IO) {
        if (!enableBaiduApi) {
            // 免费模拟功能：返回示例识别结果
            delay(1000) // 模拟网络延迟
            return@withContext OcrResult(
                success = true,
                message = "模拟识别成功（免费版本）",
                textList = listOf(
                    "超市购物小票",
                    "商品名称：蔬菜水果",
                    "金额：￥45.80",
                    "日期：${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())}",
                    "收银员：001"
                )
            )
        }
        
        try {
            val token = getOcrAccessToken()
            if (token == null) {
                return@withContext OcrResult(false, "获取OCR访问令牌失败", emptyList())
            }
            
            val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=$token"
            
            val requestBody = FormBody.Builder()
                .add("image", imageBase64)
                .add("language_type", "CHN_ENG")
                .add("detect_direction", "true")
                .add("detect_language", "true")
                .build()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                return@withContext parseOcrResponse(responseBody ?: "")
            } else {
                return@withContext OcrResult(false, "OCR请求失败: ${response.code}", emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext OcrResult(false, "OCR识别异常: ${e.message}", emptyList())
        }
    }
    
    /**
     * 语音识别
     * @param audioData 音频数据的Base64编码
     * @param format 音频格式 (wav, pcm, amr, m4a)
     * @param rate 采样率 (8000, 16000)
     * @return 识别结果
     */
    suspend fun recognizeSpeech(
        audioData: String,
        format: String = "wav",
        rate: Int = 16000
    ): SpeechResult = withContext(Dispatchers.IO) {
        if (!enableBaiduApi) {
            // 免费模拟功能：返回示例语音识别结果
            delay(1500) // 模拟语音处理延迟
            val sampleTexts = listOf(
                "买菜花了五十元",
                "午餐消费三十八块",
                "加油费用二百元",
                "超市购物一百二十元",
                "咖啡十五元",
                "地铁费用六元"
            )
            return@withContext SpeechResult(
                success = true,
                message = "模拟识别成功（免费版本）",
                text = sampleTexts.random()
            )
        }
        
        try {
            val token = getSpeechAccessToken()
            if (token == null) {
                return@withContext SpeechResult(false, "获取语音识别访问令牌失败", "")
            }
            
            val url = "https://vop.baidu.com/server_api"
            
            val jsonBody = JSONObject().apply {
                put("format", format)
                put("rate", rate)
                put("channel", 1)
                put("cuid", "android_app")
                put("token", token)
                put("speech", audioData)
                put("len", audioData.length)
            }
            
            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                return@withContext parseSpeechResponse(responseBody ?: "")
            } else {
                return@withContext SpeechResult(false, "语音识别请求失败: ${response.code}", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext SpeechResult(false, "语音识别异常: ${e.message}", "")
        }
    }
    
    /**
     * 解析OCR响应
     */
    private fun parseOcrResponse(responseBody: String): OcrResult {
        return try {
            val jsonObject = JSONObject(responseBody)
            
            if (jsonObject.has("error_code")) {
                val errorCode = jsonObject.getInt("error_code")
                val errorMsg = jsonObject.getString("error_msg")
                return OcrResult(false, "OCR错误 ($errorCode): $errorMsg", emptyList())
            }
            
            val wordsResult = jsonObject.getJSONArray("words_result")
            val textList = mutableListOf<String>()
            
            for (i in 0 until wordsResult.length()) {
                val wordObj = wordsResult.getJSONObject(i)
                val words = wordObj.getString("words")
                textList.add(words)
            }
            
            OcrResult(true, "识别成功", textList)
        } catch (e: Exception) {
            OcrResult(false, "解析OCR响应失败: ${e.message}", emptyList())
        }
    }
    
    /**
     * 解析语音识别响应
     */
    private fun parseSpeechResponse(responseBody: String): SpeechResult {
        return try {
            val jsonObject = JSONObject(responseBody)
            
            if (jsonObject.has("err_no")) {
                val errNo = jsonObject.getInt("err_no")
                if (errNo != 0) {
                    val errMsg = jsonObject.optString("err_msg", "未知错误")
                    return SpeechResult(false, "语音识别错误 ($errNo): $errMsg", "")
                }
            }
            
            val resultArray = jsonObject.getJSONArray("result")
            val result = if (resultArray.length() > 0) {
                resultArray.getString(0)
            } else {
                ""
            }
            
            SpeechResult(true, "识别成功", result)
        } catch (e: Exception) {
            SpeechResult(false, "解析语音识别响应失败: ${e.message}", "")
        }
    }
}

/**
 * OCR识别结果
 */
data class OcrResult(
    val success: Boolean,
    val message: String,
    val textList: List<String>
)

/**
 * 语音识别结果
 */
data class SpeechResult(
    val success: Boolean,
    val message: String,
    val text: String
)