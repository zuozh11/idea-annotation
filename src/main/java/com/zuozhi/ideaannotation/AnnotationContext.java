package com.zuozhi.ideaannotation;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
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
        return new Selection(
            text,
            startOffset,
            endOffset,
            document.getLineNumber(startOffset) + 1,
            document.getLineNumber(endOffset - 1) + 1
        );
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
        int endLine
    ) {
    }
}
