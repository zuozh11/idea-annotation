# 批注载荷精简与同文合并 1.3.7 PRD

## Solution

Selection Annotation 将来源上下文放在评论之前，来源标记与来源链接保持在同一行，并移除批注载荷底部 footer。多 Caret 选区按预处理后的选区文本分组，相同文本只输出一次代码块，其来源链接合并在同一个来源标记之后。

## Requirements

- 单个来源组使用 `> _Source:_ <link...>`；多个来源组按首次出现顺序使用 `> _Source 1:_ <link...>`、`> _Source 2:_ <link...>`……。
- 同组内的多个来源链接使用一个 ASCII 空格连接，并保持原 Caret 顺序。
- 分组键是 `AnnotationContext` 已完成首尾空白行删除和公共缩进移除后的选区文本。
- 不同文本不得因为相邻、重叠或同为整行选区而合并。
- 同组只要存在一个行内选区，就输出一次该组文本的动态 fenced code block；全部为整行选区时不输出代码块。
- 相邻来源组之间不插入裸 `>` 分隔行或空行。
- 全部来源组结束后保留一个空行，再输出 `**_comment:_**`；非空评论在标记后使用一个 ASCII 空格连接，多行评论的内部内容保持原样。
- 批注载荷不再输出 `---` footer，并以一个换行符结束。
- 全部有效选区均为整行且评论为空时，继续沿用原有紧凑来源链接输出。
- 独立路径链接、链接转义、语言标识和选区文本预处理保持不变。

## Observable Difference

调整前：

````markdown
**_User comment:_**
Please check this result.

> _Source 1:_
> [Example.java (line 42)](/project/src/Example.java)
> ```java
> return value;
> ```
>
> _Source 2:_
> [Example.java (line 45)](/project/src/Example.java)
> ```java
> return value;
> ```

---

````

调整后：

````markdown
> _Source:_ [Example.java (line 42)](/project/src/Example.java) [Example.java (line 45)](/project/src/Example.java)
> ```java
> return value;
> ```

**_comment:_** Please check this result.
````
