# 批注载荷与快捷交互方案地图

## Destination

形成一套可直接进入 `to-prd` 的 Selection Annotation `1.1.0` 完整决策：锁定带来源链接的 `Selection`、代码块评论、批注或独立路径链接的快捷复制、输入按键设置与多光标批注载荷的产品行为。

## Notes

- 本地图只规划，不实施；地图完成后在本目录生成 `PRD.md`。
- 每轮加载项目 Context，统一使用“批注”和“批注载荷”。
- 以现有 `docs/scratch/01-1.0.0-IDEA批注插件/PRD.md` 和当前插件行为为基线，只规划 `1.1.0` 新增或改变的行为。
- 用户已指定的基线：版本号为 `1.1.0`；来源链接与所选文本合并为 `Selection`，代码块语言必须自动识别并回退；非空 `User comment` 使用无语言标识代码块，空批注不输出整个章节；默认快捷键为 `Option+C`，有选区时直接复制不含 `User comment` 的载荷，无选区或文件树单选时复制独立文件/目录链接；输入框默认 `Enter` 确认、`Shift+Enter` 换行，设置页可交换两者，并在 placeholder 中展示当前按键说明；多光标使用一个包含多个编号 `Selection` 的载荷块；每份批注载荷底部固定追加一次 `---` 和空行。
- Markdown 是否可点击、行号目标是否可跳转以及嵌套语法是否稳定，必须以确定的批注载荷消费端实际行为为依据，不能只凭 CommonMark 语法有效推断。
- 查找平台 API、Markdown 规则和当前实现事实由 Agent 完成；产品选择通过 grilling 或 prototype 交给用户决定。

## Decisions so far

- [批注载荷消费端兼容范围](./wayfinder/01-批注载荷消费端兼容范围.md)：格式面向通用 Agent，使用标准、自包含、可读的 Markdown；Codex 是当前参考验收 Agent，不是专用消费端。
- [Source 可点击定位方案](./wayfinder/02-Source可点击定位方案.md)：链接目标只保留真实绝对文件路径，行号由链接文字表达；Agent 能识别目标行，但点击不保证直接跳行。
- [批注载荷 Markdown 层级与转义](./wayfinder/03-批注载荷Markdown层级与转义.md)：全量显式引用标记维持结构，代码围栏动态避开内容反引号，整份载荷在引用外以 `---` 和空行结束。
- [Selection 来源链接契约](./wayfinder/04-Selection来源链接契约.md)：删除独立 `Source`、`Selected text` 标题；每个 `Selection` 依次包含一条带行号文字的绝对路径链接和对应代码块。
- [Selected text 代码块与语言标识](./wayfinder/05-Selected-text代码块与语言标识.md)：所有选区使用代码块，语言标识依次取 IDEA 语言 ID、文件扩展名或空标识，并用动态长度反引号围栏保持内容完整。
- [User comment 代码块边界](./wayfinder/06-User-comment代码块边界.md)：非空评论使用无语言标识代码块并动态选择围栏长度；空评论及 `Option+C` 直接复制完全省略 `User comment` 章节。
- [快捷键与直接复制入口](./wayfinder/07-快捷键与直接复制入口.md)：新增独立的直接复制 Action，通过 IDEA Keymap 提供可配置快捷键，不打开输入框并复用复制反馈。
- [输入确认与换行设置](./wayfinder/08-输入确认与换行设置.md)：应用级设置可对调确认与换行按键，placeholder 动态说明当前映射，按钮改称“确认”，并避开输入法候选确认。
- [多光标选区模型与排序](./wayfinder/09-多光标选区模型与排序.md)：全部 Caret 按平台提供的视觉位置排序，逐个读取独立选区；不依赖定义不稳定的主 Caret 或创建顺序。
- [多选区批注载荷组合规则](./wayfinder/10-多选区批注载荷组合规则.md)：忽略空 Caret，按视觉顺序在同一外层引用中输出编号 `Selection`；内部不分块，整份载荷底部固定追加一次 `---` 并一次性复制。
- [多光标批注交互](./wayfinder/11-多光标批注交互.md)：采用共享一条评论方案，只打开一个位于最后选区下方的输入框；非空评论在全部 `Selection` 后只输出一次，空评论省略该章节。
- [默认快捷键平台范围](./wayfinder/12-默认快捷键平台范围.md)：直接复制在 macOS 默认使用 `Option+C`、Windows/Linux 使用 `Alt+C`，并统一交由 IDEA Keymap 配置和处理冲突。
- [无选区文件或目录链接复制](./wayfinder/13-无选区文件或目录链接复制.md)：编辑器无选区时复制当前文件链接；Project View 支持一个或多个本地文件/目录，单项首尾各一个空格，多项无首尾空格并以单个换行分隔。
- [文件与编辑器 Copy Path 菜单](./wayfinder/14-文件与编辑器Copy-Path菜单.md)：编辑器和 Project View 右键菜单新增独立 `Copy Path`；编辑器始终复制当前文件链接，Project View 支持混合多选。
- [Project View 多选路径链接复制](./wayfinder/15-Project-View多选路径链接复制.md)：文件和目录按显示顺序逐行输出，精确格式为 `links.join("\n")`，无首尾空格；任一项无本地路径则整体不可用。

## Not yet specified

无；当前可见问题均已形成决策票并解决。

## Out of scope

- 直接把批注发送到 Codex、ChatGPT 或其他聊天窗口；本版本仍只写入系统剪贴板。
- 用户自定义整套批注载荷模板、章节名称或分隔符；本版本只定义一套固定格式。
- 批注持久化、历史记录、跨编辑器或跨文件聚合。
- 为 `1.1.0` 创建或推送标签、上传 Marketplace 或创建 GitHub Release；这些属于实现完成后的独立发布授权。
