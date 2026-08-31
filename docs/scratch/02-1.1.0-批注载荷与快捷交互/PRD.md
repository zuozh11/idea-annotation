# 批注载荷与快捷交互 1.1.0 PRD

## Solution

Selection Annotation `1.1.0` 将来源链接和所选文本合并为统一的 `Selection`，使用标准 Markdown 代码块保持内容格式，并面向通用 Agent 提供无需专有语法即可理解的文件与行号定位。用户既可通过现有“批注…”入口为单个或多个选区输入一条共享评论，也可通过可配置快捷键直接复制不含评论的载荷；没有文本选区时，快捷键以及编辑器或 Project View 的 `Copy Path` 菜单可以复制独立的文件或目录链接。输入框支持可对调的确认与换行按键。

## Scope

### IDEA 插件范围

- 把单光标和多光标选区统一序列化为一个带来源链接的 Markdown 批注载荷。
- 自动识别 `Selection` 代码块语言，并在无法识别时提供确定性回退。
- 非空评论使用保真代码块；空评论完全省略 `User comment`。
- 支持多个非空选区及一条共享评论。
- 新增可由 IDEA Keymap 配置的直接复制 Action，默认使用 `Option+C`/`Alt+C`。
- 支持从无文本选区的编辑器或 Project View 单选、多选文件/目录复制独立 Markdown 路径链接。
- 在编辑器和 Project View 右键菜单中新增独立的 `Copy Path`/`复制路径` 入口。
- 新增应用级设置页，可对调输入框的确认键和换行键。
- 更新中英文用户文案、版本号和版本说明。

### 涉及角色

- 在 IntelliJ IDEA 中选择代码或文本，并把带来源定位的内容交给 Codex 或其他通用 Agent 的开发人员。

## Out of Scope

- 直接向 Codex、ChatGPT 或其他 Agent 发送批注载荷；本版本仍只写入系统剪贴板。
- 为不同选区分别输入不同评论；多光标批注只支持一条共享评论。
- 用户自定义批注载荷模板、章节名称、Markdown 语法或语言标识映射表。
- 跨编辑器、跨文件选区聚合，以及 Diff 或无稳定本地路径编辑器。
- 批注持久化、历史记录和编号管理。
- 在插件设置页维护另一套快捷键绑定；快捷键统一由 IDEA Keymap 管理。
- 创建或推送 `1.1.0` 标签、上传 Marketplace 或创建 GitHub Release；发布需要实现完成后的独立授权。

## User Stories

1. As an Agent user, I want each selection to combine its source link and exact text, so that any Markdown-capable Agent can understand what code I am referring to.
2. As a developer, I want selected text and comments preserved in code blocks, so that indentation, symbols and line breaks are not changed by Markdown rendering.
3. As a developer, I want to copy several editor selections as one compact annotation, so that I can discuss related locations without repeating the same comment.
4. As a keyboard user, I want a configurable shortcut to copy selected context without opening an input box, so that I can send context with minimal interaction.
5. As a user with a preferred multiline input habit, I want to choose whether `Enter` or `Shift+Enter` confirms the annotation, so that the input follows my normal workflow.
6. As an Agent user, I want to copy the current file or selected Project View item as a compact Markdown link, so that I can reference a file or directory without creating an annotation payload.

## Requirements

### 通用 Selection 批注载荷

#### IDEA 插件逻辑

- 批注载荷使用标准 Markdown，不依赖 Codex、ChatGPT 或其他单一 Agent 的专有指令。
- 批注正文位于一个外层 Markdown block quote 中；每个结构行和内容物理行都显式包含外层引用 marker，不依赖 lazy continuation。
- 正文结束后在 block quote 外固定输出一个 `---` footer，footer 前后各保留一个空行，便于用户连续粘贴多份批注。
- 单个有效选区使用标题 `Selection`；多个有效选区按视觉位置顺序使用 `Selection 1`、`Selection 2`……。
- 每个 `Selection` 依次包含一条来源链接和一个所选文本代码块，不再输出独立的 `Source`、`Selected text` 标题。
- 来源链接文字使用文件名和 1-based 行号：
  - 单行选区：`<文件名> (line <行号>)`。
  - 多行选区：`<文件名> (lines <起始行>-<结束行>)`。
- 来源链接目标只包含文件真实本地绝对路径，不追加行号或行范围。点击只要求打开文件，不承诺直接跳到目标行；Agent 根据链接文字理解目标行。
- 起始行取首个实际选中字符所在行；结束行取最后一个实际选中字符所在行。结束 offset 为排他值，选区末尾换行符不得把下一行计入范围。
- 链接文字和链接目标必须输出为有效 Markdown，同时保持用户可见文件名和真实绝对路径语义。

