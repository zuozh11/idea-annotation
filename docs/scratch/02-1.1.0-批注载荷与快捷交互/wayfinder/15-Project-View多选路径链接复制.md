# Project View 多选路径链接复制

- Type: grilling
- Status: resolved
- Blocked by: ./13-无选区文件或目录链接复制.md, ./14-文件与编辑器Copy-Path菜单.md

## Question

Project View 同时选中多个文件或目录时，`Option+C`/`Alt+C` 和 `Copy Path` 右键菜单应按什么顺序输出链接，链接之间及整份字符串首尾使用什么精确空白规则；混合选中有效与无本地路径项目时如何处理？

## Resolution

- Project View 支持同时选择文件和目录，也支持两者混合多选。
- 链接按 Project View 从上到下的显示顺序输出。
- 每个选中项输出一条独立 Markdown 链接，链接之间使用恰好一个 `\n`，不插入空行。
- 多选结果不添加任何前导或尾随空格，精确生成规则为：`links.join("\n")`。
- 最后一条链接后没有换行；结果不包含 block quote、`Selection`、`User comment` 或 `---` footer。
- `Option+C`/`Alt+C` 与 Project View 右键菜单的 `Copy Path` 使用完全相同的多选结果。
- 如果任意选中项没有真实本地绝对路径，整个 Action 不可用；不进行部分复制、不静默忽略无效项。

以上由用户确认；多选无首尾空格的规则覆盖此前 `" " + links.join("\n") + " "` 的方案。单个链接仍保留首尾各一个 ASCII 空格。
