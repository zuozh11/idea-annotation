---
name: intellij-plugin-development
description: 开发、修改或诊断本仓库的 IntelliJ Platform 插件时使用，覆盖 Action、编辑器选区、内嵌 UI、Keymap、资源包、plugin.xml、Gradle 与平台升级；不用于普通 Java 应用或稳定版本发布。
---

# IntelliJ 插件开发

先运行项目知识 `scope`，并为行为演进加载场景 `A`、为平台集成加载场景 `B`。本 Skill 提供少走弯路的路线，当前代码、构建配置和目标平台 API 才是实时事实。

## 建立当前基线

1. 从 `build.gradle.kts` 和 `plugin.xml` 读取当前语言、模块形态、目标 IDE、工具链、兼容范围、插件身份与注册点。
2. 从受影响的实现、最近相关版本 PRD、`CHANGELOG.md` 和用户最新确认还原当前可观察行为。旧 PRD 只解释历史决策。
3. 行为或交互变更先明确用户能观察到的差异；发布、签名或证据收口改用 `$release-selection-annotation`。

## 核对平台能力

- API、扩展点、Action Group、线程约束或兼容性不确定时，按任务读取 [官方资料索引](references/official-docs.md)。官方文档不足时查当前目标 IDE 依赖源码、Platform Explorer 或 SDK Code Samples。
- 区分稳定公开 API、实验 API 与内部 API。平台升级时重新核对现有取舍并优先检查稳定替代，不把当前 API 状态缓存进 Skill。
- 保留当前工程语言与模块结构，除非本次需求本身要求调整；不要从模板工程迁入无关框架或示例层次。

## 项目路线

### Action 与焦点上下文

- 让 `update()` 快速、无副作用，并完整覆盖启用与可见状态；按当前平台契约选择更新线程。
- 从当前 `DataContext` 读取 Editor、文件与 Project View 选择，不使用失焦界面的残留状态。Action 不持有 Editor、Project、Swing 组件或会话状态。
- 浮动工具栏、编辑器菜单和 Project View 的注册点以当前目标 IDE 中真实 Group/Place 为准。需要关闭平台提示层时先找公开 Hint/Popup 能力，不回退到内部浮动工具栏实现。

### Editor、选区与路径

- 文本来自当前 `Document` 和 Caret 选区，未保存内容不回读磁盘。
- 结束 offset 为排他值；非空选区的末行按最后一个实际字符计算。多 Caret 的排序、忽略与组合使用当前产品规则，不从单选区示例外推。
- 文件与目录身份通过当前 Virtual File/DataContext 判断；批注上下文与独立路径链接是不同输出，不混用可用性条件。

### 内嵌 UI 与生命周期

- Editor 内组件由 editor-owned service/controller 管理，随 Editor 或 Inlay 释放；Action 只负责触发。
- Swing 与 Editor UI 在 EDT 操作；耗时 VFS、PSI、索引或 I/O 离开 EDT。只有实际修改 Document/PSI 时才引入相应 Command/Write API。
- 定位按当前选择的可视几何与缩放语义计算，复用 Editor、Inlay、Popup、Balloon、JBUI/UIUtil，而不是复制固定设备像素。
- 输入法组合态、焦点转移、取消、失败重试和评论保留以当前可观察行为为验收对象。

### 设置与 Keymap

- 快捷键由 IDEA Keymap 管理；设置页展示当前映射并跳转到同一 Action。
- 跳转 Keymap 时使用当前平台提供的按 Action ID 选择能力；不要依靠搜索框初始化延迟、固定 Timer 或 Swing 显示时序猜测。
- 设置项只持有自身偏好，不维护第二套快捷键状态。

### 文案与 Markdown

- 用户可见文字通过现有 bundle 访问类读取，并同步维护仓库当前支持的翻译；ID、路径和 Markdown 协议标记保持非本地化。
- 批注载荷和独立路径链接是外部协议。修改 formatter 前逐项确认空行、缩进、转义、动态围栏、多选区排序、评论与结尾，再同步相关版本文档。

## 交付

- 只运行能证明当前请求的最小检查。行为或 UI 需要人工确认时改用 `$sandbox-selection-annotation`。
- 报告静态实现、编译或打包、sandbox 启动、用户验收分别证明了什么，不把它们合并成“已验证”。
