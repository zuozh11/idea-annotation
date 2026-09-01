# 移除批注载荷首行空白 1.3.3 PRD

## Solution

Selection Annotation 生成批注载荷时，第一个选区组必须从载荷的第一个字符开始，不再预置换行符。粘贴结果因此不再以空白行开头。

## Requirements

- 含行内选区、非空评论或混合选区的批注载荷，首字符必须是第一个选区组的 `>`。
- 仅删除批注载荷开头原有的一个 `\n`。
- 不改变多 Caret 选区组分隔、`_User comment:_` 上方空白行、评论首尾处理、footer 和载荷结尾。
- 纯路径链接以及“全部整行且空评论”的链接结果保持现有格式。

## Observable Difference

调整前：

```text
\n> [Example.java (line 42)](/project/src/Example.java)
```

调整后：

```text
> [Example.java (line 42)](/project/src/Example.java)
```
