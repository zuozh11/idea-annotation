# 文件与编辑器 Copy Path 菜单

- Type: grilling
- Status: resolved
- Blocked by: ./13-无选区文件或目录链接复制.md

## Question

编辑器和 Project View 的右键菜单是否需要提供独立的路径链接复制入口；它与有选区时 `Option+C`/`Alt+C` 的批注载荷行为如何区分？

## Resolution

- 新增独立的 `Copy Path` Action；默认英文文案为 `Copy Path`，简体中文为 `复制路径`，不使用省略号。
- Action 注册到编辑器右键菜单：当前编辑器关联一个具有真实本地绝对路径的文件时显示。无论编辑器是否存在文本选区，点击都只复制当前文件的独立 Markdown 链接，不生成批注载荷。
- Action 注册到 Project View 文件/目录右键菜单：当前焦点选中一个或多个均具有真实本地绝对路径的文件或目录时显示，支持文件与目录混合多选。
- 多选时按 Project View 显示顺序输出，每项一条链接并用一个 `\n` 分隔；若任一项无真实本地绝对路径则不显示或不启用入口。
- 输出精确格式与无选区快捷复制相同：单项链接首尾各一个 ASCII 空格；多项不加首尾空格，只在链接之间包含一个换行；不包含 block quote、`Selection`、`User comment` 或 `---` footer。
- `Copy Path` 菜单 Action 与快捷键直接复制 Action 职责分离：菜单始终复制路径链接；快捷键在编辑器存在非空选区时仍优先复制批注载荷。

以上由用户确认。
