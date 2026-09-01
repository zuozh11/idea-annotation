package com.zuozhi.ideaannotation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public final class CopyContextAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        boolean available = editor != null
            ? AnnotationContext.localTextFile(editor) != null
            : PathContext.from(event) != null;
        event.getPresentation().putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
        event.getPresentation().setEnabled(available);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String content = context(event);
        if (content != null) {
            ClipboardFeedback.copy(event, content);
        }
    }

    private static String context(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            AnnotationContext annotation = AnnotationContext.from(editor);
            if (annotation != null) {
                return AnnotationFormatter.format(annotation, "");
            }
        }
        PathContext paths = PathContext.from(event);
        return paths == null ? null : AnnotationFormatter.formatPaths(paths.files());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
