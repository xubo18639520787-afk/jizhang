# 个人记账应用 📱💰

一个基于Jetpack Compose + Material Design 3的现代化Android记账应用，采用MVVM架构设计。

## ✨ 主要特性

- 🎨 **现代化UI**: 使用Jetpack Compose + Material Design 3
- 🏗️ **MVVM架构**: 清晰的代码结构和数据流
- 💾 **本地存储**: Room数据库持久化数据
- 🔄 **响应式编程**: StateFlow实现响应式UI更新
- 🎯 **依赖注入**: Hilt框架管理依赖
- 🤖 **智能输入**: 集成百度OCR和语音识别API
- ☁️ **云端构建**: GitHub Actions自动化构建APK

## 🚀 快速开始

### 云端构建（推荐）

无需本地Android开发环境，直接使用GitHub Actions云端构建：

1. **Fork本仓库**到你的GitHub账号
2. **进入Actions页面**，点击"Run workflow"手动触发构建
3. **等待构建完成**（约5-10分钟）
4. **下载APK文件**从Artifacts部分

详细说明请查看：[云端构建说明.md](./云端构建说明.md)

### 本地开发

如果你有Android开发环境：

```bash
# 克隆仓库
git clone https://github.com/your-username/personal-accounting-app.git
cd personal-accounting-app

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease
```

## 📱 应用截图

### 主要功能界面

- **首页**: 月度统计 + 最近交易记录
- **添加交易**: 完整的交易录入表单，支持OCR和语音输入
- **统计分析**: 分类统计和图表展示
- **设置页面**: 账户管理、外观设置、数据管理

## 🏗️ 技术架构

### 核心技术栈

- **UI层**: Jetpack Compose + Material Design 3
- **架构模式**: MVVM + Repository Pattern
- **数据库**: Room Database
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose

### 项目结构

```
app/src/main/kotlin/com/personalaccounting/app/
├── data/                    # 数据层
│   ├── entity/             # 数据库实体
│   ├── dao/                # 数据访问对象
│   ├── database/           # 数据库配置
│   └── repository/         # 仓库实现
├── domain/                 # 领域层
│   ├── model/              # 领域模型
│   └── mapper/             # 实体映射
├── presentation/           # 表现层
│   ├── screen/             # UI界面
│   ├── viewmodel/          # 视图模型
│   ├── navigation/         # 导航配置
│   └── theme/              # 主题样式
└── di/                     # 依赖注入模块
```

## 🎨 设计系统

### 颜色主题

- **主色调**: 薄荷绿 (#4CAF50)
- **强调色**: 橙色 (#FF9800)
- **背景色**: Material Design 3 动态颜色

### 字体排版

- **标题**: Material Design 3 Typography Scale
- **正文**: 支持中文优化的字体显示
- **数字**: 等宽字体确保金额对齐

## 🔧 配置说明

### API配置

应用集成了百度智能云API，需要配置以下服务：

1. **百度OCR API**: 用于票据识别
2. **百度语音识别API**: 用于语音输入

在正式使用前，请在`local.properties`中配置API密钥：

```properties
BAIDU_API_KEY=your_api_key_here
BAIDU_SECRET_KEY=your_secret_key_here
```

### 数据库

应用使用Room数据库，包含以下主要表：

- **transactions**: 交易记录
- **accounts**: 账户信息  
- **categories**: 分类信息

数据库会在首次启动时自动创建并初始化默认数据。

## 📋 功能清单

### ✅ 已完成功能

- [x] 完整的MVVM架构实现
- [x] Room数据库集成
- [x] 四个主要界面开发
- [x] Material Design 3主题
- [x] 响应式UI状态管理
- [x] GitHub Actions云端构建
- [x] 中文本地化支持

### 🚧 待开发功能

- [ ] 百度API实际集成
- [ ] 数据导入导出
- [ ] 图表统计增强
- [ ] 深色模式完善
- [ ] 应用签名配置

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建功能分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -am 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 创建Pull Request

## 📄 开源协议

本项目采用MIT协议开源，详见[LICENSE](LICENSE)文件。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交GitHub Issue
- 发送邮件至项目维护者

---

**享受记账，掌控财务！** 💪✨