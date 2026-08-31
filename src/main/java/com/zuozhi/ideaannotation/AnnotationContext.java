package com.zuozhi.ideaannotation;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.util.List;

record AnnotationContext(
    String absolutePath,
    String selectedText,
    int selectionStart,
    int selectionEnd,
    int startLine,
    int endLine
) {
    static AnnotationContext from(Editor editor) {
        if (editor.isDisposed() || editor.getEditorKind() == EditorKind.DIFF) {
            return null;
        }

        List<Caret> selectedCarets = editor.getCaretModel().getAllCarets().stream()
            .filter(Caret::hasSelection)
            .toList();
        if (selectedCarets.size() != 1) {
            return null;
        }

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null || !file.isValid() || file.isDirectory()
            || !file.isInLocalFileSystem() || file.getFileType().isBinary()) {
            return null;
        }

        Path path = file.toNioPath();
        if (!path.isAbsolute()) {
            return null;
        }

        Caret caret = selectedCarets.getFirst();
        String selectedText = caret.getSelectedText();
        int startOffset = caret.getSelectionStart();
        int endOffset = caret.getSelectionEnd();
        if (selectedText == null || selectedText.isEmpty() || endOffset <= startOffset) {
            return null;
        }

        Document document = editor.getDocument();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset - 1) + 1;
        return new AnnotationContext(
            path.toString(), selectedText, startOffset, endOffset, startLine, endLine
        );
    }

    String source() {
        if (startLine == endLine) {
            return absolutePath + ":" + startLine;
        }
        return absolutePath + ":" + startLine + "-" + endLine;
    }

    SelectionKey selectionKey() {
        return new SelectionKey(absolutePath, selectionStart, selectionEnd);
    }

    record SelectionKey(String absolutePath, int selectionStart, int selectionEnd) {
    }
}
