---
name: sandbox-selection-annotation
description: 启动或重启 Selection Annotation 的 IDEA sandbox，确认当前仓库已打开，并把行为或 UI 改动交给用户人工验收；不用于发布构建或自动替用户确认交互通过。
---

# Selection Annotation 沙箱交接

先运行项目知识 `scope` 并加载 `C01`。读取当前 `AGENTS.md`、`build.gradle.kts` 和本次变更，确定当前 IDE/JBR 与验证范围；不要沿用过去会话中的版本路径。

## 启动

1. 找到当前任务已经启动的 `runIde` 会话；需要重启时只停止该会话及其对应 sandbox IDE，不影响正式 IDEA 或其他 Gradle 任务。
2. 使用 committed Gradle wrapper 的 `runIde`，把当前仓库绝对路径作为要打开的项目。需要本地 IDEA 时，从当前安装或配置解析匹配的 JBR/`IDEA_HOME`，不在 Skill 中固定版本目录。
3. 等待 sandbox IDE 真正启动，而不是只等待 Gradle 输出任务开始。

## 确认项目上下文

- 通过当前可用的桌面 UI 能力确认 sandbox 窗口显示的是本仓库；若未打开，使用 IDEA 的打开项目流程选择当前仓库。
- 检查窗口来自 sandbox 而不是正式 IDEA，且当前改动已由本次 Gradle 运行加载。
- 除非用户明确要求 Agent 执行交互验证，否则不要代替用户点击并宣布行为通过。

## 交接

向用户说明 sandbox 已启动、当前仓库已可见、本次需要操作的验收点以及已知未覆盖范围。把“插件进入目标项目上下文”和“用户人工验收通过”分别记录；用户确认后才收口对应 UI 行为。

普通沙箱交接不追加测试、格式探针、哈希、`buildPlugin`、签名或 Marketplace 操作。用户明确改变验证范围时，按新范围执行并如实报告。
