# 选区首行缩进 1.3.9 PRD

## Solution

Selection Annotation 在处理多行选区时，结合原始文档计算所有选中物理行共有的源缩进。首行从内容中间开始时，不复制该行未选中的文字，但以空格保留未选内容在公共缩进之后占据的相对字符位置。

## Requirements

- 当选区起点位于首行前导缩进区域，或正好位于首个非空字符时，公共缩进计算必须包含该行在选区之外的真实前导缩进。
- 当选区起点已经进入首行实际内容时，首行仍以源文件的真实前导缩进参与公共前缀计算。
- 首行中未选中的非缩进内容不得进入批注载荷，但其在公共缩进之后占据的字符位置必须转换为等长 ASCII 空格，使所选片段保持相对列位置。
- 仍然只把 ASCII 空格和 Tab 视为缩进字符，并按字符前缀计算公共缩进。
- 删除公共缩进后，必须保留各行额外缩进、文本内容、行顺序及内部换行。
- 首尾空白行、多 Caret 独立预处理、来源行号和整行选区识别保持不变。

## Observable Difference

源代码为：

```java
                .set(first)
                .set(second)
                .set(third)
```

当选区从首行的 `.` 开始时，调整前输出：

```java
.set(first)
                .set(second)
                .set(third)
```

调整后输出：

```java
.set(first)
.set(second)
.set(third)
```

当首行从 `placeholder` 的 `c` 开始、后续行完整选中时，调整后输出：

```java
          ceholder,
Consumer<String> showError,
Color background,
Color foreground
```
