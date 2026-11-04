# PROGRESS.md - 《简记账》（CoinFlow）项目进度与迭代日志

> 本文档用于实时追踪《简记账》项目的开发进度、里程碑完成情况以及每次调试与改动的详细日志。
> **每次完成代码修改或调试后均需在此更新。**

---

## 一、 项目概况

* **应用名称**：简记账 (CoinFlow)
* **应用包名**：`com.bookkeeping.app`
* **架构方案**：Clean Architecture + MVI/MVVM (StateFlow)
* **核心技术栈**：Kotlin 2.3 + Jetpack Compose + Material 3 + Room 2.8.5 + KSP
* **存储策略**：100% 本地离线优先（SQLite / Room）
* **最新 APK 产物**：[`app/build/outputs/apk/debug/app-debug.apk`](app/build/outputs/apk/debug/app-debug.apk) (19.5 MB)

---

## 二、 里程碑与阶段计划 (Milestones)

| 阶段 | 核心任务 | 状态 | 交付物 / 验收标准 |
| :---: | :--- | :---: | :--- |
| **Phase 1** | **工程骨架与本地数据库底座** | ✅ 已完成 | Gradle 9.1 + AGP 9.0.1 骨架、Room 三大实体、DAO 响应式查询、自动预置 15 类常用分类与 4 个账户、账户余额原子联动 |
| **Phase 2** | **核心记账与流水明细 UI** | ✅ 已完成 | 首页月度切换看板、自然日分组流水、弹窗二次确认删账、快捷记账弹层、自定义加减数字键盘、Canvas 动态环形统计图、资产列表与新增账户 |
| **Phase 3** | **真机联调与体验调优** | 🔄 进行中 | USB 数据线连接真机安装、界面交互手感调优、不同分辨率屏幕适配、基础输入边界值校验 |
| **Phase 4** | **统计图表深化与账单筛选** | ⏳ 待开始 | 支持按时间区间（近7天/月/年）自定义筛选、搜索备注/分类、月度柱状对比趋势图 |
| **Phase 5** | **预算管理与数据备份** | ⏳ 待开始 | 月度预算限额与超支预警进度条、本地数据 CSV 导出与恢复 |

---

## 三、 详细变更与调试日志 (Changelog)

| 日期 / 时间 | 模块 / 环节 | 变更详情与说明 | 验证与测试状态 |
| :--- | :--- | :--- | :--- |
| **2026-11-04 00:15** | **产品架构** | 完成记账应用全栈设计与规划，确定本地离线优先与 Compose 方案 | 架构文档已确认通过 |
| **2026-11-04 00:26** | **工程环境** | 适配 Gradle 9.1 与 JBR 25，解决 Foojay 工具链自动下载网络挂起问题；配置 `gradle.properties` 与 JVM 参数 | `./gradlew.bat tasks` 执行成功 |
| **2026-11-04 00:30** | **数据持久层** | 编写 Room 2.8.5 (KSP 2.3.13) 实体与 DAO；实现 `BookkeepingRepository` 记账/删账时账户余额自动平账与预置数据 | 编译通过，KSP 生成代码正常 |
| **2026-11-04 00:34** | **UI 表现层** | 编写 `LedgerScreen`、`AddTransactionSheet`、`NumberPad`、`DonutChart`、`StatsScreen`、`AccountScreen` 及 `BookkeepingViewModel` | Compose 界面编写完成 |
| **2026-11-04 00:35** | **测试与打包** | 编写 `FormatHelperTest` 单元测试；执行 `assembleDebug` 生成 `app-debug.apk` | 单元测试 100% 通过，APK 产出完成 |
| **2026-11-04 21:28** | **协作与规范** | 创建 `AGENTS.md`（工具位置、踩坑总结、协作规则）与 `PROGRESS.md`（进度看板） | 文档建立完成 |
