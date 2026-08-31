# Source 可点击定位方案

- Type: research
- Status: resolved
- Blocked by: none

## Question

在已确定的批注载荷消费端中，文件绝对路径、单行定位和行范围定位分别支持哪些 Markdown 链接目标；用户给出的文件链接、`路径:行号` 链接和 `路径:起始行-结束行` 链接能否实际跳转，若不能，最小可行格式是什么？

## Resolution

### 验证结果

- 使用独立 Codex 任务 `01a05841-3f86-7cf2-baf2-3911a9766735` 验证以下格式：

  ```markdown
  > [SupplierProductAppService.java (line 198)](/Users/zuozhi/workspace/Lanyou/YYC_SRM/yyc-srm-backend/ly-pd-production/ly-pd-production-service/src/main/java/com/szlanyou/cloud/production/supplierproductcontext/north/local/appservice/SupplierProductAppService.java)
  ```

- 独立任务能够从链接文字识别目标为 `SupplierProductAppService.java` 第 `198` 行，并会在收到检查请求时查看该行及必要上下文。
- 链接目标本身只包含真实绝对文件路径，不包含行号，因此点击只保证打开文件，不保证直接跳到指定行。

结论：采用“真实文件路径作为链接目标、行号作为链接文字的一部分”的方案。它保留文件链接的稳定性，并满足 Agent 对目标行的语义识别；直接点击跳行不作为本版本契约。
