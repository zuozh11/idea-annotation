# User comment 代码块边界

- Type: grilling
- Status: resolved
- Blocked by: ./03-批注载荷Markdown层级与转义.md

## Question

`User comment` 应使用双层引用还是代码块；非空多行批注、内部空行、仅空白批注和批注自身包含 Markdown 或反引号时分别如何保持原始格式？

## Resolution

### 决策树

- **结构选择**：放弃双层引用，`User comment` 统一使用位于批注外层 block quote 内的无语言标识 fenced code block，不再叠加第二层 block quote。
- **内容保真**：批注继续只去除整个输入的首尾空白；内部换行、缩进、空格和 Markdown 符号按字面文本保留，不继续作为 Markdown 解析。
- **围栏回退**：围栏使用反引号，长度取至少三个且严格长于批注中最长连续反引号序列。
- **空批注**：批注经首尾去空白后为空时，整个 `User comment` 章节都不输出，不保留标题、空代码块或占位行。
- **直接复制**：`Option+C` 直接复制空批注时应用同一规则，每个批注块只输出 `Source` 和 `Selected text`。

- **非空批注**：每个物理内容行都显式保留外层 `> ` marker：

  ````markdown
  > **User comment:**
  > ```
  > 这是我的评论
  >   保留缩进
  > ```
  ````

空批注规则由用户后续确认，并覆盖此前“输出带空内容行的空代码块”决策。
