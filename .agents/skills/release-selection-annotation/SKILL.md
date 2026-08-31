---
name: release-selection-annotation
description: 发布本仓库 Selection Annotation 的稳定版本，或重建其 GitHub Actions Gradle 共享缓存；适用于用户要求发布版本、推送发布标签、安装签名插件、收口 Marketplace/GitHub Release 证据或刷新发布缓存；不用于普通开发构建。
---

# 发布 Selection Annotation

在仓库根目录执行。开始前完整读取 [AGENTS.md](../../../AGENTS.md)、[发布指南](../../../docs/publishing.md) 和当前 [Release 工作流](../../../.github/workflows/release.yml)；重建缓存时再读取 [Build 工作流](../../../.github/workflows/build.yml)。这些文件是动态事实来源，本 Skill 不缓存当前版本、任务名或 Marketplace 状态。

## 选择模式

- 用户明确要求“发布”“推送版本标签”或“发布并安装”时，执行稳定版本发布。
- 用户明确要求“重建/刷新 Actions 缓存”时，只执行缓存重建。
- “准备发布”“检查流程”“看看状态”只允许准备或只读检查，不授权推送版本标签。

## 稳定版本发布

1. **固定发布提交**
   - 确认当前分支、工作区、`origin/main` 和现有标签，不改动无关文件。
   - 从 `build.gradle.kts` 读取稳定 SemVer；确认 `RELEASE_NOTES.md` 与 `CHANGELOG.md` 已同步该版本。
   - 发布资料尚未完成时，一次性补齐并创建可直接回滚的本地提交。

2. **推送主分支**
   - 推送发布提交到 `main`，按刚读取的当前工作流判断是否需要等待 CI，不等待没有被触发的任务。
   - 确认远端 `main` 指向准备发布的提交。

3. **发布授权门**
   - 只有用户当前请求明确要求发布或推送标签时，才继续。
   - 创建与 Gradle 版本完全一致、指向该提交的新稳定标签并推送。不得移动或复用已经用于发布的标签。

4. **等待权威 Release**
   - 找到该标签触发的 Release run，持续等待到成功或出现真实失败。
   - 按刚读取的 Release 工作流和发布指南核对实际 Job、任务、产物及外部副作用，不从本 Skill 推断流程形状。
   - 失败时按 `docs/publishing.md` 的失败边界处理。Marketplace 可能已收到版本时，先查后台状态再决定是否重试上传。

5. **收口证据**
   - 确认 GitHub Release 存在且包含本次签名 ZIP。
   - 记录 Marketplace 的真实状态，严格区分“上传已接受”“审核中”“已公开”。不等待 JetBrains 审核完成，也不把 GitHub Release 当作公开上架证据。
   - 把本次提交、标签、Actions run、GitHub Release、Marketplace 状态及可选本地安装证据写入 `docs/publishing.md`，创建文档提交并推送 `main`。临时审核状态不写入 README。
   - 用户要求安装时，只安装 GitHub Release 中的签名 ZIP，并说明是否需要重启 IDEA。

## 重建共享 Gradle 缓存

缓存重建不是每次发布的步骤，只有用户明确要求时才执行。发现构建依赖或缓存策略变化时可以建议重建，但不得自行触发远端工作流。

1. 先把相关配置提交并推送到 `main`。
2. 按刚读取的 Build 工作流和发布指南选择允许写入共享缓存的 ref，并手工触发缓存重建入口。
3. 等待 run 完成，根据当前工作流预期核对实际任务和日志，确认新的共享缓存条目已经保存且没有发生发布副作用。
4. 报告 run URL、首次无缓存构建耗时和保存的缓存规模。

## 完成标准

- **发布模式**：标签指向发布提交；Gradle 版本、标签、Release run、GitHub Release 和签名 ZIP 版本相同；`origin/main` 包含发布提交及后续证据提交；`docs/publishing.md` 记录对应 URL、Marketplace 状态与时间；用户要求的本地安装已完成。
- **缓存模式**：当前权威配置指定 ref 上的缓存重建 run 成功，日志证明共享缓存已保存且没有发生发布副作用。
- 最终报告实际证据与未覆盖的外部状态。不要把“已触发”“正在运行”或“等待审核”当成未完成的本地工作。
