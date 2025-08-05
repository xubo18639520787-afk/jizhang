# 🔑 百度API密钥配置快速指南

## 📋 配置步骤

### 第一步：获取API密钥

1. **访问百度智能云官网**
   - 网址：https://cloud.baidu.com/
   - 注册并完成实名认证

2. **创建OCR应用**
   - 登录控制台：https://console.bce.baidu.com/
   - 搜索"OCR"或在左侧找到"AI服务" → "文字识别"
   - 首次使用需要"开通服务"
   - 进入"应用管理"，点击"创建应用"
   - 应用名称：个人记账助手
   - 应用类型：移动应用
   - 包名：`com.personalaccounting.app`
   - 记录：**API Key** 和 **Secret Key**

3. **创建语音识别应用**
   - 在控制台搜索"语音"或找到"AI服务" → "语音识别"
   - 首次使用需要"开通服务"
   - 进入"应用管理"，点击"创建应用"
   - 应用名称：个人记账语音助手
   - 应用类型：移动应用
   - 包名：`com.personalaccounting.app`
   - 记录：**App ID**、**API Key** 和 **Secret Key**

### 第二步：配置本地密钥

1. **复制配置文件模板**
   ```bash
   cp local.properties.example local.properties
   ```

2. **编辑 local.properties 文件**
   ```properties
   # 百度OCR API配置
   baidu.ocr.api.key=你的OCR_API_KEY
   baidu.ocr.secret.key=你的OCR_SECRET_KEY

   # 百度语音识别API配置
   baidu.speech.app.id=你的SPEECH_APP_ID
   baidu.speech.api.key=你的SPEECH_API_KEY
   baidu.speech.secret.key=你的SPEECH_SECRET_KEY
   ```

### 第三步：验证配置

1. **编译项目**
   ```bash
   ./gradlew assembleDebug
   ```

2. **测试功能**
   - 安装APK到设备
   - 进入"添加交易"页面
   - 测试"拍照识别"和"语音输入"功能

## 🔒 安全提醒

- ✅ `local.properties` 文件已配置在 `.gitignore` 中，不会被提交到Git
- ✅ API密钥通过 BuildConfig 安全注入到应用中
- ❌ 不要将API密钥直接写在代码中
- ❌ 不要在公开仓库中暴露密钥

## 💡 使用提示

### OCR识别最佳实践
- 拍摄清晰的票据或文字图片
- 确保光线充足，避免反光
- 支持识别金额、商家名称等信息

### 语音识别最佳实践
- 在安静环境下录音
- 清晰说出交易信息，如"买菜花了50元"
- 支持中文数字和金额识别

## 🚨 常见问题

### 1. API调用失败
- 检查网络连接
- 确认API密钥是否正确
- 查看百度云控制台调用统计

### 2. 权限问题
确保应用已获得权限：
- 相机权限（OCR功能）
- 录音权限（语音功能）
- 网络权限（API调用）

### 3. 配额限制
- OCR：每月免费1000次
- 语音识别：每月免费5000次
- 超出后需要付费使用

## 📞 技术支持

如遇问题，可以：
1. 查看 `百度API配置指南.md` 详细文档
2. 检查百度智能云官方文档
3. 在项目Issues中反馈

---

配置完成后，你的个人记账应用就可以使用智能输入功能了！🎉