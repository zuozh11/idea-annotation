package com.zuozhi.ideaannotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(
    name = "com.zuozhi.ideaannotation.AnnotationSettings",
    storages = @Storage("selectionAnnotation.xml")
)
public final class AnnotationSettings implements PersistentStateComponent<AnnotationSettings> {
    public boolean confirmWithShiftEnter;

    static AnnotationSettings getInstance() {
        return ApplicationManager.getApplication().getService(AnnotationSettings.class);
    }

    @Override
    public AnnotationSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AnnotationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
