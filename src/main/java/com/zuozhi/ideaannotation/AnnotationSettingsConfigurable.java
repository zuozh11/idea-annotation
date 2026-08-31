package com.zuozhi.ideaannotation;

import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.impl.ui.KeymapPanel;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnnotationSettingsConfigurable implements Configurable {
    private static final String COPY_ANNOTATION_ACTION_ID
        = "com.zuozhi.ideaannotation.CopyAnnotationAction";
    private static final String COPY_CONTEXT_ACTION_ID
        = "com.zuozhi.ideaannotation.CopyContextAction";
    private static final String COPY_PATH_ACTION_ID
        = "com.zuozhi.ideaannotation.CopyPathAction";

    private JBCheckBox confirmWithShiftEnter;
    private Map<String, JBLabel> shortcutLabels;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return IdeaAnnotationBundle.message("settings.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        shortcutLabels = new LinkedHashMap<>();
        confirmWithShiftEnter = new JBCheckBox(
            IdeaAnnotationBundle.message("settings.confirmWithShiftEnter")
        );

        JPanel panel = FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(
                IdeaAnnotationBundle.message("settings.section.overview")
            ))
            .addComponent(helpLabel(
                IdeaAnnotationBundle.message("settings.overview.description")
            ), 4)
            .addVerticalGap(8)
            .addComponent(new TitledSeparator(
                IdeaAnnotationBundle.message("settings.section.shortcuts")
            ))
            .addLabeledComponent(
                IdeaAnnotationBundle.message(
                    "action.com.zuozhi.ideaannotation.CopyAnnotationAction.text"
                ) + ":",
                shortcutControl(
                    COPY_ANNOTATION_ACTION_ID
                )
            )
            .addLabeledComponent(
                IdeaAnnotationBundle.message(
                    "action.com.zuozhi.ideaannotation.CopyContextAction.text"
                ) + ":",
                shortcutControl(
                    COPY_CONTEXT_ACTION_ID
                )
            )
            .addLabeledComponent(
                IdeaAnnotationBundle.message(
                    "action.com.zuozhi.ideaannotation.CopyPathAction.text"
                ) + ":",
                shortcutControl(
                    COPY_PATH_ACTION_ID
                )
            )
            .addComponent(helpLabel(
                IdeaAnnotationBundle.message("settings.shortcuts.description")
            ), 4)
            .addVerticalGap(8)
            .addComponent(new TitledSeparator(
                IdeaAnnotationBundle.message("settings.section.inputBehavior")
            ))
            .addComponent(confirmWithShiftEnter, 4)
            .addComponent(helpLabel(
                IdeaAnnotationBundle.message("settings.confirmWithShiftEnter.description")
            ), 4)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        updateShortcutLabels();
        return panel;
    }

    private JComponent shortcutControl(String actionId) {
        JBLabel shortcutLabel = helpLabel("");
        shortcutLabels.put(actionId, shortcutLabel);

        ActionLink configureLink = new ActionLink(
            IdeaAnnotationBundle.message("settings.shortcuts.configure"),
            (ActionListener) event -> openKeymapSettings(actionId)
        );
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(shortcutLabel);
        panel.add(Box.createHorizontalStrut(JBUI.scale(8)));
        panel.add(configureLink);
        return panel;
    }

    private static JBLabel helpLabel(String text) {
        return new JBLabel(
            text,
            UIUtil.ComponentStyle.SMALL,
            UIUtil.FontColor.BRIGHTER
        ).setAllowAutoWrapping(true);
    }

    private static void openKeymapSettings(String actionId) {
        ShowSettingsUtil.getInstance().showSettingsDialog(
            null,
            KeymapPanel.class,
            panel -> panel.selectAction(actionId)
        );
    }

    private void updateShortcutLabels() {
        shortcutLabels.forEach((actionId, label) -> {
            String shortcut = KeymapUtil.getShortcutText(actionId);
            label.setText(shortcut.isBlank()
                ? IdeaAnnotationBundle.message("settings.shortcuts.notSet")
                : shortcut);
        });
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
        updateShortcutLabels();
    }

    @Override
    public void disposeUIResources() {
        confirmWithShiftEnter = null;
        shortcutLabels = null;
    }
}
