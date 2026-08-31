# Repository Guidelines

## Project Direction

This repository contains **Selection Annotation**, an IntelliJ Platform plugin that turns editor selections and focused paths into source-aware Markdown. Preserve the current single-module Java shape while it remains the smallest design for the requested behavior; architecture, platform version, and compatibility range may evolve through explicit product work.

Reply in Simplified Chinese. Verify current external facts on the live web when the task depends on them; repository-local work should use the checked-in state as authority.

## Sources of Truth

- Read the current implementation under `src/main/` before changing behavior. Versioned documents under `docs/scratch/` preserve the decisions of their release; use the document relevant to the feature, but do not restore retired behavior merely because it appears in an older PRD.
- Read `build.gradle.kts` for the current plugin version, Java toolchain, target IDE, Gradle plugin, and compatibility range. Do not carry those values forward from instructions or past sessions.
- Read `src/main/resources/META-INF/plugin.xml` for the current plugin identity, extensions, actions, icons, and Marketplace-facing metadata.
- Read `.github/workflows/` and `docs/publishing.md` before changing CI, signing, publication, versions, compatibility, or release evidence. The checked-in workflows define what currently runs.
- Read `docs/CONTEXT.md` for project terminology and load only the relevant rules from `docs/rules/` through the project-knowledge protocol below.

## Repository Layout

- `src/main/java/com/zuozhi/ideaannotation/` owns editor context detection, actions, editor-owned UI, localization access, settings, clipboard feedback, and Markdown formatting.
- `src/main/resources/META-INF/plugin.xml` owns action and extension registration.
- `src/main/resources/messages/` contains the default English bundle and Simplified Chinese bundle.
- `docs/scratch/` contains versioned product decisions and prototypes.
- `docs/publishing.md` is the release runbook and evidence ledger.
- `.agents/skills/` contains reusable project workflows.

Keep action dispatch, context extraction, editor-owned UI, settings, clipboard feedback, and formatting separate while their responsibilities remain distinct. Prefer deleting or reusing code before adding new abstractions.

## Workflow Routing

- Use `$intellij-plugin-development` for implementation, diagnosis, API migration, `plugin.xml`, localization, Gradle, or compatibility work.
- Use `$sandbox-selection-annotation` when a behavior or UI change must be started in the IDEA sandbox and handed to the user for acceptance.
- Use `$release-selection-annotation` for stable publication, signed-package installation, release evidence, or shared Gradle-cache rebuilding.

These Skills route to current repository state; they are not substitutes for inspecting the affected code and configuration.

## Coding Conventions

- Use four-space indentation and UTF-8. Follow Java naming conventions under `com.zuozhi.ideaannotation`; action classes use the `Action` suffix.
- Keep clipboard output deterministic. Treat the currently accepted Markdown shape as an external protocol and update the relevant product document when intentionally changing it.
- Put user-visible text in the maintained resource bundles and access it through the bundle class. Keep IDs, protocol markers, paths, and other non-display values out of localization bundles.
- Build with the committed Gradle wrapper and keep generated output under ignored build directories.

## Evidence and Validation

Choose the smallest evidence that proves the requested result. Ordinary behavior and UI work normally ends with a sandbox handoff and user acceptance; use the sandbox Skill rather than adding unrelated tests, hashes, builds, or verifiers. When the user explicitly requests a different validation boundary, follow that boundary and report it.

Keep these evidence states distinct: static code inspection, successful compilation or packaging, sandbox startup with the repository open, user acceptance, signed package creation, Marketplace upload acceptance, JetBrains approval, and public availability. One state never implies the next.

## Commits, Pushes, and Releases

- Use short Conventional-style subjects. Each commit should represent one complete, independently understandable and directly reversible delivery result.
- Pull requests should describe the user-visible flow, affected files, screenshots for UI changes, and the command actually run.
- Pushing ordinary commits and pushing a stable version tag are different actions. A stable tag is a publication action and requires explicit authorization immediately before it is pushed.
- Keep credentials, private keys, certificate chains, passwords, signing files, local IDE state, and sandbox data outside the repository.

<!-- project-knowledge:start -->
## 项目知识

执行项目任务时，按下列协议选择、加载与维护项目知识。加载结果中的项目术语用于当前任务命名，项目规则必须遵守。本轮上下文若已有同等协议，直接使用，不必重复执行。
1. 以项目根为工作目录，执行：node docs/agents/project-knowledge.mjs scope
2. 根据当前任务与 scope 返回结果，自主选择 Context、sceneId 或 ruleId，执行：node docs/agents/project-knowledge.mjs load [--context <path>]... [--rule <sceneId|ruleId>]...
3. sceneId 加载整个场景，ruleId 加载单条原子 RULE；需要补充知识时可以继续执行 load。
4. 出现项目特有术语、实体关系、规范命名，或长期有效、不遵守就会跑偏的规则时，执行：node docs/agents/project-knowledge.mjs maintain。一次性结论、局部实现、能从代码确认的事实和已有文档不记录。
完整返回正文必须遵守；疑问或报错执行 node docs/agents/project-knowledge.mjs -h。
<!-- project-knowledge:end -->