### Selection 代码块

#### IDEA 插件逻辑

- 所有所选文本都使用 fenced code block，不因文件是否属于编程语言而退回普通引用文本。
- 代码块语言标识按以下顺序确定：
  1. 优先取 IDEA 为当前文件识别的语言 ID，并转换为小写。
  2. 没有可用语言 ID时，取文件扩展名并转换为小写。
  3. 语言 ID 和扩展名均不可用时，不输出语言标识。
- 所选文本除 IDEA `Document` 已统一使用 `\n` 外保持原样，包括缩进、空行、首尾空格和首尾换行。
- 代码围栏使用反引号，长度取至少三个且严格长于所选文本中最长的连续反引号序列，避免内容提前关闭代码块。

### User comment

#### IDEA 插件逻辑

- 输入评论整体去除首尾空白；内部换行、缩进、空格和 Markdown 符号保持原样。
- 非空评论在全部 `Selection` 之后输出一个 `User comment` 章节，并使用不带语言标识的 fenced code block。
- 评论代码围栏长度取至少三个且严格长于评论中最长的连续反引号序列。
- 评论为空或仅包含空白时，整个 `User comment` 章节都不输出，不保留标题、空代码块或占位行。
- 多光标批注的一条非空共享评论只输出一次，不复制到每个 `Selection` 中。

### 单光标载荷格式

#### IDEA 插件逻辑

- 单光标非空评论的标准输出为：

````markdown
> **Selection:**
> [SupplierProductAppService.java (line 198)](/absolute/path/SupplierProductAppService.java)
> ```java
> Optional<Product> result = bySystem.get(code);
> ```
>
> **User comment:**
> ```
> 检查空值处理
> ```

---

````

- 单光标空评论省略 `User comment`：

````markdown
> **Selection:**
> [SupplierProductAppService.java (line 198)](/absolute/path/SupplierProductAppService.java)
> ```java
> Optional<Product> result = bySystem.get(code);
> ```

---

````

### 多光标载荷格式

#### IDEA 插件逻辑

- 同一编辑器至少存在一个非空选区时允许批注；没有选区的 Caret 忽略。
- 有效选区按 `CaretModel.getAllCarets()` 提供的视觉位置顺序排列，不依赖主 Caret 或 Caret 创建顺序。
- 相同、相邻或重叠选区不合并、不去重；每个非空 Caret 分别生成一个编号 `Selection`。
- 所有编号 `Selection` 位于同一个外层 block quote 中，Selection 之间不使用 `---` 分隔。
- 多光标非空共享评论的标准输出为：

````markdown
> **Selection 1:**
> [SupplierProductAppService.java (line 198)](/absolute/path/SupplierProductAppService.java)
> ```java
> Optional<Product> result = bySystem.get(code);
> ```
>
> **Selection 2:**
> [SupplierProductAppService.java (lines 210-212)](/absolute/path/SupplierProductAppService.java)
> ```java
> if (StringUtils.isBlank(code)) {
>     return Optional.empty();
> }
> ```
>
> **User comment:**
> ```
> 检查这两处空值处理是否一致
> ```

---

````

- 多光标空评论和直接复制载荷省略 `User comment`。
- 整份载荷先完整生成，再一次性写入剪贴板；全部成功或全部失败，不产生部分复制。
- 单光标、多光标、空评论、非空评论和直接复制载荷均在最后一个代码围栏后追加同一个 footer：先保留一个空行，再输出 `---`，随后保留一个空行。
- footer 不属于外层 block quote；整份字符串固定以 `---\n\n` 结束，不在多个 `Selection` 之间追加分隔线。

### 批注输入交互

#### IDEA 插件逻辑

- 单光标继续在选区末行下方打开现有内嵌输入框。
- 多光标通过“批注…”入口打开一个共享评论输入框，显示在视觉位置最靠后的有效选区末行下方；所有选区继续保留。
- 默认按键行为：
  - `Enter`：确认并复制批注。
  - `Shift+Enter`：在评论中换行。
- 用户对调设置后：
  - `Shift+Enter`：确认并复制批注。
  - `Enter`：在评论中换行。
- 输入法正在组合候选文字时，`Enter` 只确认候选，不触发插件确认。
- 输入框原“复制”按钮改为“确认”，英文为 `Confirm`；点击仍执行生成载荷并写入剪贴板。
- placeholder 根据当前设置动态展示真实按键说明：
  - 默认中文：`输入批注内容（可为空）… Enter 确认，Shift+Enter 换行`。
  - 对调中文：`输入批注内容（可为空）… Shift+Enter 确认，Enter 换行`。
  - 英文资源包提供对应英文文案。
