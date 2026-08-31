# Selection 来源链接契约

- Type: grilling
- Status: resolved
- Blocked by: ./02-Source可点击定位方案.md

## Question

单光标与多光标批注中，来源链接和所选文本应继续分为 `Source`、`Selected text` 章节，还是合并为 `Selection`；链接文字和目标如何表达文件与行范围？

## Resolution

### 锁定格式

- 删除独立的 `Source` 和 `Selected text` 章节；每个选区改为一个同时包含来源链接和所选文本代码块的 `Selection` 章节。
- 单光标标题固定为 `Selection`；多光标标题按视觉顺序使用 `Selection 1`、`Selection 2`……。
- 每个 `Selection` 只输出一条来源链接，不同时输出文件、单行和行范围三条链接；链接后直接输出该选区的代码块。
- 链接目标固定为不带行号的本地绝对文件路径。
- 链接文字使用文件名，并按 1-based 选区行范围追加定位信息：
  - 单行选区：`<文件名> (line <行号>)`。
  - 多行选区：`<文件名> (lines <起始行>-<结束行>)`。
- 起止行计算继续沿用 `1.0.0` 规则：结束 offset 为排他值，按最后一个实际选中字符确定结束行，选区末尾换行符不得把下一行计入范围。
- 点击链接只要求打开目标文件，不要求直接跳到指定行；Agent 根据链接文字识别需要检查的行号或行范围。

### 示例

````markdown
> **Selection:**
> [SupplierProductAppService.java (line 198)](/absolute/path/SupplierProductAppService.java)
> ```java
> Optional<Product> result = bySystem.get(code);
> ```
````

以上由用户确认；统一 `Selection` 结构覆盖此前独立 `Source`、`Selected text` 标题的方案。
