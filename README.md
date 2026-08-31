# Selection Annotation

[![Build](https://github.com/zuozh11/idea-annotation/actions/workflows/build.yml/badge.svg)](https://github.com/zuozh11/idea-annotation/actions/workflows/build.yml)
[![Get from JetBrains Marketplace](https://img.shields.io/badge/Get%20from-JetBrains%20Marketplace-000000?logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/plugin/33955-selection-annotation)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33955)](https://plugins.jetbrains.com/plugin/33955-selection-annotation)

![Selection Annotation 图标](src/main/resources/META-INF/pluginIcon.svg)

Selection Annotation 是 IntelliJ IDEA 插件，用于把编辑器选区整理成可直接粘贴到 Codex 等对话中的 Markdown 批注：自动携带本地绝对路径、准确行号、所选原文和可选评论。

## 功能

- 从 IDEA 原生浮动代码工具栏或编辑器右键菜单打开“批注…”。
- 在选区下方显示固定宽度的多行批注输入框，评论允许为空。
- 使用当前编辑器缓冲区内容，支持未保存文件和具有真实本地路径的 Scratch 文件。
- 自动计算 1-based 单行或多行范围，选区末尾换行不会误计下一行。
- 复制成功后保留原选区；复制失败时保留已输入评论以便重试。
- 用户界面提供英文和简体中文资源。

## 使用方式

1. 在本地文本文件中选中一段文字。
2. 点击浮动代码工具栏中的 **批注…**，或从编辑器右键菜单选择 **批注…**。
3. 输入可选评论，点击 **复制**。
4. 将剪贴板内容粘贴到目标对话。

示例输出：

```markdown
> **Source:**
> /project/src/Example.java:42-45
>
> **Selected text:**
> first line
>     second line
>
> **User comment:**
> 请检查这里的边界条件
>
```

## 兼容范围

- IntelliJ IDEA 2026.2 及以上，最低平台构建为 `262`。
- Java/JBR 25。
- 普通本地文本文件和具有真实本地路径的 Scratch 文件。
- 不支持 Diff 编辑器、无稳定本地路径的临时文件和多个非空选区。

## 安装

本地构建后，在 IDEA 中打开 **Settings → Plugins → ⚙ → Install Plugin from Disk…**，选择 `build/distributions/` 下的 ZIP。

Marketplace `1.0.1` 已提交 JetBrains 审核，当前状态为 **Under review**。插件页面：[Selection Annotation](https://plugins.jetbrains.com/plugin/33955-selection-annotation)；真实审核证据见 [发布指南的当前状态](docs/publishing.md#2026-08-31-当前状态)。

## 开发

项目为单模块 Java Gradle 工程，使用仓库内的 Gradle Wrapper：

```bash
./gradlew buildPlugin   # 生成可安装 ZIP
./gradlew runIde        # 启动带插件的 IDEA 沙箱
./gradlew test          # 运行测试
./gradlew verifyPlugin  # 运行 IntelliJ Plugin Verifier
```

如果系统默认 Java 不是 25，可使用 IDEA 自带 JBR：

```bash
export JAVA_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew buildPlugin
```

可安装 ZIP 输出到 `build/distributions/`，构建产物不进入 Git。

## 发布维护

- 当前版本由 `build.gradle.kts` 的 `version` 定义。
- 当前版本 Marketplace/GitHub Release 说明维护在 `RELEASE_NOTES.md`，完整历史维护在 `CHANGELOG.md`。
- 首次建档、签名证书、GitHub Secrets、标签触发、失败处理和发布证据见 [JetBrains Marketplace 发布指南](docs/publishing.md)。
- Developer EULA 的仓库副本见 [EULA.md](EULA.md)，Marketplace 使用[公开版本](https://gist.github.com/zuozh11/ea84559dc08fa8efeba10d0c5a1152e1)。

## 数据边界

插件只读取当前选区与本地文件路径，并把生成的批注写入系统剪贴板；不发送网络请求，不收集遥测，也不持久化批注历史。绝对路径会出现在剪贴板内容中，粘贴到外部服务前请确认目标和内容。
