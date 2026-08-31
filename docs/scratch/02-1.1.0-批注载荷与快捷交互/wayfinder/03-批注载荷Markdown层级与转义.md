# 批注载荷 Markdown 层级与转义

- Type: research
- Status: resolved
- Blocked by: ./01-批注载荷消费端兼容范围.md

## Question

在已确定的消费端中，来源链接与 fenced code block 如何稳定放入引用块，普通评论与底部 footer 如何退出引用块，以及所选文本自身包含反引号围栏时如何保持结构？

## Resolution

### CommonMark 结构规则

- 每个选区的来源链接、围栏起止行、代码内容和内部空行都显式添加外层 `> ` 标记，不依赖 Markdown 的 lazy continuation。
- fenced code block 可以位于 block quote 内，但围栏和全部代码内容都必须属于该 block quote；代码块内容按字面文本处理，不再解析其中的 Markdown。
- 代码围栏至少包含三个连续反引号。为避免所选文本自身包含反引号围栏而提前关闭代码块，实际围栏长度取“三个”和“所选文本中最长连续反引号长度加一”中的较大值。
- 文件链接必须保持为有效的标准 Markdown inline link。链接文字中的 Markdown 特殊字符和绝对路径作为 link destination 时需要按 CommonMark 规则输出；具体字符编码属于 formatter 实现细节，不能改变用户可见的文件名或真实绝对路径语义。
- `_User comment:_`、非空评论文本和 `---` footer 均位于选区 block quote 之外。非空评论与标记、footer 之间各保留一个空行；空评论按空模板保留三个空行后输出 footer；footer 后保留一个空行。

### 依据

- [CommonMark 0.31.2 · Block quotes](https://spec.commonmark.org/0.31.2/#block-quotes)：block quote marker 可以逐行包裹任意 block；fenced code block 的后续行不能省略引用标记。
- [CommonMark 0.31.2 · Fenced code blocks](https://spec.commonmark.org/0.31.2/#fenced-code-blocks)：围栏至少三个反引号或波浪号，关闭围栏必须与开始围栏同类型且长度不少于开始围栏，代码内容按字面文本处理。

结论：新载荷使用标准 Markdown 的引用内来源链接与代码块、引用外普通评论和底部水平分隔线，不需要 Agent 专用语法；动态增加反引号围栏长度只用于所选文本代码块。
