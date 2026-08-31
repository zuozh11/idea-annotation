# Selected text 代码块与语言标识

- Type: grilling
- Status: resolved
- Blocked by: ./03-批注载荷Markdown层级与转义.md

## Question

`Selected text` 应如何选择代码块语言标识，无法识别语言时使用空标识还是固定回退标识，非代码文本文件如何处理，以及为避免所选文本中的围栏破坏载荷应采用哪种已验证规则？

## Resolution

### 决策树

- **统一代码块**：所有 `Selected text` 都使用 fenced code block，不因文件是否属于编程语言而退回普通引用文本。
- **语言标识来源**：
  1. 优先使用 IDEA 为当前文件识别的语言 ID，并转换为通用的小写形式。
  2. IDEA 没有可用语言 ID 时，使用当前文件扩展名的小写形式。
  3. 语言 ID 和扩展名都不可用时，输出不带 info string 的 fenced code block。
- **内容保真**：所选文本继续保留 IDEA `Document` 中的原始空格、缩进、空行、首尾空白和换行；外层 block quote marker 和代码围栏属于载荷结构，不属于所选文本。
- **围栏回退**：围栏使用反引号，长度取至少三个且严格长于所选文本内最长连续反引号序列，避免所选文本提前关闭代码块。

以上由用户确认。
