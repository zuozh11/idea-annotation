# JetBrains Marketplace 发布指南

## 发布模型

- 插件 ID：`com.zuozhi.ideaannotation`，发布后不可变更。
- Marketplace 名称：`Selection Annotation`。原名称含 `IDEA`，不符合 Marketplace 对插件名称不得包含 JetBrains 产品名称的审核规则。
- 当前版本：`1.3.5`；最低兼容构建：`262`；不设置 `until-build`。
- 首次版本由手工工作流生成签名 ZIP 和 GitHub Release，再由维护者在 Marketplace 页面创建插件条目并上传该 ZIP。
- Marketplace 条目存在后，后续稳定版本由与 Gradle `version` 完全一致的标签自动发布，例如 `1.0.2`。
- 普通 `main` 推送不运行 CI；Pull Request 只构建插件，稳定标签在同一 Runner 内跳过测试和 Plugin Verifier，并依次构建、签名和上传，避免额外下载约 945 MB 的校验依赖以及跨 Runner 重复构建。
- **Build** 工作流可在 `main` 上手工运行一次无缓存构建，用于重建默认分支共享 Gradle 缓存；它不签名、不上传 Marketplace，也不创建 GitHub Release。
- `1.0.x` 用于缺陷修复；新增向后兼容功能使用 `1.1.0`。

> GitHub Actions 构建、签名和 `publishPlugin` 成功只证明 Marketplace 接受了上传。JetBrains 仍会审核新插件和每个更新，Marketplace 后台状态才是是否已公开上架的最终证据。

## 一次性准备

### 1. 准备 Marketplace 账户

