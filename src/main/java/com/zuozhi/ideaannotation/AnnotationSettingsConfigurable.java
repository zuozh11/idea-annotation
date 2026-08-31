package com.zuozhi.ideaannotation;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public final class AnnotationSettingsConfigurable implements Configurable {
    private JBCheckBox confirmWithShiftEnter;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return IdeaAnnotationBundle.message("settings.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        confirmWithShiftEnter = new JBCheckBox(
            IdeaAnnotationBundle.message("settings.confirmWithShiftEnter")
        );
        return confirmWithShiftEnter;
    }

    @Override
    public boolean isModified() {
        return confirmWithShiftEnter.isSelected()
            != AnnotationSettings.getInstance().confirmWithShiftEnter;
    }

    @Override
    public void apply() {
        AnnotationSettings.getInstance().confirmWithShiftEnter
            = confirmWithShiftEnter.isSelected();
    }

    @Override
    public void reset() {
        confirmWithShiftEnter.setSelected(
            AnnotationSettings.getInstance().confirmWithShiftEnter
        );
    }

    @Override
    public void disposeUIResources() {
        confirmWithShiftEnter = null;
    }
}
