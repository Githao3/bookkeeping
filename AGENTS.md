# AGENTS.md - 记账项目开发与协作指南

> 本文档面向参与《简记账》（CoinFlow）项目开发的所有 AI Agent 与开发者，记录了本机关键开发环境、行为约束铁律、避坑指南、调试规范以及开发进度文档维护规则。

---

## 一、 行为铁律与开发约束 (Core Constraints & Rules)

> [!CAUTION]
> **以下为最高优先级的强约束行为铁律，所有 Agent 必须严格遵守，不得违背：**

1. **严禁凭空臆造，求真务实（No Hallucination - Search & Verify First）**：
   * 严禁臆造不存在的 API、类库版本号、Gradle 配置项或系统行为。
   * 遇到不确定的版本兼容性（如 Kotlin 2.x 与 KSP/Room 对应版本）、Android 最新平台特性变更、废弃 API 替代方案等，**必须优先通过网络搜索或执行命令核查验证真实性**，确保事实与代码 100% 准确。
2. **借鉴先行，调研成熟实现（Search & Learn Before Building）**：
   * 在构思或实现具体功能（例如复杂图表绘制、记账动效交互、数字键盘计算逻辑、CSV/Excel 导入导出、数据备份等）之前，**必须先进行网络搜索，检索业界成熟开源项目、优质博客或官方推荐方案进行借鉴**。
   * 吸收优秀的设计模式与交互细节，拒绝闭门造车或产出粗制滥造、脆弱劣质的实现。
3. **进度文档强行同步（Keep PROGRESS.md Updated）**：
   * 任何阶段性编码、Bug 修复、UI 样式调整或真机联调后，**必须第一时间更新根目录下的 [`PROGRESS.md`](PROGRESS.md)**，保证项目进度与改动履历清晰可溯。

---

## 二、 本机环境与关键工具路径 (Tool Locations)

在进行构建、调试或命令行操作时，请优先使用以下已被验证过的绝对路径与配置：

| 工具 / 环境 | 绝对路径 / 配置 | 说明 |
| :--- | :--- | :--- |
| **Android SDK 根目录** | `D:\Android\Sdk` | `local.properties` 中已配置 `sdk.dir=D\:\\Android\\Sdk` |
| **ADB (Android Debug Bridge)** | `D:\Android\Sdk\platform-tools\adb.exe` | 用于真机连接、安装 APK、Logcat 查看及屏幕交互 |
| **Build Tools** | `D:\Android\Sdk\build-tools\36.0.0` | 编译构建工具链 |
| **Compile Platforms** | `D:\Android\Sdk\platforms\android-37.0` | API 37 编译平台 |
| **Java / JDK (JBR)** | `D:\Android\AndroidStudio\jbr` | OpenJDK 25.0.4（Android Studio 内置的高性能运行时） |
| **Android CLI** | `C:\Users\Admin\AppData\AndroidCLI\android.exe` | 官方 Google Android CLI 工具 |
| **GitHub CLI (gh)** | `D:\Program Files\GitHub CLI\gh.exe` | 已登录账号 `Githao3`，支持仓库托管与同步 |
| **Gradle 用户目录** | `D:\Android\.gradle` | 集中缓存目录，避免占用 C 盘空间 |
| **Gradle Wrapper** | `.\gradlew.bat` | 当前项目集成 Gradle 9.1.0 Wrapper |
| **Debug APK 输出路径** | `app\build\outputs\apk\debug\app-debug.apk` | 编译生成的最新测试包 |

---

## 三、 踩坑记录与重要技术经验 (Pitfalls & Gotchas)

### 1. Java 25 与 Foojay Toolchain 自动下载冲突
* **现象**：Gradle 9.1 构建时会默认尝试通过 Foojay Disco API 自动下载并切到 JDK 17，在某些内网/国内网络下极易超时挂起，导致 `ToolchainProvisioningException`。
* **解决方案**：
  * 在 `gradle.properties` 中添加 `org.gradle.java.installations.auto-download=false`。
  * 在 `app/build.gradle.kts` 中不强制声明 `jvmToolchain(17)`，统一复用本地的 JBR 25，并通过 `compileOptions` 指定目标字节码为 Java 17。
  * 在 `gradle.properties` 的 `org.gradle.jvmargs` 中加入 `--add-opens=java.base/java.nio.file=ALL-UNNAMED` 等必要开放参数。

### 2. AGP 9.0.1 对 Compile SDK 37 的兼容性告警
* **现象**：AGP 9.0.1 检测到 `compileSdk = 37` 会提示尚未经过官方充分验证的警告。
* **解决方案**：在 `gradle.properties` 中配置 `android.suppressUnsupportedCompileSdk=37.0` 即可完全消除警告。

### 3. gradlew.bat 运行时缺失 JAVA_HOME 容错
* **现象**：当外部终端直接调用 `gradlew.bat` 且系统未导出 `JAVA_HOME` 时，批处理脚本会直接报错退出。
* **解决方案**：已在 `gradlew.bat` 头部内置自动 fallback 检测：
  ```bat
  if not defined JAVA_HOME if exist "D:\Android\AndroidStudio\jbr" set JAVA_HOME=D:\Android\AndroidStudio\jbr
  ```

### 4. 老旧安卓机型（Android 10 及以下）调试
* **现象**：Android 11 之前没有独立的“无需插线系统级无线调试”。
* **解决方案**：优先引导使用 USB 数据线直连。如需无线，先插线执行 `adb tcpip 5555`，再拔线使用 `adb connect <手机IP>:5555`。
* **注意**：部分国产 ROM（如小米 MIUI / HyperOS）在 USB 调试下还需要额外开启 **「USB 安装」** 和 **「USB 调试（安全设置）」** 才能正常写入 APK。

---

## 四、 进度文档维护规范 (Progress Updating Rule)

> [!IMPORTANT]
> **强规则要求**：任何 Agent 在完成阶段性开发、Bug 修复、UI 调整或真机调试改动后，**必须同步更新根目录下的进度文档 [`PROGRESS.md`](PROGRESS.md)**。

### 每次更新需要维护的内容：
1. **当前开发阶段与任务清单**：核对并勾选已完成的任务，标注进行中或下阶段待办事项。
2. **变更日志（Changelog）**：在文档的更新日志表格中追加一条记录，包含：
   * **日期/时间**
   * **改动模块**（如：数据层 / UI 账单页 / 构建脚本）
   * **改动摘要**（做了哪些修改、解决了什么问题）
   * **测试与验证状态**（如：单测通过 / APK 编译成功 / 真机验证正常）

---

## 五、 常用命令行速查表 (Cheat Sheet)

```bash
# 1. 检查 ADB 已连接设备
D:\Android\Sdk\platform-tools\adb.exe devices -l

# 2. 安装当前编译好的 Debug APK
D:\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk

# 3. 启动应用主界面
D:\Android\Sdk\platform-tools\adb.exe shell am start -n com.bookkeeping.app/.MainActivity

# 4. 执行全量单元测试
.\gradlew.bat test

# 5. 快速编译打包
.\gradlew.bat assembleDebug

# 6. 查看应用专属 Logcat 日志
D:\Android\Sdk\platform-tools\adb.exe logcat -s Bookkeeping:V AndroidRuntime:E

# 7. GitHub 仓库操作 (GitHub CLI 已登录)
& "D:\Program Files\GitHub CLI\gh.exe" repo create bookkeeping --public --source=. --remote=origin --push
```