1. 登录 [JetBrains Marketplace](https://plugins.jetbrains.com/)。
2. 接受 JetBrains Marketplace Developer Agreement，创建 Vendor profile，并填写 trader/non-trader 状态。
3. 确认公开的 vendor 信息：
   - 网站：`https://github.com/zuozh11`
   - 邮箱：`zuozhi22@outlook.com`
4. 决定 Developer EULA。Marketplace 要求每个插件提供许可证；当前仓库为私有仓库，不应在未公开源码的情况下选择开源许可证。

### 2. 生成签名材料

在仓库之外的受限目录操作，不要把生成文件放进项目：

```bash
umask 077
mkdir -p /安全路径/selection-annotation-signing
cd /安全路径/selection-annotation-signing

openssl genpkey \
  -aes-256-cbc \
  -algorithm RSA \
  -out private_encrypted.pem \
  -pkeyopt rsa_keygen_bits:4096

openssl rsa \
  -in private_encrypted.pem \
  -aes256 \
  -traditional \
  -out private.pem

openssl req \
  -key private.pem \
  -new \
  -x509 \
  -days 365 \
  -out chain.crt
```

保存 `private.pem` 的输出密码。`private_encrypted.pem` 只用于转换，确认 `private.pem` 可用后可从该安全目录删除。

### 3. 配置 GitHub Actions Secrets

在私有仓库的 **Settings → Secrets and variables → Actions** 创建：

| Secret | 内容 |
| --- | --- |
| `CERTIFICATE_CHAIN` | `chain.crt` 的完整 PEM 内容 |
| `PRIVATE_KEY` | `private.pem` 的完整 PEM 内容 |
| `PRIVATE_KEY_PASSWORD` | `private.pem` 的密码 |
| `PUBLISH_TOKEN` | Marketplace Profile → My Tokens 创建的 Personal Access Token |

也可在仓库目录使用 GitHub CLI；命令会从标准输入读取，不把内容写入 Git：

```bash
gh secret set CERTIFICATE_CHAIN < /安全路径/selection-annotation-signing/chain.crt
gh secret set PRIVATE_KEY < /安全路径/selection-annotation-signing/private.pem
gh secret set PRIVATE_KEY_PASSWORD
gh secret set PUBLISH_TOKEN
```

不得提交 PEM、密码、Token、包含这些值的 `.env` 或本地 Gradle 配置。证书到期或凭据泄露时，在 Marketplace/GitHub 撤销或替换对应凭据后再发布。

## 首次上架 1.0.1

首次插件条目必须在 Marketplace 页面人工创建；`publishPlugin` 只用于已有条目的后续版本。

1. 通过 Pull Request 的 **Build** 工作流确认插件可以构建后合入 `main`；直接推送 `main` 不运行 CI。
2. 在 GitHub **Actions → Release → Run workflow** 中选择 `main` 手工运行。工作流通过后会自动创建 `1.0.1` 标签和 GitHub Release，并附带签名 ZIP；工作流使用仓库 `GITHUB_TOKEN` 创建标签，不会递归触发标签发布流程。
3. 从 GitHub Release 或工作流产物 `selection-annotation-1.0.1-signed` 下载 `*-signed.zip`。
4. 在 Marketplace 选择 **Upload plugin**：
   - 选择 Vendor profile；
   - 上传签名 ZIP；
   - 选择真实适用的标签和默认渠道；
   - 选择 Developer EULA；
   - 根据需要设置 Hidden；
   - 提交审核。
5. 在 Marketplace 后台记录插件页面、版本、上传时间与审核状态。`1.0.1` GitHub Release 只证明签名发布物已生成，不证明 Marketplace 已上传或审核通过。

## 后续自动发布

以 `1.0.2` 为例：

1. 在 `build.gradle.kts` 更新 `version = "1.0.2"`。
2. 把当前版本说明写入 `RELEASE_NOTES.md`，并把同一版本追加到 `CHANGELOG.md`。
3. 合入并推送 `main`；普通 `main` 推送不触发 Build 工作流，无需等待。
4. 经人工确认后，立即在同一提交创建并推送同版本标签：

   ```bash
   git tag 1.0.2
   git push origin 1.0.2
   ```

5. **Release** 工作流将：
   1. 校验标签与 Gradle 版本完全一致；
   2. 跳过测试和 Plugin Verifier，执行 `buildPlugin` 和 `signPlugin`；
   3. 上传签名包为 GitHub Actions artifact；
   4. 在同一 Runner 执行 `publishPlugin`，复用刚生成的签名包；
   5. 仅在 Marketplace 上传成功后创建同名 GitHub Release 并附加签名 ZIP。
6. 进入 Marketplace 后台记录本次更新的真实审核状态。审核通过并公开可见前，不得宣称“已上架”。

## 重建 Gradle 缓存

依赖或构建配置明显变化，或缓存仍包含已移除的 Plugin Verifier 依赖时：

1. 将最新配置推送到 `main`。
2. 在 GitHub **Actions → Build → Run workflow** 中选择 `main` 手工运行。
3. 手工任务不读取旧 Gradle 缓存，完成 `buildPlugin -x test` 后将新的精简缓存写入默认分支作用域。
4. 后续 Pull Request 和稳定标签可以读取该共享缓存；普通 `main` 推送仍不运行 CI。

## 失败处理

| 失败位置 | 结果 | 处理 |
| --- | --- | --- |
| 版本或标签校验 | 未签名、未上传、无 GitHub Release | 修正 `version` 或创建正确的新标签；不要移动已用于发布的标签 |
| 构建或签名 | 未上传、无 GitHub Release | 查看对应 Job 日志，修复后发布新提交和标签 |
| 缺少签名或 Token Secret | 未上传、无 GitHub Release | 补齐或轮换 Secret 后重新运行 |
| `publishPlugin` 明确失败 | 无 GitHub Release | 按错误修正；如果响应含糊，先查 Marketplace 后台是否已收到该版本，避免重复上传 |
| 首次签名成功，GitHub Release 失败 | Marketplace 尚未上传，GitHub Release 缺失 | 只重新运行失败的 `initial-github-release` job |
| Marketplace 上传成功，GitHub Release 失败 | Marketplace 进入审核，GitHub Release 缺失 | 只重新运行失败的 `github-release-after-publish` job，不重复执行 Marketplace 上传 |
| JetBrains 审核未通过 | 版本未公开 | 按 Marketplace 反馈处理，并保留后台状态和沟通记录；构建成功不能覆盖审核结论 |

## 发布证据

每次发布交接至少记录：

| 证据 | 证明范围 |
| --- | --- |
| Pull Request Build 工作流 URL 与结论（如有） | 合入前的对应提交已成功构建；不包含测试或 Plugin Verifier，直接推送 `main` 时没有此证据 |
| Release 工作流 `package` job 结论 | 标签提交已完成构建和签名；Release 不运行测试或 Plugin Verifier |
| 首次 Release 工作流 URL 与 `initial-github-release` job 结论 | `1.0.1` 签名 ZIP 和 GitHub Release 已生成或失败 |
| 后续 Release 工作流 URL、`package` 中的 `Publish plugin` step 与 `github-release-after-publish` job 结论 | 同一签名包的 Marketplace 上传请求与 GitHub Release 创建成功或失败 |
| GitHub Release URL | 对应签名 ZIP 已形成 GitHub 发布物；不证明 Marketplace 状态 |
| Marketplace 插件/版本页面 URL、状态、记录时间 | JetBrains 当前审核或公开状态，最终发布证据 |

### 2026-09-03 1.3.5 当前状态

- Pull Request [#1](https://github.com/zuozh11/idea-annotation/pull/1) 已合入 `main`；发布准备提交 `5a2b316` 已推送到 `main`，稳定标签 `1.3.5` 指向该提交并已推送。
- [Release #33662436093](https://github.com/zuozh11/idea-annotation/actions/runs/33662436093) 已完成版本校验、构建、签名、Marketplace 上传和 GitHub Release 创建；`Build signed package` 与 `Create GitHub Release after Marketplace upload` job 均成功。
- [GitHub Release 1.3.5](https://github.com/zuozh11/idea-annotation/releases/tag/1.3.5) 已创建，正式资产为 `idea-annotation-1.3.5-signed.zip`，GitHub 记录的 SHA-256 为 `e518e1a2f36d6de42a794ad785fc760962ec32cd916c455ded692bccc1b248c5`。
- Marketplace `Publish plugin` step 已成功；2026-09-03 01:41 CST 公共 API 显示插件 `hasUnapprovedUpdate=true`，已批准并列出的最新公开版本为 `1.3.4`。这证明 `1.3.5` 上传已被 Marketplace 接受但仍待 JetBrains 审核，不代表已经公开上架。
- 本次未要求安装，未替换正式 IntelliJ IDEA 用户插件目录中的现有版本。

### 2026-09-01 1.3.4 当前状态

- 用户在 IDEA 2026.2 sandbox 中迭代确认最终批注载荷格式，并明确要求发布 `1.3.4`。
- 提交 `3b4b3c1` 已推送到 `main`，稳定标签 `1.3.4` 指向该提交并已推送。
- [Release #33478742262](https://github.com/zuozh11/idea-annotation/actions/runs/33478742262) 已完成版本校验、构建、签名、Marketplace 上传和 GitHub Release 创建；`Build signed package` 与 `Create GitHub Release after Marketplace upload` job 均成功。
- [GitHub Release 1.3.4](https://github.com/zuozh11/idea-annotation/releases/tag/1.3.4) 已创建，正式资产为 `idea-annotation-1.3.4-signed.zip`。
- Marketplace `publishPlugin` 已成功；2026-09-01 14:45 CST 公共 API 显示插件 `hasUnapprovedUpdate=true`，已批准并列出的最新公开版本为 `1.3.3`。这证明 `1.3.4` 上传已被 Marketplace 接受但仍待 JetBrains 审核，不代表已经公开上架。
- GitHub Release 中的签名版 `1.3.4` 已安装到正式 IntelliJ IDEA 2026.2 用户插件目录；安装包内元数据显示版本为 `1.3.4`、插件 ID 为 `com.zuozhi.ideaannotation`、`since-build=262`。安装时正式 IDEA 进程未运行，下次启动将直接加载新版本。
- 被替换的 `1.3.3` 插件目录保留在 `/tmp/idea-annotation-before-1.3.4-20260901144628`，可在临时目录尚未清理时用于回退。

### 2026-09-01 1.3.3 当前状态

- 用户已在 IDEA 2026.2 sandbox 中人工验收批注载荷不再以空白行开头，并明确确认发布 `1.3.3`。
- 提交 `7abb866` 已推送到 `main`，稳定标签 `1.3.3` 指向该提交并已推送。
- [Release #33468497004](https://github.com/zuozh11/idea-annotation/actions/runs/33468497004) 已完成版本校验、构建、签名、Marketplace 上传和 GitHub Release 创建；`Build signed package` 与 `Create GitHub Release after Marketplace upload` job 均成功。
- [GitHub Release 1.3.3](https://github.com/zuozh11/idea-annotation/releases/tag/1.3.3) 已创建，正式资产为 `idea-annotation-1.3.3-signed.zip`。
- Marketplace `publishPlugin` 已成功；2026-09-01 12:09 CST 公共 API 显示插件 `hasUnapprovedUpdate=true`，已批准并列出的最新公开版本仍为 `1.3.2`。这证明 `1.3.3` 上传已被 Marketplace 接受但仍待 JetBrains 审核，不代表已经公开上架。
- 本次未要求安装，未替换正式 IntelliJ IDEA 用户插件目录中的现有版本。

### 2026-09-01 1.3.2 当前状态

- `1.3.1` 发布工作流因 README 输出示例仍需更新而主动取消；[Release #33465890294](https://github.com/zuozh11/idea-annotation/actions/runs/33465890294) 的 `Publish plugin`、签名包上传和 GitHub Release 创建步骤均为 skipped，未产生 Marketplace 或 GitHub Release 分发副作用；已使用的 `1.3.1` 标签未移动或复用。
- 提交 `0bccfcb` 已推送到 `main`，稳定标签 `1.3.2` 指向该提交并已推送。
- [Release #33466093001](https://github.com/zuozh11/idea-annotation/actions/runs/33466093001) 已完成版本校验、构建、签名、Marketplace 上传和 GitHub Release 创建；`Build signed package` 与 `Create GitHub Release after Marketplace upload` job 均成功。
- [GitHub Release 1.3.2](https://github.com/zuozh11/idea-annotation/releases/tag/1.3.2) 已创建，正式资产为 `idea-annotation-1.3.2-signed.zip`。
- Marketplace `publishPlugin` 已成功；2026-09-01 12:04 CST 公共 API 显示更新 `1158286` 的 `approve=true`、`listed=true`，公开版本为 `1.3.2`。这证明 `1.3.2` 已通过 JetBrains 审核并公开列出。
- GitHub Release 中的签名版 `1.3.2` 已安装到正式 IntelliJ IDEA 2026.2 用户插件目录；安装包内元数据显示版本为 `1.3.2`、插件 ID 为 `com.zuozhi.ideaannotation`、`since-build=262`。当前正式 IDEA 进程仍在运行，需要重启后才会加载新版本。
- 被替换的旧版插件目录保留在 `/tmp/idea-annotation-before-1.3.2-20260901112842`，可在临时目录尚未清理时用于回退。

### 2026-09-01 1.1.2 当前状态

- 用户明确要求本次跳过沙箱界面确认和人工验收；沙箱启动及后续 CI 均不作为人工验收证据。
- 提交 `e4c3b08` 已推送到 `main`；[Build #33423190281](https://github.com/zuozh11/idea-annotation/actions/runs/33423190281) 的构建、ZIP artifact 和 Plugin Verifier report 均成功。
- 稳定标签 `1.1.2` 指向提交 `e4c3b08` 并已推送。
- [Release #33423717229](https://github.com/zuozh11/idea-annotation/actions/runs/33423717229) 已完成版本校验、构建、Plugin Verifier、签名、Marketplace 上传和 GitHub Release 创建；`package`、`publish` 与 `github-release-after-publish` job 均成功。
- [GitHub Release 1.1.2](https://github.com/zuozh11/idea-annotation/releases/tag/1.1.2) 已创建，正式资产为 `idea-annotation-1.1.2-signed.zip`。
- Marketplace `1.1.2` 更新编号为 `1158184`；2026-09-01 02:19 CST API 状态为 `approve=false`、`listed=false`，插件状态为 `hasUnapprovedUpdate=true`，兼容范围为 `262.0+`。这证明 Marketplace 已接受上传但仍待 JetBrains 审核，不代表已经公开上架。
- GitHub Release 中的签名版 `1.1.2` 已安装到正式 IntelliJ IDEA 2026.2 用户插件目录；安装包内元数据显示版本为 `1.1.2`、插件 ID 为 `com.zuozhi.ideaannotation`、`since-build=262`。当前正式 IDEA 进程仍在运行，需要重启后才会加载新版本。
- 被替换的旧版插件目录保留在 `/tmp/idea-annotation-before-1.1.2-20260901022037`，可在临时目录尚未清理时用于回退。

### 2026-09-01 1.1.1 当前状态

- 用户已在 IDEA 2026.2 沙箱中人工验收 `1.1.1` 设置页、三个快捷键配置入口的精确定位，以及 `Copy Selection or Path` 的复制图标。
- 提交 `ffe870a` 已推送到 `main`；[Build #33421517246](https://github.com/zuozh11/idea-annotation/actions/runs/33421517246) 的构建、ZIP artifact 和 Plugin Verifier report 均成功。
- 本地使用 IDEA 自带 JBR 25 执行 `buildPlugin verifyPlugin` 成功；Plugin Verifier 1.410 对 `IU-262.8665.337` 的结论为 Compatible，保留 3 条既有的 `ComponentInlay` 实验 API 提示。
- 稳定标签 `1.1.1` 指向提交 `ffe870a` 并已推送。
- [Release #33421969866](https://github.com/zuozh11/idea-annotation/actions/runs/33421969866) 已完成版本校验、构建、Plugin Verifier、签名、Marketplace 上传和 GitHub Release 创建；`package`、`publish` 与 `github-release-after-publish` job 均成功。
- [GitHub Release 1.1.1](https://github.com/zuozh11/idea-annotation/releases/tag/1.1.1) 已创建，正式资产为 `idea-annotation-1.1.1-signed.zip`。
- Marketplace API 当前资源编号更新为 `1158180`，并显示 `approve=false`、`hasUnapprovedUpdate=true`；这证明 `1.1.1` 上传已被接受但仍未通过 JetBrains 审核，不代表已经公开上架。
- GitHub Release 中的签名版 `1.1.1` 已安装到正式 IntelliJ IDEA 2026.2 用户插件目录；需要重启当前 IDEA 进程后才会加载新版本。

### 2026-09-01 1.1.0 状态

- 用户已在正式 IDEA 环境完成人工验收，并明确确认 `1.1.0` 可以发布。
- 提交 `96cc704` 已推送到 `main`；[Build #33415986376](https://github.com/zuozh11/idea-annotation/actions/runs/33415986376) 的构建、ZIP artifact 和 Plugin Verifier report 均成功。
- 稳定标签 `1.1.0` 指向提交 `96cc704` 并已推送。
- [Release #33416449991](https://github.com/zuozh11/idea-annotation/actions/runs/33416449991) 已完成版本校验、构建、Plugin Verifier、签名、Marketplace 上传和 GitHub Release 创建；`package`、`publish` 与 `github-release-after-publish` job 均成功。
- [GitHub Release 1.1.0](https://github.com/zuozh11/idea-annotation/releases/tag/1.1.0) 已创建，正式资产为 `idea-annotation-1.1.0-signed.zip`。
- 正式 IntelliJ IDEA 2026.2 用户插件目录已安装 GitHub Release 中的签名版 `1.1.0`；当前 IDEA 进程仍使用已加载版本，重启后加载新版本。
- Marketplace `1.1.0` 的更新编号为 `1158169`；2026-09-01 00:59 CST 后台准确状态为 **Under review**，兼容范围显示 `262.0+`，大小为 `37.75 KB`。这证明 Marketplace 已接受上传并进入 JetBrains 审核，不代表已经公开上架。
- Marketplace 公开页当前仍展示 `1.0.2`，说明 `1.1.0` 尚未通过审核并替换公开版本；同时也证明此前的 `1.0.2` 已在 2026-09-01 前通过审核并公开。
- README 已更新为 `1.1.0` 功能说明，批注输入框截图为 `docs/images/selection-annotation.png`，Codex 输出效果截图为 `docs/images/selection-annotation-output.png`。

### 2026-08-31 状态

- 提交 `34f6d65` 已推送到 `main`；[Build #33394375112](https://github.com/zuozh11/idea-annotation/actions/runs/33394375112) 的构建、ZIP artifact 和 Plugin Verifier report 均成功。
- 本地使用 IDEA 自带 JBR 25 执行 `clean buildPlugin verifyPlugin` 成功；Plugin Verifier 1.410 对正式 `IU-262.9437.185` 的结论为 Compatible，保留 3 条 `ComponentInlay` 实验 API 提示。
- [Release #33394808978](https://github.com/zuozh11/idea-annotation/actions/runs/33394808978) 已完成 `1.0.2` 构建、校验、签名、Marketplace 上传和 GitHub Release 创建；首次上传因 `PUBLISH_TOKEN` 含非 ASCII 字符在请求发送前失败，旋转 Token 并重跑失败任务后成功。
- [GitHub Release 1.0.2](https://github.com/zuozh11/idea-annotation/releases/tag/1.0.2) 已创建，资产为 `idea-annotation-1.0.2-signed.zip`。
- Marketplace `1.0.2` 的更新编号为 `1157735`；2026-08-31 21:26 CST 后台准确状态为 **Under review**，兼容范围显示 `262.0+`。这证明 Marketplace 已接受上传并进入 JetBrains 审核，不代表已公开上架。
- 正式 IntelliJ IDEA 2026.2 用户插件目录已安装签名版 `1.0.2`；IDEA 重启后加载新版本。
- 提交 `aca445b` 已推送到 `main`；[Build #33385642197](https://github.com/zuozh11/idea-annotation/actions/runs/33385642197) 的构建、ZIP artifact 和 Plugin Verifier report 均成功。
- `CERTIFICATE_CHAIN`、`PRIVATE_KEY`、`PRIVATE_KEY_PASSWORD`、`PUBLISH_TOKEN` 已配置为私有仓库 GitHub Actions Secrets；秘密值未写入仓库。
- 使用 IDEA 自带 JBR 25 执行 `buildPlugin verifyPlugin` 成功，生成 `build/distributions/idea-annotation-1.0.1.zip`。
- Plugin Verifier 1.410 对官方 `IU-262.8665.337` 的结论为 Compatible；无内部 API 使用，保留 3 条 `ComponentInlay` 实验 API 提示。报告位于 `build/reports/pluginVerifier/IU-262.8665.337/`。
- [Release #33387119602](https://github.com/zuozh11/idea-annotation/actions/runs/33387119602) 已完成构建、校验和签名，并创建 [GitHub Release 1.0.1](https://github.com/zuozh11/idea-annotation/releases/tag/1.0.1)；资产为 `idea-annotation-1.0.1-signed.zip`。
- Marketplace 插件编号为 `33955`，插件页面为 [Selection Annotation](https://plugins.jetbrains.com/plugin/33955-selection-annotation)，版本 `1.0.1` 的更新编号为 `1157596`。
- Marketplace 于 2026-08-31 接受首次上传，后台准确状态为 **Under review**，兼容范围显示 `262.0+`；这证明已进入 JetBrains 审核，不代表已公开上架。
- Developer EULA 使用[公开 Gist](https://gist.github.com/zuozh11/ea84559dc08fa8efeba10d0c5a1152e1)，仓库副本为 `EULA.md`。

## 官方依据

- [Publishing a Plugin](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
- [IntelliJ Platform Gradle Plugin extension](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html)
- [Uploading a new plugin](https://plugins.jetbrains.com/docs/marketplace/uploading-a-new-plugin.html)
- [JetBrains Marketplace Approval Guidelines](https://plugins.jetbrains.com/docs/marketplace/jetbrains-marketplace-approval-guidelines.html)
- [Best practices for listing your plugin](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html)
