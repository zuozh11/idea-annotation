package com.zuozhi.ideaannotation;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

record AnnotationContext(
    String absolutePath,
    String fileName,
    String language,
    List<Selection> selections
) {
    static AnnotationContext from(Editor editor) {
        VirtualFile file = localTextFile(editor);
        if (file == null) {
            return null;
        }

        Document document = editor.getDocument();
        List<Selection> selections = editor.getCaretModel().getAllCarets().stream()
            .filter(Caret::hasSelection)
            .map(caret -> selection(caret, document))
            .filter(selection -> selection != null)
            .toList();
        if (selections.isEmpty()) {
            return null;
        }

        return new AnnotationContext(
            file.toNioPath().toString(),
            file.getName(),
            language(file),
            selections
        );
    }

    static VirtualFile localTextFile(Editor editor) {
        if (editor.isDisposed() || editor.getEditorKind() == EditorKind.DIFF) {
            return null;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null || !file.isValid() || file.isDirectory()
            || !file.isInLocalFileSystem() || file.getFileType().isBinary()) {
            return null;
        }
        Path path = file.toNioPath();
        return path.isAbsolute() ? file : null;
    }

    Selection lastSelection() {
        return selections.getLast();
    }

    private static Selection selection(Caret caret, Document document) {
        String text = caret.getSelectedText();
        int startOffset = caret.getSelectionStart();
        int endOffset = caret.getSelectionEnd();
        if (text == null || text.isEmpty() || endOffset <= startOffset) {
            return null;
        }
        String content = removeCommonIndent(text);
        if (content.isEmpty()) {
            return null;
        }
        return new Selection(
            content,
            startOffset,
            endOffset,
            document.getLineNumber(startOffset) + 1,
            document.getLineNumber(endOffset - 1) + 1,
            isWholeLineSelection(document, startOffset, endOffset)
        );
    }

    private static boolean isWholeLineSelection(
        Document document,
        int startOffset,
        int endOffset
    ) {
        int startLine = document.getLineNumber(startOffset);
        int endLine = document.getLineNumber(endOffset - 1);
        int firstEffectiveLine = -1;
        int lastEffectiveLine = -1;
        CharSequence content = document.getCharsSequence();

        for (int line = startLine; line <= endLine; line++) {
            int selectedStart = Math.max(startOffset, document.getLineStartOffset(line));
            int selectedEnd = Math.min(endOffset, document.getLineEndOffset(line));
            if (containsEffectiveContent(content, selectedStart, selectedEnd)) {
                if (firstEffectiveLine < 0) {
                    firstEffectiveLine = line;
                }
                lastEffectiveLine = line;
            }
        }

        int firstLineStart = document.getLineStartOffset(firstEffectiveLine);
        int firstLineEnd = document.getLineEndOffset(firstEffectiveLine);
        int firstContentOffset = firstLineStart;
        while (firstContentOffset < firstLineEnd
            && isIndentCharacter(content.charAt(firstContentOffset))) {
            firstContentOffset++;
        }
        int effectiveSelectionStart = Math.max(startOffset, firstLineStart);
        int effectiveSelectionEnd = Math.min(
            endOffset,
            document.getLineEndOffset(lastEffectiveLine)
        );
        return effectiveSelectionStart <= firstContentOffset
            && effectiveSelectionEnd == document.getLineEndOffset(lastEffectiveLine);
    }

    private static boolean containsEffectiveContent(
        CharSequence content,
        int startOffset,
        int endOffset
    ) {
        for (int offset = startOffset; offset < endOffset; offset++) {
            if (!isIndentCharacter(content.charAt(offset))) {
                return true;
            }
        }
        return false;
    }

    private static String removeCommonIndent(String text) {
        String[] lines = text.split("\n", -1);
        int firstLine = 0;
        while (firstLine < lines.length && isBlankLine(lines[firstLine])) {
            firstLine++;
        }
        int lastLine = lines.length;
        while (lastLine > firstLine && isBlankLine(lines[lastLine - 1])) {
            lastLine--;
        }
        String commonIndent = null;
        for (int index = firstLine; index < lastLine; index++) {
            String line = lines[index];
            if (isBlankLine(line)) {
                continue;
            }
            int indentEnd = 0;
            while (indentEnd < line.length()
                && isIndentCharacter(line.charAt(indentEnd))) {
                indentEnd++;
            }
            String indent = line.substring(0, indentEnd);
            if (commonIndent == null) {
                commonIndent = indent;
            } else {
                int commonLength = 0;
                int maximum = Math.min(commonIndent.length(), indent.length());
                while (commonLength < maximum
                    && commonIndent.charAt(commonLength) == indent.charAt(commonLength)) {
                    commonLength++;
                }
                commonIndent = commonIndent.substring(0, commonLength);
            }
            if (commonIndent.isEmpty()) {
                break;
            }
        }

        int indentLength = commonIndent == null ? 0 : commonIndent.length();
        return String.join("\n", Arrays.stream(lines, firstLine, lastLine)
            .map(line -> isBlankLine(line) ? "" : line.substring(indentLength))
            .toList());
    }

    private static boolean isBlankLine(String line) {
        return line.chars().allMatch(character -> isIndentCharacter((char) character));
    }

    private static boolean isIndentCharacter(char character) {
        return character == ' ' || character == '\t';
    }

    private static String language(VirtualFile file) {
        if (file.getFileType() instanceof LanguageFileType languageFileType) {
            String id = languageFileType.getLanguage().getID();
            if (!id.isBlank()) {
                return id.toLowerCase(Locale.ROOT);
            }
        }
        String extension = file.getExtension();
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }

    record Selection(
        String text,
        int startOffset,
        int endOffset,
        int startLine,
        int endLine,
        boolean wholeLine
    ) {
    }
}
