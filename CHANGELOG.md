# Changelog

## 1.3.9 - 2026-09-03

- Preserved the relative column of a partial first line while normalizing complete lines in multiline selections.

## 1.3.8 - 2026-09-03

- Restored immediate focus of the annotation input and dismissal of the floating code toolbar after choosing Annotate.

## 1.3.7 - 2026-09-03

- Simplified annotation Markdown by placing compact source links before the comment and removing the footer.
- Combined matching multi-caret selections into one source group with shared selected text.
- Updated the README workflow animation and output examples.

## 1.3.6 - 2026-09-03

- Replaced the annotation input border's internal IntelliJ API with a plugin-owned public Swing border while preserving its theme-aware appearance.

## 1.3.5 - 2026-09-03

- Restyled the annotation editor to match the inline breakpoint log editor, with a localized title and overlaid actions.
- Added theme-aware borders around every source selection while its annotation editor is open.
- Prevented stale annotation inlays from blocking later annotation inputs.

## 1.3.4 - 2026-09-01

- Moved the emphasized user comment before labeled source context and numbered multiple sources.

## 1.3.3 - 2026-09-01

- Removed the leading blank line from annotation payloads.

## 1.3.2 - 2026-09-01

- Updated the README input screenshot and output example for the current annotation format.

## 1.3.1 - 2026-09-01

- Added compact source-link output for whole-line selections and omitted redundant code blocks.
- Added Copy Selection to the native floating code toolbar.
- Added local file, directory, and clipboard image pasting to annotation comments.
- Added atomic clickable resource links with deterministic image names and Markdown serialization.
- Preserved ordinary text pasting and explicit Enter/Shift+Enter behavior inside the IDEA Action System.
- Improved the annotation input card spacing, height, border, and button rendering.

## 1.1.2 - 2026-09-01

- Removed trailing blank lines from each selected text block.

## 1.1.1 - 2026-09-01

- Redesigned the settings page with a concise feature overview and clearer visual grouping.
- Added current shortcut details and links that open the matching IntelliJ IDEA Keymap entry.
- Aligned the direct copy action with the native Copy icon.

## 1.1.0 - 2026-09-01

- Added a compact source-link and fenced-code annotation template with a fixed comment marker and footer.
- Added multi-caret annotations with one shared comment.
- Removed leading blank lines and common indentation from selected text while preserving relative indentation.
- Added direct Option+C/Alt+C copying for selections and focused files or directories.
- Added Copy Path menus for the editor and Project View.
- Added an application setting that swaps Enter and Shift+Enter confirmation behavior.

## 1.0.2 - 2026-08-31

- Centered the annotation input beneath the full selected text range.

## 1.0.1 - 2026-08-31

- Added the native floating code toolbar entry and kept the editor context-menu entry.
- Positioned the fixed-width annotation input relative to the current selection.
- Added English and Simplified Chinese user-interface text.
- Improved clipboard feedback and preserved comments after copy failures.
- Replaced the internal floating-toolbar hide call with the public platform hint API.

## 1.0.0 - 2026-08-30

- Added source-aware Markdown annotations for selected editor text.
- Added an inline multiline comment input and clipboard output.
