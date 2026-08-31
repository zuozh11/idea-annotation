# Selection Annotation

**English** | [简体中文](README.zh-CN.md)

[![Build](https://github.com/zuozh11/idea-annotation/actions/workflows/build.yml/badge.svg)](https://github.com/zuozh11/idea-annotation/actions/workflows/build.yml)
[![Get from JetBrains Marketplace](https://img.shields.io/badge/Get%20from-JetBrains%20Marketplace-000000?logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/plugin/33955-selection-annotation)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33955)](https://plugins.jetbrains.com/plugin/33955-selection-annotation)

![Selection Annotation icon](src/main/resources/META-INF/pluginIcon.svg)

Selection Annotation is an IntelliJ IDEA plugin that turns an editor selection into a Markdown annotation ready to paste into Codex or another conversation. It automatically includes the local absolute path, accurate line numbers, the selected text, and an optional comment.

![Selection Annotation input](docs/images/selection-annotation.png)

## Features

- Open **Annotate…** from the native IDEA floating code toolbar or the editor context menu.
- Enter an optional multiline comment in a fixed-width input beneath the selection.
- Use the current editor buffer, including unsaved files and Scratch files with a real local path.
- Calculate 1-based single-line or multiline ranges without counting an unselected next line after a trailing newline.
- Keep the original selection after a successful copy and preserve the entered comment when clipboard writing fails.
- Follow the IDE language with English and Simplified Chinese user-interface text.

## Usage

1. Select text in a local text file.
2. Click **Annotate…** in the floating code toolbar, or choose **Annotate…** from the editor context menu.
3. Enter an optional comment and click **Copy**.
4. Paste the clipboard content into the target conversation.

Example output:

```markdown
> **Source:**
> /project/src/Example.java:42-45
>
> **Selected text:**
> first line
>     second line
>
> **User comment:**
> Please check the boundary conditions here.
>
```

## Compatibility

- IntelliJ IDEA 2026.2 or later, with minimum platform build `262`.
- Java/JBR 25.
- Regular local text files and Scratch files with a real local path.
- Diff editors, temporary files without a stable local path, and multiple non-empty selections are not supported.

## Installation

After building locally, open **Settings → Plugins → ⚙ → Install Plugin from Disk…** in IDEA and select the ZIP under `build/distributions/`.

Marketplace page: [Selection Annotation](https://plugins.jetbrains.com/plugin/33955-selection-annotation).

## Development

This is a single-module Java Gradle project that uses the committed Gradle Wrapper:

```bash
./gradlew buildPlugin   # Build the installable ZIP
./gradlew runIde        # Launch an IDEA sandbox with the plugin
./gradlew test          # Run tests
./gradlew verifyPlugin  # Run IntelliJ Plugin Verifier
```

If the default system Java is not version 25, use the JBR bundled with IDEA:

```bash
export JAVA_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew buildPlugin
```

The installable ZIP is written to `build/distributions/`; build output is not committed.

## Release Maintenance

- The current version is defined by `version` in `build.gradle.kts`.
- Marketplace and GitHub Release notes for the current version are maintained in `RELEASE_NOTES.md`; complete history is maintained in `CHANGELOG.md`.
- Initial Marketplace setup, signing certificates, GitHub Secrets, tag-triggered releases, failure handling, and release evidence are documented in the [JetBrains Marketplace publishing guide](docs/publishing.md).
- The repository copy of the Developer EULA is available in [EULA.md](EULA.md), and Marketplace uses the [public version](https://gist.github.com/zuozh11/ea84559dc08fa8efeba10d0c5a1152e1).

## Data Boundary

The plugin only reads the current selection and local file path, then writes the generated annotation to the system clipboard. It does not send network requests, collect telemetry, or persist annotation history. Absolute paths are included in clipboard content, so verify the destination and content before pasting into an external service.
