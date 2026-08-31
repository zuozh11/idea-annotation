# CONTEXT 格式

本文件只定义项目采用的 Context 布局应怎样编写。任务运行时如何选择 Context 由项目知识协议说明。

入口为 `docs/CONTEXT.md`：

```markdown
---
description: 订单服务
---
# Ordering

## Language

**Order**:
客户确认购买后形成的交易意向。
_Avoid_: Purchase
```

## Frontmatter

每个 `CONTEXT.md` 文件开头必须提供单行简短说明：

```yaml
---
description: 供应商服务
---
```

`description` 使用非空单行普通文本，直接填写便于选择的简洁 Context 显示名称，例如「功能平移服务」「生产寻源服务」。不填写职责摘要、文件路径或技术栈。当前不添加其他 Frontmatter 字段。

运行时 `scope` 以单行 JSON 返回单 Context 模式和 RULE 候选；`load` 自动加载 `docs/CONTEXT.md`，不能传 `--context`。

## 术语写法

- 定义项目特有概念，不记录通用编程术语。
- 同一概念选择一个统一名称，把应避免的别名写在 `_Avoid_` 中。
- 定义保持一到两句话；说明它是什么，不堆砌操作步骤。
- 术语自然聚类时再使用子标题，不为形式完整制造章节。

修改后运行：

```bash
node docs/agents/project-knowledge.mjs validate-context
```
