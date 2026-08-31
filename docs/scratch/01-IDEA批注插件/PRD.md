# IDEA 批注插件 PRD

## Solution

为 IntelliJ IDEA 提供接近 Codex 批注体验的轻量插件：用户选中本地文本后，可从选区右上角的小胶囊或编辑器右键菜单打开批注浮层，输入可为空的评论，并将文件绝对路径、选区行号、所选文本和评论按固定 Markdown 格式复制到系统剪贴板。插件提供胶囊开关，关闭自动入口后仍保留右键批注能力。

## Scope

### IDEA 插件范围

- 识别当前编辑器中的有效单选区及其本地文件来源。
- 提供选区右上角小胶囊和编辑器右键菜单双入口。
- 提供多行批注输入、取消、复制、成功及失败反馈。
- 生成带绝对路径和 1-based 行号的固定 Markdown 批注载荷。
- 提供应用级胶囊显示开关。
- 交付完整 Gradle 源码工程和可本地安装的插件 ZIP。

### 涉及角色

- 使用 IntelliJ IDEA 阅读、编写代码或其他文本文件，并需要把局部内容连同批注意见复制到 Codex 等外部对话的开发人员。

## Out of Scope

- 直接向 Codex、ChatGPT 或其他聊天窗口发送内容。
- 批注持久化、历史记录、多条批注聚合和编号管理。
- 语音输入、截图附件、删除批注及像素级复刻 Codex 界面。
- Diff 编辑器、无稳定本地路径的临时文件和多个非空选区。
- IntelliJ IDEA 2026.1 及更早版本、其他 JetBrains 产品的兼容承诺。
- JetBrains Marketplace 发布、插件签名和自动更新。
- 胶囊位置、尺寸、快捷键、载荷模板和项目级设置。

## User Stories

1. As an IDEA user, I want to select text and quickly open a comment input, so that I can prepare a Codex-ready annotation without manually assembling context.
2. As an IDEA user, I want the source path and selected line range included automatically, so that the pasted annotation can locate the original code precisely.
3. As an IDEA user, I want to disable the automatic capsule without losing the context-menu action, so that I can reduce editor distraction while retaining the feature.
4. As an IDEA user, I want my comment preserved when clipboard writing fails, so that I can retry without re-entering it.

## Requirements

### 批注入口

#### IDEA 插件逻辑

- 当前编辑器必须关联一个有效、具有真实本地绝对路径的文本文件，并且恰好存在一个非空选区，才允许发起批注。
- 普通本地文件和具有真实本地路径的 Scratch 可用；文件存在未保存修改时仍可用，所选文本读取当前编辑器缓冲区。
- Diff、无稳定本地路径的编辑器或多个非空选区中，不显示胶囊，右键菜单也不显示“批注…”，且不额外提示。
- 胶囊设置开启时，鼠标拖选或键盘扩展选区稳定后，在所选文本右上角显示尽量小的“批注”胶囊。
- 单行选区以选区右端定位；多行选区以第一条被选中可视行的右端定位。靠近编辑器可视区域边缘时，胶囊必须向内调整，不能溢出。
- 胶囊不得抢占编辑器焦点、清除选区或明显遮挡所选内容。编辑器滚动后不得悬空在旧位置。
- 有效选区的编辑器右键菜单始终提供“批注…”，不受胶囊设置或当前选区胶囊抑制状态影响。

### 批注输入与关闭

#### IDEA 插件逻辑

- 点击胶囊或右键“批注…”打开同一个多行输入浮层，并立即聚焦输入框。
- 批注允许为空。非空批注保留内部换行、缩进和空格，只去除整个输入首尾的空白；仅含空白的输入按空批注处理。
- `Esc`、点击浮层外部或点击“取消”均关闭浮层且不写入剪贴板。
- 取消后保留原选区；胶囊设置开启时，当前选区继续显示胶囊。
- 同一编辑器同一时刻最多存在一个胶囊或一个批注输入浮层。

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

- 点击“复制”成功后，将完整批注载荷写入系统剪贴板、关闭输入浮层并短暂显示“批注已复制”。
- 成功后保留原选区，但当前选区不立即重新显示胶囊；选区发生变化后才恢复自动胶囊。
- 成功后，右键菜单仍可对同一选区立即再次发起批注。
- 剪贴板写入失败时，输入浮层必须保持打开，批注内容不得丢失，并在浮层内显示“复制失败，请重试”。用户可再次点击“复制”，不弹模态错误框。

### 胶囊设置

#### IDEA 插件逻辑

- 在 `Settings / Preferences > Tools > IDEA Annotation` 提供设置页。
- 设置页首版只提供应用级复选项“显示选区批注胶囊”，默认开启并持久化保存。
- 关闭后立即移除当前胶囊，后续选区不再自动显示胶囊；右键“批注…”保持可用。
- 重新开启后，从下一次选区变化开始恢复自动胶囊。

### 交付与验收

#### IDEA 插件逻辑

- 首版最低支持 IntelliJ IDEA 2026.2，目标构建分支为 `262`。
- 插件应保持跨平台实现，不编写 macOS 专用路径逻辑；首版只要求在当前 macOS + IntelliJ IDEA 2026.2.1 环境完成人工验收。
- 人工验收必须覆盖：单行和多行选区、行号边界、未保存内容、多行及空批注、胶囊定位、右键入口、胶囊开关、成功抑制、取消恢复、复制失败重试，以及不支持场景不显示入口。
- `buildPlugin` 必须生成可从 IDEA 设置页本地安装的 ZIP，安装后能够完成全部人工验收流程。
- 首版不要求格式化单元测试或完整 UI 自动化。

## Implementation Decisions

### 锁定决策

| 决策项 | 决策值 | 来源 |
| --- | --- | --- |
| 技术基线 | IDEA 2026.2、Java 25、Gradle 9.x、IntelliJ Platform Gradle Plugin 2.18.1 | [IntelliJ 平台能力与兼容基线](./wayfinder/01-IntelliJ平台能力与兼容基线.md) |
| 平台 API | 只使用 IntelliJ Platform 公开 API，不依赖内部浮动工具栏 | [IntelliJ 平台能力与兼容基线](./wayfinder/01-IntelliJ平台能力与兼容基线.md) |
| 批注入口 | 所选文本右上角小胶囊 + 编辑器右键菜单，共用输入浮层 | [批注入口与交互流程](./wayfinder/02-批注入口与交互流程.md) |
| 载荷格式 | 固定 Markdown 引用格式，允许空批注，结尾恰好一个换行符 | [批注载荷格式边界](./wayfinder/03-批注载荷格式边界.md) |
| Source | 本地绝对路径 + 1-based 单行或行范围 | [批注来源定位规则](./wayfinder/04-批注来源定位规则.md) |
| 设置 | 应用级胶囊开关，默认开启；右键入口不受影响 | [批注胶囊设置](./wayfinder/07-批注胶囊设置.md) |
| 交付物 | Gradle 源码工程 + 本地安装 ZIP | [支持范围与交付边界](./wayfinder/05-支持范围与交付边界.md) |
| 验收证据 | 当前 macOS IDEA 2026.2.1 沙箱人工验收 | [验收场景与失败反馈](./wayfinder/06-验收场景与失败反馈.md) |
