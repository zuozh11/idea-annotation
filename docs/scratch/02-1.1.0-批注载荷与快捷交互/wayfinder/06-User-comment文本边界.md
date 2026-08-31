# User comment 文本边界

- Type: grilling
- Status: resolved
- Blocked by: ./03-批注载荷Markdown层级与转义.md

## Question

`User comment` 应放在引用块内还是外，使用标题、代码块还是普通文本；空评论时是否保留标记？

## Resolution

- `_User comment:_` 固定输出在全部选区引用内容之后，并位于 block quote 之外。
- `_User comment:_` 始终输出，包括输入为空、仅含空白和 `Option+C`/`Alt+C` 直接复制选区载荷。
- 评论输入继续整体去除首尾空白，内部换行、缩进、空格和 Markdown 符号保持原样。
- 非空评论不使用 block quote 或 fenced code block；`_User comment:_` 后先保留一个空行，再按原始多行文本输出评论，评论后保留一个空行再输出 `---`。
- 空评论只输出 `_User comment:_`，其后不添加占位符、空代码块或额外文字。
- 多光标共享评论只在全部选区之后输出一次。
- 空评论标记之后按空模板保留三个空行，再输出 `---` footer；footer 后保留一个空行。

### 非空评论示例

````markdown
> [AnnotationEditorService.java (line 71)](/Users/zuozhi/workspace/zuozhi/idea-annotation/src/main/java/com/zuozhi/ideaannotation/AnnotationEditorService.java)
> ```java
> INPUT_SIZE
> ```
_User comment:_

123123

---

````

### 空评论示例尾部

```markdown
_User comment:_



---

```

以上由用户确认；该规则覆盖此前“空评论隐藏整个章节”和“非空评论使用代码块”的方案。
