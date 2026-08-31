package com.zuozhi.ideaannotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(name = "com.zuozhi.ideaannotation.AnnotationSettings", storages = @Storage("ideaAnnotation.xml"))
public final class AnnotationSettings implements PersistentStateComponent<AnnotationSettings.SettingsState> {
    private final SettingsState state = new SettingsState();

    public static AnnotationSettings getInstance() {
        return ApplicationManager.getApplication().getService(AnnotationSettings.class);
    }

    boolean isCapsuleEnabled() {
        return state.showSelectionCapsule;
    }

    void setCapsuleEnabled(boolean enabled) {
        state.showSelectionCapsule = enabled;
    }

    @Override
    public @NotNull SettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState loadedState) {
        XmlSerializerUtil.copyBean(loadedState, state);
    }

    public static final class SettingsState {
        public boolean showSelectionCapsule = true;
    }
}
