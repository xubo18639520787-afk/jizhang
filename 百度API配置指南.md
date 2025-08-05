# 百度API配置指南

## 📋 概述

本应用集成了百度智能云的OCR（文字识别）和语音识别API，需要配置相应的API密钥才能使用智能输入功能。

## 🔑 获取API密钥步骤

### 1. 注册百度智能云账号
1. 访问 [百度智能云官网](https://cloud.baidu.com/)
2. 点击"立即注册"或"登录"
3. 完成账号注册和实名认证

### 2. 创建应用获取API密钥

#### OCR文字识别API
1. 登录百度智能云控制台：https://console.bce.baidu.com/
2. 在左侧导航栏找到 **AI服务** 或直接搜索"OCR"
3. 点击 **文字识别** 进入OCR服务页面
4. 如果是首次使用，点击"开通服务"
5. 进入 **应用管理** 或 **管理中心**
6. 点击"创建应用"
7. 填写应用信息：
   - **应用名称**: 个人记账助手
   - **应用类型**: 移动应用
   - **包名**: com.personalaccounting.app
   - **应用描述**: 个人记账应用的OCR功能
8. 创建成功后，在应用列表中找到你的应用，记录：
   - **API Key**
   - **Secret Key**

#### 语音识别API
1. 在百度智能云控制台
2. 在左侧导航栏找到 **AI服务** 或搜索"语音"
3. 点击 **语音识别** 进入语音服务页面
4. 如果是首次使用，点击"开通服务"
5. 进入 **应用管理** 或 **管理中心**
6. 点击"创建应用"
7. 填写应用信息：
   - **应用名称**: 个人记账语音助手
   - **应用类型**: 移动应用
   - **包名**: com.personalaccounting.app
8. 创建成功后，在应用列表中记录：
   - **App ID**
   - **API Key**
   - **Secret Key**

## 🔧 配置API密钥

### 方法一：使用配置文件（推荐）

1. 在项目根目录创建 `local.properties` 文件（如果不存在）
2. 添加以下配置：

```properties
# 百度OCR API配置
baidu.ocr.api.key=你的OCR_API_KEY
baidu.ocr.secret.key=你的OCR_SECRET_KEY

# 百度语音识别API配置
baidu.speech.app.id=你的SPEECH_APP_ID
baidu.speech.api.key=你的SPEECH_API_KEY
baidu.speech.secret.key=你的SPEECH_SECRET_KEY
```

3. 更新 `app/build.gradle.kts` 文件，添加构建配置：

```kotlin
android {
    defaultConfig {
        // 从local.properties读取API密钥
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField("String", "BAIDU_OCR_API_KEY", "\"${localProperties.getProperty("baidu.ocr.api.key", "")}\"")
        buildConfigField("String", "BAIDU_OCR_SECRET_KEY", "\"${localProperties.getProperty("baidu.ocr.secret.key", "")}\"")
        buildConfigField("String", "BAIDU_SPEECH_APP_ID", "\"${localProperties.getProperty("baidu.speech.app.id", "")}\"")
        buildConfigField("String", "BAIDU_SPEECH_API_KEY", "\"${localProperties.getProperty("baidu.speech.api.key", "")}\"")
        buildConfigField("String", "BAIDU_SPEECH_SECRET_KEY", "\"${localProperties.getProperty("baidu.speech.secret.key", "")}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

### 方法二：直接在代码中配置（不推荐用于生产环境）

直接修改 `BaiduApiService.kt` 文件中的常量：

```kotlin
companion object {
    // OCR API配置
    private const val OCR_API_KEY = "你的OCR_API_KEY"
    private const val OCR_SECRET_KEY = "你的OCR_SECRET_KEY"
    
    // 语音识别API配置
    private const val SPEECH_APP_ID = "你的SPEECH_APP_ID"
    private const val SPEECH_API_KEY = "你的SPEECH_API_KEY"
    private const val SPEECH_SECRET_KEY = "你的SPEECH_SECRET_KEY"
}
```

## 🔒 安全注意事项

### 1. 保护API密钥
- ✅ 使用 `local.properties` 文件存储密钥
- ✅ 确保 `local.properties` 已添加到 `.gitignore`
- ❌ 不要将API密钥直接写在代码中提交到Git
- ❌ 不要在公开的代码仓库中暴露密钥

### 2. 更新.gitignore
确保 `.gitignore` 文件包含：
```
# API密钥配置文件
local.properties
```

## 📱 测试API配置

### 1. 编译应用
```bash
./gradlew assembleDebug
```

### 2. 测试OCR功能
1. 安装应用到设备
2. 进入"添加交易"页面
3. 点击"拍照识别"按钮
4. 拍摄包含文字的图片
5. 检查是否能正确识别文字和金额

### 3. 测试语音功能
1. 在"添加交易"页面
2. 点击"语音输入"按钮
3. 说出交易信息，如"买菜花了50元"
4. 检查是否能正确识别并解析

## 🚨 常见问题

### 1. API调用失败
- 检查API密钥是否正确
- 确认应用包名与注册时一致
- 检查网络连接
- 查看百度云控制台的调用统计

### 2. 权限问题
确保应用已获得以下权限：
- 相机权限（OCR功能）
- 录音权限（语音功能）
- 网络权限（API调用）

### 3. 配额限制
- 百度API有免费调用额度限制
- 超出限制需要付费或等待重置
- 可在控制台查看使用情况

## 💰 费用说明

### OCR文字识别
- 免费额度：每月1000次
- 超出后：0.015元/次

### 语音识别
- 免费额度：每月5000次
- 超出后：0.0035元/次

## 📞 技术支持

如遇到问题，可以：
1. 查看百度智能云官方文档
2. 联系百度云技术支持
3. 在项目Issues中反馈问题

---

配置完成后，应用的智能输入功能就可以正常使用了！🎉