# Repository Guidelines

## Project Structure & Module Organization

This repository contains the single-module Java IntelliJ IDEA plugin **Selection Annotation**. Keep it as one Gradle module.

- `src/main/java/com/zuozhi/ideaannotation/` owns editor context detection, the action, inline input UI, localization access, and Markdown formatting.
- `src/main/resources/META-INF/plugin.xml` owns stable plugin metadata and action/extension registration; `pluginIcon.svg` is the Marketplace/IDE plugin icon.
- `src/main/resources/messages/` contains the default English bundle and `zh_CN` bundle.
- `docs/scratch/01-1.0.0-IDEA批注插件/PRD.md` is the behavior baseline. Read it before changing plugin behavior or interaction.
- `docs/publishing.md` is the release runbook. Read it before changing metadata, compatibility, signing, GitHub Actions, versions, or Marketplace publication.
- `EULA.md` is the repository copy of the public Developer EULA. Keep both copies synchronized when changing license terms; the public URL is recorded in `docs/publishing.md`.

Keep the action, editor-owned UI service, selection context, and formatter separate only while their current responsibilities remain distinct.

## Build, Test, and Development Commands

Use the committed Gradle wrapper with Java/JBR 25. The target platform is IntelliJ IDEA 2026.2 (`262`).

```bash
./gradlew buildPlugin   # Compile and package the installable plugin ZIP
./gradlew runIde        # Launch a sandbox IntelliJ IDEA with the plugin enabled
./gradlew test          # Run tests when the requested change needs them
./gradlew verifyPlugin  # Run Plugin Verifier for release changes
```

Use `IDEA_HOME=/Applications/IntelliJ IDEA.app/Contents` when a local IDEA installation must replace the downloaded target platform. Build output belongs under `build/` and stays out of Git.

## Coding Style & Naming Conventions

Use four-space indentation and UTF-8 files. Follow Java naming: `UpperCamelCase` classes, `lowerCamelCase` methods and variables, and lowercase package segments under `com.zuozhi.ideaannotation`. Name actions with an `Action` suffix. Keep clipboard output deterministic and preserve the exact Markdown blockquote contract in the PRD. Put every user-visible string in both resource bundles.

## Testing Guidelines

Use JUnit through the IntelliJ Platform test framework when tests are required. Name tests `*Test` and mirror production package paths under `src/test`. Prefer coverage of multiline selections, comments, absolute source paths, line-boundary behavior, and exact clipboard text. Run only the smallest check needed for the requested result; Marketplace publication changes require `buildPlugin` and `verifyPlugin`.

## Commit & Pull Request Guidelines

Use the existing short Conventional-style subjects such as `feat: ...` or `fix: ...`. Keep each commit independently understandable and directly reversible. Pull requests should explain the user-visible flow, list affected files, include screenshots for UI changes, and state the Gradle command actually run.

## Security & Configuration

The plugin ID is `com.zuozhi.ideaannotation`; do not change it after Marketplace publication. The Marketplace numeric ID is `33955`, and the display name is `Selection Annotation`. Keep `sinceBuild` at `262` and omit `untilBuild` unless the compatibility policy is explicitly changed.

Use stable SemVer: `1.0.x` for fixes and `1.1.0` for new backward-compatible functionality. Update `version`, `RELEASE_NOTES.md`, and `CHANGELOG.md` together. A normal `main` push only builds and verifies. The Marketplace entry already exists, so all versions after `1.0.1` must be released with a matching stable tag; do not use the initial `workflow_dispatch` path for updates. Obtain explicit confirmation before pushing a version tag because it uploads Marketplace and creates the GitHub Release.

Keep README content stable and user-facing. Do not put temporary Marketplace review or moderation status in `README.md`; record exact upload, review, approval, and publication evidence only in `docs/publishing.md`.

Keep Marketplace tokens, certificate chains, private keys, key passwords, signing files, local IDE state, and sandbox data out of the repository. Supply publication credentials only through the documented environment variables or GitHub Actions Secrets. Treat a successful build, GitHub Release, and Marketplace approval as separate evidence states.

<!-- project-knowledge:start -->
## 项目知识

执行项目任务时，按下列协议选择、加载与维护项目知识。加载结果中的项目术语用于当前任务命名，项目规则必须遵守。本轮上下文若已有同等协议，直接使用，不必重复执行。
1. 以项目根为工作目录，执行：node docs/agents/project-knowledge.mjs scope
2. 根据当前任务与 scope 返回结果，自主选择 Context、sceneId 或 ruleId，执行：node docs/agents/project-knowledge.mjs load [--context <path>]... [--rule <sceneId|ruleId>]...
3. sceneId 加载整个场景，ruleId 加载单条原子 RULE；需要补充知识时可以继续执行 load。
4. 出现项目特有术语、实体关系、规范命名，或长期有效、不遵守就会跑偏的规则时，执行：node docs/agents/project-knowledge.mjs maintain。一次性结论、局部实现、能从代码确认的事实和已有文档不记录。
完整返回正文必须遵守；疑问或报错执行 node docs/agents/project-knowledge.mjs -h。
<!-- project-knowledge:end -->
