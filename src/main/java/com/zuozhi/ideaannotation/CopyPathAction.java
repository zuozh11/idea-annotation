package com.zuozhi.ideaannotation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public final class CopyPathAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(PathContext.from(event) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PathContext context = PathContext.from(event);
        if (context != null) {
            ClipboardFeedback.copy(event, AnnotationFormatter.formatPaths(context.files()));
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
