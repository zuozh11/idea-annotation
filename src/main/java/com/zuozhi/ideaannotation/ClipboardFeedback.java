package com.zuozhi.ideaannotation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.JComponent;
import java.awt.Component;

final class ClipboardFeedback {
    private ClipboardFeedback() {
    }

    static void copy(AnActionEvent event, String text) {
        try {
            CopyPasteManager.copyTextToClipboard(text);
            show(event, "annotation.copy.succeeded", MessageType.INFO);
        } catch (RuntimeException exception) {
            show(event, "annotation.copy.failed", MessageType.ERROR);
        }
    }

    private static void show(AnActionEvent event, String messageKey, MessageType type) {
        Component component = event.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
        if (!(component instanceof JComponent anchor)) {
            return;
        }
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                IdeaAnnotationBundle.message(messageKey),
                type,
                null
            )
            .setFadeoutTime(1800)
            .createBalloon()
            .showInCenterOf(anchor);
    }
}