- `Esc`、点击输入框外部或点击“取消”关闭输入框，不写入剪贴板并保留全部选区。
- 确认成功后关闭输入框、保留全部选区并显示复制成功反馈。
- 写入剪贴板失败时保留输入框、共享评论和全部选区，继续显示复制失败反馈，允许再次按确认键或点击“确认”重试。

### 输入按键设置

#### IDEA 插件逻辑

- 新增应用级 `Selection Annotation` 设置页；设置在所有项目中生效。
- 设置页允许用户交换 `Enter` 与 `Shift+Enter` 的确认和换行职责。
- 设置变化应用于后续打开的批注输入框，并同步改变 placeholder 的按键说明。
- 设置页不提供快捷键编辑器；Action 快捷键继续由 IDEA Keymap 管理。

### 直接复制快捷键

#### IDEA 插件逻辑

- 新增独立的快捷直接复制 Action，与现有打开输入框的“批注…”Action 分离。
- 默认快捷键：
  - macOS：`Option+C`。
  - Windows/Linux：`Alt+C`。
- 用户可以在 IDEA **Settings → Keymap** 中修改或删除快捷键；快捷键冲突由 IDEA 原生冲突提示和 Keymap 配置处理。
- 插件不为 `Option+C`/`Alt+C` 与字符输入或其他 Action 的冲突增加额外兼容逻辑。
- Action 根据当前焦点上下文决定输出：
  - 编辑器聚焦且至少有一个非空选区时，生成不含 `User comment` 的单光标或多光标批注载荷。
  - 编辑器聚焦且没有任何非空选区时，复制当前编辑器文件的独立路径链接，不附加光标行号。
  - Project View 聚焦且选中一个或多个有效本地文件或目录时，按显示顺序复制对应独立路径链接。
- 快捷键只读取当前焦点的数据上下文，不使用其他失焦界面的残留选中项。
- 触发后不打开输入框，直接把对应内容写入剪贴板。
- 复制成功与失败反馈沿用批注确认流程；失败时不产生部分载荷。

### 独立路径链接与 Copy Path 菜单

#### IDEA 插件逻辑

- 独立路径链接的文字使用文件名或目录名，目标使用不带行号的真实本地绝对路径。
- 单项精确输出为一个前导 ASCII 空格、一条 Markdown 链接和一个尾随 ASCII 空格，不包含换行、block quote、`Selection`、`User comment` 或 `---` footer。
- 文件示例：` [SupplierProductAppService.java](/absolute/path/SupplierProductAppService.java) `。
- 目录示例：` [idea-annotation](/Users/zuozhi/workspace/zuozhi/idea-annotation) `。
- Project View 多选支持文件和目录混合选择，按从上到下的显示顺序输出；每项一条链接，链接之间使用恰好一个 `\n`，不插入空行。
- 多项不添加任何前导或尾随空格，精确生成规则为：`links.join("\n")`。最后一条链接后没有换行，结果不附加批注 footer。
- 编辑器右键菜单新增 `Copy Path`，简体中文为“复制路径”：
  - 当前编辑器关联一个具有真实本地绝对路径的文件时显示。
  - 无论编辑器是否存在文本选区，点击都只复制当前文件的独立路径链接，不生成批注载荷。
- Project View 文件或目录右键菜单新增同一个 `Copy Path`/“复制路径”入口：
  - 当前焦点选中一个或多个均具有真实本地绝对路径的文件或目录时显示。
  - 点击复制全部选中项的独立路径链接，文件和目录可以混合多选。
- 如果 Project View 任意选中项无真实本地绝对路径，则快捷键和 `Copy Path` 整体不可用，不部分复制、不静默忽略。
- `Copy Path` 是立即执行的 Action，文案不使用省略号；它与快捷直接复制 Action 分离，菜单始终复制路径链接，快捷键在编辑器有选区时仍复制批注载荷。

### 支持范围与验收

#### IDEA 插件逻辑

