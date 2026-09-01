# 批注评论前置 1.3.4 PRD

## Solution

Selection Annotation 生成批注载荷时，先单独输出一行 `_User comment:_`，从下一行开始输出评论内容，再输出带 `_Source:_` 标记的全部选区上下文，使接收方先读到用户意图，再读取明确标识的来源链接和代码。底部 `---` footer 继续位于末尾。

## Requirements

- 非空评论载荷使用 `_User comment:_\n<comment>\n\n<selection groups>\n---\n\n`；多行评论保持原样。
- 载荷中存在行内选区且评论为空时，使用 `_User comment:_\n\n<selection groups>\n---\n\n`。
- 评论继续整体执行 `strip()`；内部换行、缩进、空格、Markdown 与资源链接保持原样。
- 单个选区组必须先输出 `> _Source:_`，再输出来源链接和按需存在的代码块。
- 多 Caret 的选区组继续按当前视觉顺序输出，相邻组之间继续使用一个裸 `>` 行；各组按顺序输出 `> _Source 1:_`、`> _Source 2:_`……标记。
- 整行选区组继续只输出来源链接；行内选区组继续输出来源链接与动态 fenced code block。
- footer 继续只输出一次，整份载荷继续以 `---\n\n` 结束。
- 全部整行且评论为空时，继续只输出紧凑来源链接，不增加评论标记或 footer。
- 独立路径链接、链接转义、语言标识和选区文本处理保持不变。

## Observable Difference

调整前：

````markdown
> [Example.java (lines 42-45)](/project/src/Example.java)
> ```java
> return value;
> ```

_User comment:_

Please check this result.

---

````

调整后：

````markdown
_User comment:_
Please check this result.

> _Source:_
> [Example.java (lines 42-45)](/project/src/Example.java)
> ```java
> return value;
> ```

---

````
