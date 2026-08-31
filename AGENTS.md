# Repository Guidelines

## Project Structure & Module Organization

This repository is currently an initial scaffold: only project-local Codex configuration exists in `.codex/config.toml`. Keep the IntelliJ IDEA plugin as a single Gradle module when implementation begins. Use the standard layout:

- `src/main/java/` or `src/main/kotlin/` for plugin actions, dialogs, and clipboard formatting.
- `src/main/resources/META-INF/plugin.xml` for plugin metadata and action registration.
- `src/test/` for formatter and action tests.
- `src/main/resources/icons/` for plugin-owned icons, if any.

Place the editor context-menu action, comment dialog, and annotation formatter in separate, purpose-named classes only when each has distinct behavior.

## Build, Test, and Development Commands

Use the Gradle wrapper committed with the plugin project:

```bash
./gradlew buildPlugin   # Compile and package the installable plugin ZIP
./gradlew runIde        # Launch a sandbox IntelliJ IDEA with the plugin enabled
./gradlew test          # Run unit and platform tests
./gradlew verifyPlugin  # Check compatibility and plugin structure before release
```

Do not rely on a machine-wide Gradle installation. Update this section if task names differ from the generated IntelliJ Platform Gradle project.

## Coding Style & Naming Conventions

Use four-space indentation and UTF-8 files. Follow standard Java or Kotlin naming: `UpperCamelCase` classes, `lowerCamelCase` methods and variables, and lowercase package segments such as `com.zuozhi.ideaannotation`. Name actions with an `Action` suffix and dialogs with a `Dialog` suffix. Keep clipboard output formatting deterministic and preserve the required Markdown blockquote structure.

## Testing Guidelines

Use JUnit through the IntelliJ Platform test framework. Name tests `*Test` and mirror production package paths under `src/test`. Prioritize tests for multiline selections, comments, absolute source paths, and exact clipboard text. Run `./gradlew test` before submitting behavior changes.

## Commit & Pull Request Guidelines

No Git history exists yet, so there is no established commit convention. Use short imperative subjects such as `Add editor annotation action`. Keep each commit independently understandable and reversible. Pull requests should explain the user-visible flow, list affected files, and include screenshots for dialog or context-menu changes. Link related issues when available and state which Gradle command was run.

## Security & Configuration

Keep secrets, signing keys, local IDE state, and generated sandbox data out of the repository. Treat `.codex/config.toml` as project configuration; avoid adding user-specific absolute paths.

<!-- project-knowledge:start -->
## 项目知识

执行项目任务时，按下列协议选择、加载与维护项目知识。加载结果中的项目术语用于当前任务命名，项目规则必须遵守。本轮上下文若已有同等协议，直接使用，不必重复执行。
1. 以项目根为工作目录，执行：node docs/agents/project-knowledge.mjs scope
2. 根据当前任务与 scope 返回结果，自主选择 Context、sceneId 或 ruleId，执行：node docs/agents/project-knowledge.mjs load [--context <path>]... [--rule <sceneId|ruleId>]...
3. sceneId 加载整个场景，ruleId 加载单条原子 RULE；需要补充知识时可以继续执行 load。
4. 出现项目特有术语、实体关系、规范命名，或长期有效、不遵守就会跑偏的规则时，执行：node docs/agents/project-knowledge.mjs maintain。一次性结论、局部实现、能从代码确认的事实和已有文档不记录。
完整返回正文必须遵守；疑问或报错执行 node docs/agents/project-knowledge.mjs -h。
<!-- project-knowledge:end -->