- 继续只支持具有真实本地绝对路径的文本编辑器；普通本地文件和具有真实本地路径的 Scratch 可用，未保存修改仍读取当前编辑器缓冲区。
- Diff、无稳定本地路径的编辑器、二进制文件和无非空选区场景不显示或不启用相关入口，且不额外提示。
- 继续以 IntelliJ IDEA `2026.2`、Java/JBR `25`、`sinceBuild = "262"` 且不设置 `untilBuild` 为兼容基线。
- 人工验收至少覆盖：
  - 单行与多行单选区的新 `Selection` 格式。
  - 语言 ID、扩展名和空语言标识三级回退。
  - 所选文本或评论包含反引号围栏时的内容保真。
  - 空评论省略章节与非空评论代码块。
  - 多光标视觉排序、空 Caret 忽略、重叠选区保留和单块编号结构。
  - 多光标共享评论只输出一次。
  - 所有载荷底部只输出一次 `---`，其前后空行和连续粘贴结果符合固定 footer 规则。
  - `Option+C`/`Alt+C` 直接复制及 IDEA Keymap 重绑。
  - 编辑器无选区时快捷复制当前文件链接，且不附加光标行号。
  - Project View 单选、多选及文件和目录混选时的快捷复制与 `Copy Path` 右键菜单。
  - 多选链接按显示顺序逐行输出，无首尾空格、链接间单换行且末尾无换行。
  - 编辑器存在选区时，`Copy Path` 菜单仍只复制文件链接，而快捷键复制批注载荷。
  - 独立路径链接首尾空格、无换行、无批注结构和无 footer 的精确输出。
  - 默认与对调后的确认/换行键、动态 placeholder 和输入法候选确认。
  - 取消、成功和复制失败重试。
- `buildPlugin` 必须生成可从 IDEA 设置页安装的 `1.1.0` ZIP。
- 版本交付时同步更新 Gradle `version`、`RELEASE_NOTES.md` 和 `CHANGELOG.md`；发布标签和 Marketplace 上传不属于本 PRD 的实施授权。

## Implementation Decisions

### 锁定决策

| 决策项 | 决策值 | 来源 |
| --- | --- | --- |
| 版本 | `1.1.0` | 用户确认 |
| 消费端定位 | 面向通用 Agent 的标准、自包含 Markdown；Codex 仅作为当前参考验收 Agent | [批注载荷消费端兼容范围](./wayfinder/01-批注载荷消费端兼容范围.md) |
| 来源链接 | 链接文字包含文件名与行号，目标只包含真实绝对路径，不承诺点击跳行 | [Source 可点击定位方案](./wayfinder/02-Source可点击定位方案.md) |
| 载荷章节 | 删除独立 `Source`、`Selected text`；统一为包含来源链接和代码块的 `Selection` | [Selection 来源链接契约](./wayfinder/04-Selection来源链接契约.md) |
| Selection 代码块 | 语言依次取 IDEA 语言 ID、文件扩展名或空标识；动态增加反引号围栏长度 | [Selected text 代码块与语言标识](./wayfinder/05-Selected-text代码块与语言标识.md) |
| User comment | 非空评论使用无语言代码块；空评论完全省略章节 | [User comment 代码块边界](./wayfinder/06-User-comment代码块边界.md) |
| 直接复制 | 独立 Action，不打开输入框，由 IDEA Keymap 配置 | [快捷键与直接复制入口](./wayfinder/07-快捷键与直接复制入口.md) |
| 输入键 | 应用级设置对调 `Enter`/`Shift+Enter`；按钮和提示使用“确认” | [输入确认与换行设置](./wayfinder/08-输入确认与换行设置.md) |
| 多光标顺序 | 忽略空 Caret，按视觉位置排列，不依赖主 Caret | [多光标选区模型与排序](./wayfinder/09-多光标选区模型与排序.md) |
| 多选区载荷 | 一个外层引用内输出编号 `Selection`，内部不使用分隔线；整份载荷底部追加一次 `---\n\n`，原子复制 | [多选区批注载荷组合规则](./wayfinder/10-多选区批注载荷组合规则.md) |
| 多光标评论 | 一个输入框填写共享评论，在全部 `Selection` 后只输出一次 | [多光标批注交互](./wayfinder/11-多光标批注交互.md) |
| 默认快捷键 | macOS `Option+C`，Windows/Linux `Alt+C`，冲突交由 IDEA Keymap | [默认快捷键平台范围](./wayfinder/12-默认快捷键平台范围.md) |
| 无选区快捷复制 | 编辑器无选区时复制当前文件；Project View 支持单选或多选文件/目录路径链接 | [无选区文件或目录链接复制](./wayfinder/13-无选区文件或目录链接复制.md) |
| Copy Path 菜单 | 编辑器和 Project View 右键菜单提供独立路径复制；Project View 支持文件和目录混合多选 | [文件与编辑器 Copy Path 菜单](./wayfinder/14-文件与编辑器Copy-Path菜单.md) |
| 多选路径格式 | 按 Project View 显示顺序逐行输出，精确格式为 `links.join("\n")`，无首尾空格；任一项无效则整体不可用 | [Project View 多选路径链接复制](./wayfinder/15-Project-View多选路径链接复制.md) |
| 技术与兼容基线 | IDEA 2026.2、Java 25、`sinceBuild = 262`、无 `untilBuild` | [1.0.0 行为基线](../01-1.0.0-IDEA批注插件/PRD.md) / 项目规则 |
