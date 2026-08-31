# 批注载荷 Markdown 层级与转义

- Type: research
- Status: resolved
- Blocked by: ./01-批注载荷消费端兼容范围.md

## Question

在已确定的消费端中，单层外部引用内的 fenced code block、双层 `User comment` 引用、多行空白以及所选文本自身包含反引号围栏时分别如何解析；要保持内容完整和结构稳定，批注载荷需要遵循什么最小转义规则？

## Resolution

### CommonMark 结构规则

- 外层批注继续使用 block quote。为避免依赖 Markdown 的 lazy continuation，章节标题、围栏起止行、代码内容和内部空行的每个物理行都显式添加外层 `> ` 标记。
- fenced code block 可以位于 block quote 内，但围栏和全部代码内容都必须属于该 block quote；代码块内容按字面文本处理，不再解析其中的 Markdown。
- 代码围栏至少包含三个连续反引号。为避免所选文本自身包含反引号围栏而提前关闭代码块，实际围栏长度取“三个”和“所选文本中最长连续反引号长度加一”中的较大值。
- 文件链接必须保持为有效的标准 Markdown inline link。链接文字中的 Markdown 特殊字符和绝对路径作为 link destination 时需要按 CommonMark 规则输出；具体字符编码属于 formatter 实现细节，不能改变用户可见的文件名或真实绝对路径语义。
- 整份批注内容结束后，在外层 block quote 之外输出固定的 `---` footer；footer 前后各保留一个空行，使用户连续粘贴多份批注时仍形成独立的 Markdown 分隔线。

### 依据

- [CommonMark 0.31.2 · Block quotes](https://spec.commonmark.org/0.31.2/#block-quotes)：block quote marker 可以逐行包裹任意 block；fenced code block 的后续行不能省略引用标记。
- [CommonMark 0.31.2 · Fenced code blocks](https://spec.commonmark.org/0.31.2/#fenced-code-blocks)：围栏至少三个反引号或波浪号，关闭围栏必须与开始围栏同类型且长度不少于开始围栏，代码内容按字面文本处理。

结论：新载荷可以只使用标准 Markdown 实现外层引用、代码块和底部水平分隔线，不需要任何 Agent 专用语法；动态增加反引号围栏长度即可为所选文本和非空批注提供确定性回退。
