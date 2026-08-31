# IDEA 批注插件 PRD

## Solution

为 IntelliJ IDEA 提供轻量批注插件：用户选中本地文本后，可从 IDEA 原生浮动代码工具栏或编辑器右键菜单打开内嵌批注输入框，输入可为空的评论，并将文件绝对路径、选区行号、所选文本和评论按固定 Markdown 格式复制到系统剪贴板。

## Scope

### IDEA 插件范围

- 识别当前编辑器中的有效单选区及其本地文件来源。
- 接入 IDEA 原生浮动代码工具栏，并保留编辑器右键菜单入口。
- 提供多行批注输入、取消、复制、成功及失败反馈。
- 生成带绝对路径和 1-based 行号的固定 Markdown 批注载荷。
- 提供默认英文和简体中文界面文案。
- 交付完整 Gradle 源码工程和可本地安装的插件 ZIP。
- 通过签名插件包首次创建 JetBrains Marketplace 条目，并在后续版本中由版本标签触发自动构建、兼容性校验、签名和上传。

### 涉及角色

- 使用 IntelliJ IDEA 阅读、编写代码或其他文本文件，并需要把局部内容连同批注意见复制到 Codex 等外部对话的开发人员。

## Out of Scope

- 直接向 Codex、ChatGPT 或其他聊天窗口发送内容。
- 批注持久化、历史记录、多条批注聚合和编号管理。
- 语音输入、截图附件、删除批注及像素级复刻 Codex 界面。
- Diff 编辑器、无稳定本地路径的临时文件和多个非空选区。
- IntelliJ IDEA 2026.1 及更早版本、其他 JetBrains 产品的兼容承诺。
- 浮动工具栏位置、尺寸、快捷键、载荷模板和插件设置页。

## User Stories

1. As an IDEA user, I want to select text and quickly open a comment input, so that I can prepare a Codex-ready annotation without manually assembling context.
2. As an IDEA user, I want the source path and selected line range included automatically, so that the pasted annotation can locate the original code precisely.
3. As an IDEA user, I want the annotation entry integrated into IDEA’s native floating code toolbar, so that it follows the IDE’s existing selection interaction.
4. As an IDEA user, I want my comment preserved when clipboard writing fails, so that I can retry without re-entering it.
5. As a plugin maintainer, I want a version tag to publish a signed update automatically, so that Marketplace releases are reproducible and do not expose credentials.

## Requirements

### 批注入口

#### IDEA 插件逻辑

- 当前编辑器必须关联一个有效、具有真实本地绝对路径的文本文件，并且恰好存在一个非空选区，才允许发起批注。
- 普通本地文件和具有真实本地路径的 Scratch 可用；文件存在未保存修改时仍可用，所选文本读取当前编辑器缓冲区。
- Diff、无稳定本地路径的编辑器或多个非空选区中，原生浮动代码工具栏和右键菜单都不显示“批注…”，且不额外提示。
- 有效选区的 IDEA 原生浮动代码工具栏显示“批注…”；点击后立即隐藏当前浮动工具栏，避免遮挡批注输入框。
- 有效选区的编辑器右键菜单始终提供“批注…”。

### 批注输入与关闭

#### IDEA 插件逻辑

- 点击原生浮动代码工具栏或右键菜单中的“批注…”后，在选区末行下方打开同一个块级内嵌多行输入框，并立即聚焦。
- 内嵌输入框使用固定的 520 逻辑像素宽度、紧凑高度、当前编辑器背景和单层圆角边框，不随编辑器宽度拉伸。
- 输入框显示在选区末行下方，左边缘优先与选区起点对齐；空间不足时向编辑器当前可视区域内收。
- 批注允许为空。非空批注保留内部换行、缩进和空格，只去除整个输入首尾的空白；仅含空白的输入按空批注处理。
- `Esc`、点击输入框外部或点击“取消”均关闭输入框且不写入剪贴板。
- 取消后保留原选区。
- 同一编辑器同一时刻最多存在一个批注输入框。

### 国际化

#### IDEA 插件逻辑

- Action、输入提示、按钮、成功反馈和失败反馈等全部用户可见文案必须来自资源包。
- 默认资源包使用英文，并提供 `zh_CN` 简体中文资源包；插件文案跟随 IDEA 当前界面语言。
- 首版不提供插件内部语言切换设置。

### 批注载荷

#### IDEA 插件逻辑

- `Source` 使用文件本地绝对路径并追加 1-based 选区行号：
  - 单行选区：`<绝对路径>:<行号>`。
  - 多行选区：`<绝对路径>:<起始行>-<结束行>`。
- 起始行取选区首字符所在行；结束行取最后一个实际选中字符所在行。选区结束 offset 为排他值，末尾换行符不得把下一行计入范围。
- 所选文本除 IDEA 已统一使用 `\n` 外保持原样，包括缩进、空行、首尾空格和首尾换行。
- 所选文本和非空批注的每个物理行都添加 `> ` 前缀；章节之间统一使用一个裸 `>`。
- 载荷最后以一个裸 `>` 结束，并在其后保留恰好一个换行符。
- 非空批注的标准输出为：

