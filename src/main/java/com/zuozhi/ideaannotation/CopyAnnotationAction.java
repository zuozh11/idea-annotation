package com.zuozhi.ideaannotation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.codeFloatingToolbar.CodeFloatingToolbar;
import org.jetbrains.annotations.NotNull;

public final class CopyAnnotationAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
        event.getPresentation().setEnabledAndVisible(
            editor != null && AnnotationContext.from(editor) != null
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        CodeFloatingToolbar toolbar = CodeFloatingToolbar.getToolbar(editor);
        if (toolbar != null) {
            toolbar.scheduleHide();
        }
        ApplicationManager.getApplication()
            .getService(AnnotationEditorService.class)
            .openInput(editor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
