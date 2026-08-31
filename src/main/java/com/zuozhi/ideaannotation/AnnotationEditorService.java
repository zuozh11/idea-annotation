package com.zuozhi.ideaannotation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.ComponentInlayAlignment;
import com.intellij.openapi.editor.ComponentInlayKt;
import com.intellij.openapi.editor.ComponentInlayRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayProperties;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.IdentityHashMap;
import java.util.Map;

@Service(Service.Level.APP)
public final class AnnotationEditorService {
    private final Map<Editor, EditorController> controllers = new IdentityHashMap<>();

    void register(Editor editor) {
        controllers.computeIfAbsent(editor, EditorController::new);
    }

    void unregister(Editor editor) {
        EditorController controller = controllers.remove(editor);
        if (controller != null) {
            controller.dispose();
        }
    }

    public void openInput(Editor editor) {
        controllers.computeIfAbsent(editor, EditorController::new).openInput();
    }

    private static final class EditorController implements Disposable {
        private static final int GAP = 6;
        private static final Dimension INPUT_SIZE = JBUI.size(520, 112);

        private final Editor editor;
        private Inlay<ComponentInlayRenderer<JPanel>> inputInlay;

        private EditorController(Editor editor) {
            this.editor = editor;
        }

        private void openInput() {
            AnnotationContext context = AnnotationContext.from(editor);
            if (context == null) {
                return;
            }
            closeInput();

            Color background = editor.getColorsScheme().getDefaultBackground();

            EditorTextField commentField = new EditorTextField(
                "",
                editor.getProject(),
                PlainTextFileType.INSTANCE
            ) {
                @Override
                protected boolean shouldHaveBorder() {
                    return false;
                }
            };
            commentField.setOneLineMode(false);
            commentField.setPlaceholder(IdeaAnnotationBundle.message("annotation.input.placeholder"));
            commentField.setShowPlaceholderWhenFocused(true);
            commentField.setBackground(background);
            commentField.setPreferredSize(JBUI.size(INPUT_SIZE.width - 20, 62));

            JBLabel errorLabel = new JBLabel(" ");
            errorLabel.setForeground(UIUtil.getErrorForeground());

            JButton cancelButton = new JButton(
                IdeaAnnotationBundle.message("annotation.action.cancel")
            );
            JButton copyButton = new JButton(
                IdeaAnnotationBundle.message("annotation.action.copy")
            );
            cancelButton.setMargin(JBUI.insets(3, 12));
            copyButton.setMargin(JBUI.insets(3, 12));
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0));
            actions.setOpaque(false);
            actions.add(cancelButton);
            actions.add(copyButton);

            JPanel footer = new JPanel(new BorderLayout(JBUI.scale(8), 0));
            footer.setOpaque(false);
            footer.add(errorLabel, BorderLayout.CENTER);
            footer.add(actions, BorderLayout.EAST);

            JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(6)));
            panel.setBackground(background);
            panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(UIUtil.getBoundsColor(), JBUI.scale(16)),
                JBUI.Borders.empty(9, 11)
            ));
            panel.add(commentField, BorderLayout.CENTER);
            panel.add(footer, BorderLayout.SOUTH);
            panel.setPreferredSize(INPUT_SIZE);

            Point selectionPoint = editor.offsetToXY(context.selectionStart());
            Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            int cardX = Math.max(
                visibleArea.x,
                Math.min(
                    selectionPoint.x,
                    visibleArea.x + visibleArea.width - INPUT_SIZE.width
                )
            );
            JPanel inlayContainer = new JPanel(null);
            inlayContainer.setOpaque(false);
            inlayContainer.setPreferredSize(INPUT_SIZE);
            panel.setBounds(cardX, 0, INPUT_SIZE.width, INPUT_SIZE.height);
            inlayContainer.add(panel);

            InlayProperties properties = new InlayProperties()
                .showAbove(false)
                .showWhenFolded(true);
            Inlay<ComponentInlayRenderer<JPanel>> inlay = ComponentInlayKt.addComponentInlay(
                editor,
                editor.getDocument().getLineEndOffset(context.endLine() - 1),
                properties,
                inlayContainer,
                ComponentInlayAlignment.STRETCH_TO_CONTENT_WIDTH
            );
            if (inlay == null) {
                return;
            }
            inputInlay = inlay;
            commentField.setDisposedWith(inlay);
            Disposer.register(inlay, () -> {
                if (inputInlay == inlay) {
                    inputInlay = null;
                }
            });

            DumbAwareAction.create(event -> closeInput()).registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),
                commentField
            );
            commentField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent event) {
                    Component next = event.getOppositeComponent();
                    if (next != null && !SwingUtilities.isDescendingFrom(next, panel)) {
                        SwingUtilities.invokeLater(() -> {
                            Component owner = KeyboardFocusManager
                                .getCurrentKeyboardFocusManager()
                                .getFocusOwner();
                            if (owner != null && !SwingUtilities.isDescendingFrom(owner, panel)) {
                                closeInput();
                            }
                        });
                    }
                }
            });

            cancelButton.addActionListener(event -> closeInput());
            copyButton.addActionListener(event -> {
                try {
                    String payload = AnnotationFormatter.format(context, commentField.getText());
                    CopyPasteManager.copyTextToClipboard(payload);
                    closeInput();
                    showSuccess(context);
                } catch (RuntimeException exception) {
                    errorLabel.setText(
                        IdeaAnnotationBundle.message("annotation.copy.failed")
                    );
                    commentField.requestFocusInWindow();
                }
            });

            SwingUtilities.invokeLater(commentField::requestFocusInWindow);
        }

        private Point successPoint(AnnotationContext context, Dimension balloonSize) {
            Document document = editor.getDocument();
            int anchorOffset;
            if (context.startLine() == context.endLine()) {
                anchorOffset = context.selectionEnd();
            } else {
                anchorOffset = document.getLineEndOffset(context.startLine() - 1);
            }
            Point anchor = editor.offsetToXY(anchorOffset);
            Rectangle visible = editor.getScrollingModel().getVisibleArea();

            int x = anchor.x + GAP;
            if (x + balloonSize.width > visible.x + visible.width) {
                x = anchor.x - balloonSize.width - GAP;
            }
            x = Math.max(visible.x, Math.min(x, visible.x + visible.width - balloonSize.width));

            int desiredY = anchor.y - balloonSize.height - GAP;
            int y = Math.max(
                visible.y,
                Math.min(desiredY, visible.y + visible.height - balloonSize.height)
            );
            return new Point(x, y);
        }

        private void showSuccess(AnnotationContext context) {
            Dimension balloonSize = JBUI.size(120, 32);
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(
                    IdeaAnnotationBundle.message("annotation.copy.succeeded"),
                    MessageType.INFO,
                    null
                )
                .setFadeoutTime(1800)
                .createBalloon()
                .show(
                    new RelativePoint(
                        editor.getContentComponent(),
                        successPoint(context, balloonSize)
                    ),
                    Balloon.Position.above
                );
        }

        private void closeInput() {
            Inlay<?> inlay = inputInlay;
            inputInlay = null;
            if (inlay != null && inlay.isValid()) {
                Disposer.dispose(inlay);
            }
        }

        @Override
        public void dispose() {
            closeInput();
        }
    }
}
