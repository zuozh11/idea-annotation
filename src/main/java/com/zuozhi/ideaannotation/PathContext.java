package com.zuozhi.ideaannotation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

record PathContext(List<VirtualFile> files) {
    static PathContext from(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            VirtualFile file = AnnotationContext.localTextFile(editor);
            return file == null ? null : new PathContext(List.of(file));
        }

        VirtualFile[] files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0 || Arrays.stream(files).anyMatch(
            file -> !isLocalPath(file)
        )) {
            return null;
        }
        return new PathContext(List.of(files));
    }

    private static boolean isLocalPath(VirtualFile file) {
        if (!file.isValid() || !file.isInLocalFileSystem()) {
            return false;
        }
        Path path = file.toNioPath();
        return path.isAbsolute();
    }
}
