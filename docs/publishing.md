# JetBrains Marketplace 发布指南

## 发布模型

- 插件 ID：`com.zuozhi.ideaannotation`，发布后不可变更。
- Marketplace 名称：`Selection Annotation`。原名称含 `IDEA`，不符合 Marketplace 对插件名称不得包含 JetBrains 产品名称的审核规则。
- 当前版本：`1.0.1`；最低兼容构建：`262`；不设置 `until-build`。
- 首次版本由手工工作流生成签名 ZIP 和 GitHub Release，再由维护者在 Marketplace 页面创建插件条目并上传该 ZIP。
- Marketplace 条目存在后，后续稳定版本由与 Gradle `version` 完全一致的标签自动发布，例如 `1.0.2`。
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

1. 将发布配置合入并推送到 `main`，确认 **Build** 工作流成功。
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
3. 合入并推送 `main`，等待 **Build** 工作流成功。
4. 经人工确认后，在 `main` 当前提交创建并推送同版本标签：

   ```bash
   git tag 1.0.2
   git push origin 1.0.2
   ```

5. **Release** 工作流将：
   1. 校验标签与 Gradle 版本完全一致；
   2. 执行 `buildPlugin`、`verifyPlugin` 和 `signPlugin`；
   3. 上传签名包为 GitHub Actions artifact；
   4. 执行 `publishPlugin`；
   5. 仅在 Marketplace 上传成功后创建同名 GitHub Release 并附加签名 ZIP。
6. 进入 Marketplace 后台记录本次更新的真实审核状态。审核通过并公开可见前，不得宣称“已上架”。

## 失败处理

| 失败位置 | 结果 | 处理 |
| --- | --- | --- |
| 版本或标签校验 | 未签名、未上传、无 GitHub Release | 修正 `version` 或创建正确的新标签；不要移动已用于发布的标签 |
| 构建或 Plugin Verifier | 未上传、无 GitHub Release | 查看 `plugin-verifier-report`，修复后发布新提交和标签 |
| 缺少签名或 Token Secret | 未上传、无 GitHub Release | 补齐或轮换 Secret 后重新运行 |
| `publishPlugin` 明确失败 | 无 GitHub Release | 按错误修正；如果响应含糊，先查 Marketplace 后台是否已收到该版本，避免重复上传 |
| 首次签名成功，GitHub Release 失败 | Marketplace 尚未上传，GitHub Release 缺失 | 只重新运行失败的 `initial-github-release` job |
| Marketplace 上传成功，GitHub Release 失败 | Marketplace 进入审核，GitHub Release 缺失 | 只重新运行失败的 `github-release-after-publish` job，不重复执行 Marketplace 上传 |
| JetBrains 审核未通过 | 版本未公开 | 按 Marketplace 反馈处理，并保留后台状态和沟通记录；构建成功不能覆盖审核结论 |

## 发布证据

每次发布交接至少记录：

| 证据 | 证明范围 |
| --- | --- |
| Build 工作流 URL 与结论 | 对应提交已构建并通过 Plugin Verifier |
| 首次 Release 工作流 URL 与 `initial-github-release` job 结论 | `1.0.1` 签名 ZIP 和 GitHub Release 已生成或失败 |
| 后续 Release 工作流 URL 与 `publish`、`github-release-after-publish` job 结论 | Marketplace 上传请求与 GitHub Release 创建成功或失败 |
| GitHub Release URL | 对应签名 ZIP 已形成 GitHub 发布物；不证明 Marketplace 状态 |
| Marketplace 插件/版本页面 URL、状态、记录时间 | JetBrains 当前审核或公开状态，最终发布证据 |

### 2026-08-31 当前状态

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