```markdown
> **Source:**
> /project/src/Example.java:42-45
>
> **Selected text:**
> first line
>     second line
>
> **User comment:**
> 第一行
> 第二行
>
```

- 空批注的 `User comment` 标题后直接输出最终裸 `>`。

### 复制反馈与重复批注

#### IDEA 插件逻辑

- 点击“复制”成功后，将完整批注载荷写入系统剪贴板、关闭内嵌输入框并短暂显示“批注已复制”。
- 成功后保留原选区；原生浮动代码工具栏的后续显示由 IDEA 管理。
- 成功后，右键菜单仍可对同一选区立即再次发起批注。
- 剪贴板写入失败时，内嵌输入框必须保持打开，批注内容不得丢失，并在输入框内显示“复制失败，请重试”。用户可再次点击“复制”，不弹模态错误框。

### 交付与验收

#### IDEA 插件逻辑

- 首版最低支持 IntelliJ IDEA 2026.2，目标构建分支为 `262`。
- 插件应保持跨平台实现，不编写 macOS 专用路径逻辑；首版只要求在当前 macOS + IntelliJ IDEA 2026.2.1 环境完成人工验收。
- 人工验收必须覆盖：单行和多行选区、行号边界、未保存内容、多行及空批注、原生浮动工具栏入口、右键入口、固定宽度内嵌输入框的定位与样式、英文和简体中文文案、取消关闭、复制失败重试，以及不支持场景不显示入口。
- `buildPlugin` 必须生成可从 IDEA 设置页本地安装的 ZIP，安装后能够完成全部人工验收流程。
- 首版不要求格式化单元测试或完整 UI 自动化。

### Marketplace 发布

#### 发布逻辑

- Marketplace 插件 ID 固定为 `com.zuozhi.ideaannotation`；最低兼容构建为 `262`，不设置最高兼容构建。
- 插件包必须包含英文描述、当前版本变更说明、有效的 vendor 网站与邮箱，以及独立的 40×40 SVG 插件图标。
- 签名证书链、私钥、私钥密码和 Marketplace Personal Access Token 只通过 GitHub Actions Secrets 或本地环境变量提供，不写入仓库、构建日志或发布产物。
- 首次发布由维护者在 JetBrains Marketplace 创建 Vendor 和插件条目，选择许可证、标签、渠道与是否隐藏，并上传 GitHub Actions 生成的签名 ZIP。
- Marketplace 已存在插件条目后，稳定版本使用与 Gradle `version` 完全一致的 SemVer 标签触发自动发布，例如版本 `1.0.2` 对应标签 `1.0.2`。
- 标签发布必须依次完成版本一致性检查、`buildPlugin`、`verifyPlugin`、`signPlugin` 和 `publishPlugin`；任一步失败均不得创建 GitHub Release。
- 首次手工运行签名包工作流时，自动创建同版本 GitHub Release 并附带签名 ZIP；该 Release 不代表 Marketplace 已上传。后续版本在 Marketplace 上传成功后再创建同名 GitHub Release。
- GitHub Release 只证明对应签名发布物已生成，不代表 JetBrains 审核通过或版本已公开可安装。
- 首次版本、缺陷修复版本和新增向后兼容功能分别遵循 `1.0.0`、`1.0.x` 和 `1.1.0` 的版本规则；每次发布同时更新 `version`、`RELEASE_NOTES.md` 和 `CHANGELOG.md`。
- 新插件和每个更新都以 JetBrains Marketplace 后台显示的审核状态为最终发布证据；审核状态、版本、时间和插件页面链接应记录在发布交接中。

## Implementation Decisions

### 锁定决策

| 决策项 | 决策值 | 来源 |
| --- | --- | --- |
| 技术基线 | IDEA 2026.2、Java 25、Gradle 9.x、IntelliJ Platform Gradle Plugin 2.18.1 | [IntelliJ 平台能力与兼容基线](./wayfinder/01-IntelliJ平台能力与兼容基线.md) |
| 平台 API | 优先使用公开 API；允许使用 IDEA 2026.2 当前原生实现完成工具栏隐藏等必要交互优化 | [IntelliJ 平台能力与兼容基线](./wayfinder/01-IntelliJ平台能力与兼容基线.md) |
| 批注入口 | IDEA 原生浮动代码工具栏 + 编辑器右键菜单，共用块级内嵌输入框 | [批注入口与交互流程](./wayfinder/02-批注入口与交互流程.md) |
| 载荷格式 | 固定 Markdown 引用格式，允许空批注，结尾恰好一个换行符 | [批注载荷格式边界](./wayfinder/03-批注载荷格式边界.md) |
| Source | 本地绝对路径 + 1-based 单行或行范围 | [批注来源定位规则](./wayfinder/04-批注来源定位规则.md) |
| 交付物 | Gradle 源码工程 + 本地安装 ZIP | [支持范围与交付边界](./wayfinder/05-支持范围与交付边界.md) |
| 验收证据 | 当前 macOS IDEA 2026.2.1 沙箱人工验收 | [验收场景与失败反馈](./wayfinder/06-验收场景与失败反馈.md) |
| Marketplace 发布 | 首次人工建档并上传签名 ZIP；后续由同版本 SemVer 标签自动发布；Marketplace 后台审核状态为最终证据 | 当前发布需求 |
