package com.zuozhi.ideaannotation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
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

    void capsuleSettingChanged(boolean enabled) {
        if (!enabled) {
            controllers.values().forEach(EditorController::hideCapsule);
        }
    }

    private static final class EditorController implements Disposable, SelectionListener, VisibleAreaListener {
        private static final int CAPSULE_DELAY_MS = 180;
        private static final int GAP = 6;
        private static final Dimension INPUT_SIZE = JBUI.size(430, 190);

        private final Editor editor;
        private final Timer capsuleTimer;
        private JBPopup capsulePopup;
        private JBPopup inputPopup;
        private AnnotationContext.SelectionKey suppressedSelection;
        private boolean copySucceeded;

        private EditorController(Editor editor) {
            this.editor = editor;
            this.capsuleTimer = new Timer(CAPSULE_DELAY_MS, event -> showCapsule());
            capsuleTimer.setRepeats(false);
            editor.getSelectionModel().addSelectionListener(this, this);
            editor.getScrollingModel().addVisibleAreaListener(this, this);
        }

        @Override
        public void selectionChanged(@NotNull SelectionEvent event) {
            AnnotationContext context = AnnotationContext.from(editor);
            if (suppressedSelection != null
                && (context == null || !suppressedSelection.equals(context.selectionKey()))) {
                suppressedSelection = null;
            }
            hideCapsule();
            scheduleCapsule();
        }

        @Override
        public void visibleAreaChanged(@NotNull VisibleAreaEvent event) {
            hideCapsule();
        }

        private void scheduleCapsule() {
            capsuleTimer.restart();
        }

        private void showCapsule() {
            if (editor.isDisposed() || inputPopup != null
                || !AnnotationSettings.getInstance().isCapsuleEnabled()) {
                return;
            }
            AnnotationContext context = AnnotationContext.from(editor);
            if (context == null || context.selectionKey().equals(suppressedSelection)) {
                return;
            }

            JButton button = new JButton("批注");
            button.setFocusable(false);
            button.setMargin(JBUI.insets(2, 8));
            button.addActionListener(event -> openInput());

            capsulePopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(button, null)
                .setRequestFocus(false)
                .setFocusable(false)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(true)
                .setCancelOnWindowDeactivation(true)
                .setShowBorder(true)
                .createPopup();
            capsulePopup.show(new RelativePoint(
                editor.getContentComponent(), popupPoint(context, button.getPreferredSize(), true)
            ));
        }

        private void openInput() {
            AnnotationContext context = AnnotationContext.from(editor);
            if (context == null) {
                return;
            }
            hideCapsule();
            closeInput();

            JBTextArea commentField = new JBTextArea(5, 42);
            commentField.getEmptyText().setText("输入批注内容（可为空）");
            commentField.setLineWrap(false);

            JBLabel errorLabel = new JBLabel(" ");
            errorLabel.setForeground(UIUtil.getErrorForeground());

            JButton cancelButton = new JButton("取消");
            JButton copyButton = new JButton("复制");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0));
            actions.add(cancelButton);
            actions.add(copyButton);

            JBScrollPane scrollPane = new JBScrollPane(commentField);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            JPanel footer = new JPanel(new BorderLayout(JBUI.scale(8), 0));
            footer.add(errorLabel, BorderLayout.CENTER);
            footer.add(actions, BorderLayout.EAST);

            JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(8)));
            panel.setBorder(JBUI.Borders.empty(12));
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(footer, BorderLayout.SOUTH);
            panel.setPreferredSize(INPUT_SIZE);

            copySucceeded = false;
            ComponentPopupBuilder builder = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, commentField)
                .setRequestFocus(true)
                .setFocusable(true)
                .setCancelOnClickOutside(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelOnWindowDeactivation(true)
                .setCancelKeyEnabled(true)
                .setResizable(false)
                .setMovable(false)
                .setShowBorder(true);
            inputPopup = builder.createPopup();
            inputPopup.setFinalRunnable(() -> {
                inputPopup = null;
                if (!copySucceeded) {
                    scheduleCapsule();
                }
            });

            cancelButton.addActionListener(event -> inputPopup.cancel());
            copyButton.addActionListener(event -> {
                try {
                    String payload = AnnotationFormatter.format(context, commentField.getText());
                    CopyPasteManager.copyTextToClipboard(payload);
                    copySucceeded = true;
                    suppressedSelection = context.selectionKey();
                    inputPopup.closeOk(null);
                    showSuccess(context);
                } catch (RuntimeException exception) {
                    errorLabel.setText("复制失败，请重试");
                    commentField.requestFocusInWindow();
                }
            });

            inputPopup.show(new RelativePoint(
                editor.getContentComponent(), popupPoint(context, INPUT_SIZE, false)
            ));
        }

        private Point popupPoint(AnnotationContext context, Dimension popupSize, boolean capsule) {
            Document document = editor.getDocument();
            int anchorOffset;
            if (context.startLine() == context.endLine()) {
                anchorOffset = context.selectionEnd();
            } else {
                int firstLineIndex = context.startLine() - 1;
                anchorOffset = document.getLineEndOffset(firstLineIndex);
            }
            Point anchor = editor.offsetToXY(anchorOffset);
            Rectangle visible = editor.getScrollingModel().getVisibleArea();

            int x = anchor.x + GAP;
            if (x + popupSize.width > visible.x + visible.width) {
                x = anchor.x - popupSize.width - GAP;
            }
            x = Math.max(visible.x, Math.min(x, visible.x + visible.width - popupSize.width));

            int desiredY = capsule ? anchor.y - popupSize.height - GAP : anchor.y;
            int y = Math.max(
                visible.y,
                Math.min(desiredY, visible.y + visible.height - popupSize.height)
            );
            return new Point(x, y);
        }

        private void showSuccess(AnnotationContext context) {
            Point point = popupPoint(context, JBUI.size(120, 32), true);
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("批注已复制", MessageType.INFO, null)
                .setFadeoutTime(1800)
                .createBalloon()
                .show(new RelativePoint(editor.getContentComponent(), point), Balloon.Position.above);
        }

        private void hideCapsule() {
            capsuleTimer.stop();
            if (capsulePopup != null) {
                JBPopup popup = capsulePopup;
                capsulePopup = null;
                popup.cancel();
            }
        }

        private void closeInput() {
            if (inputPopup != null) {
                JBPopup popup = inputPopup;
                inputPopup = null;
                popup.cancel();
            }
        }

        @Override
        public void dispose() {
            hideCapsule();
            closeInput();
        }
    }
}
