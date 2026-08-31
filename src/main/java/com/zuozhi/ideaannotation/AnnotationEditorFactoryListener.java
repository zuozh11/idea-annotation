package com.zuozhi.ideaannotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import org.jetbrains.annotations.NotNull;

public final class AnnotationEditorFactoryListener implements EditorFactoryListener {
    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        service().register(event.getEditor());
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        service().unregister(event.getEditor());
    }

    private static AnnotationEditorService service() {
        return ApplicationManager.getApplication().getService(AnnotationEditorService.class);
    }
}
