package com.zuozhi.ideaannotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JComponent;
import javax.swing.JPanel;

public final class AnnotationSettingsConfigurable implements Configurable {
    private JBCheckBox showCapsuleCheckBox;
    private JPanel panel;

    @Override
    public String getDisplayName() {
        return "IDEA Annotation";
    }

    @Override
    public JComponent createComponent() {
        showCapsuleCheckBox = new JBCheckBox("显示选区批注胶囊");
        panel = FormBuilder.createFormBuilder()
            .addComponent(showCapsuleCheckBox)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        return showCapsuleCheckBox.isSelected() != AnnotationSettings.getInstance().isCapsuleEnabled();
    }

    @Override
    public void apply() {
        boolean enabled = showCapsuleCheckBox.isSelected();
        AnnotationSettings.getInstance().setCapsuleEnabled(enabled);
        ApplicationManager.getApplication()
            .getService(AnnotationEditorService.class)
            .capsuleSettingChanged(enabled);
    }

    @Override
    public void reset() {
        showCapsuleCheckBox.setSelected(AnnotationSettings.getInstance().isCapsuleEnabled());
    }

    @Override
    public void disposeUIResources() {
        showCapsuleCheckBox = null;
        panel = null;
    }
}
