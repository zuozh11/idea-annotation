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
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.ShadowJava2DBorder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
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
        private static final int INPUT_CORNER_RADIUS = JBUI.scale(12);
        private static final int INPUT_WIDTH = JBUI.scale(500);
        private static final int MIN_INPUT_WIDTH = JBUI.scale(200);

        private final Editor editor;
        private final List<Inlay<?>> ownedInputInlays = new ArrayList<>();
        private final List<RangeHighlighter> selectionHighlighters = new ArrayList<>();
        private Inlay<?> inputInlay;

        private EditorController(Editor editor) {
            this.editor = editor;
        }

        private void openInput() {
            AnnotationContext context = AnnotationContext.from(editor);
            if (context == null) {
                return;
            }
            closeInput();

            var colorsScheme = editor.getColorsScheme();
            Color editorBackground = colorsScheme.getDefaultBackground();
            Color inputBackground = JBColor.namedColor(
                "Debugger.Logpoint.fieldBackground",
                new JBColor(0xF7F8FA, 0x2B2D30)
            );
            Color inputBorder = JBColor.namedColor(
                "Debugger.Logpoint.fieldBorder",
                new JBColor(0xC9CCD6, 0x393B40)
            );
            Color foreground = colorsScheme.getDefaultForeground();
            Color linkForeground = colorsScheme.getAttributes(
                EditorColors.REFERENCE_HYPERLINK_COLOR
            ).getForegroundColor();
            if (linkForeground == null) {
                linkForeground = foreground;
            }

            boolean confirmWithShiftEnter = AnnotationSettings.getInstance()
                .confirmWithShiftEnter;
            JBLabel errorLabel = new JBLabel(" ");
            errorLabel.setForeground(UIUtil.getErrorForeground());
            AnnotationCommentField commentField = new AnnotationCommentField(
                editor.getProject(),
                IdeaAnnotationBundle.message(
                    confirmWithShiftEnter
                        ? "annotation.input.placeholder.shiftConfirm"
                        : "annotation.input.placeholder.enterConfirm"
                ),
                errorLabel::setText,
                inputBackground,
                foreground,
                linkForeground
            );
            commentField.setFont(colorsScheme.getFont(EditorFontType.PLAIN));
            Color caretColor = colorsScheme.getColor(EditorColors.CARET_COLOR);
            if (caretColor != null) {
                commentField.setCaretColor(caretColor);
            }
            Color selectionBackground = colorsScheme.getColor(
                EditorColors.SELECTION_BACKGROUND_COLOR
            );
            if (selectionBackground != null) {
                commentField.setSelectionColor(selectionBackground);
            }
            Color selectionForeground = colorsScheme.getColor(
                EditorColors.SELECTION_FOREGROUND_COLOR
            );
            if (selectionForeground != null) {
                commentField.setSelectedTextColor(selectionForeground);
            }
            JBScrollPane commentScrollPane = new JBScrollPane(commentField);
            commentScrollPane.setBorder(JBUI.Borders.empty());
            commentScrollPane.setOpaque(false);
            commentScrollPane.getViewport().setOpaque(false);
            commentScrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );

            ActionLink cancelButton = new ActionLink(
                IdeaAnnotationBundle.message("annotation.action.cancel")
            );
            ActionLink confirmButton = new ActionLink(
                IdeaAnnotationBundle.message("annotation.action.confirm")
            );
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(10), 0));
            actions.setOpaque(false);
            actions.add(cancelButton);
            actions.add(confirmButton);

            JLayeredPane commentArea = new JLayeredPane() {
                @Override
                public void doLayout() {
                    Insets insets = getInsets();
                    int contentLeft = insets.left + JBUI.scale(12);
                    int contentTop = insets.top + JBUI.scale(8);
                    int contentRight = insets.right + JBUI.scale(8);
                    int contentBottom = insets.bottom + JBUI.scale(8);
                    Dimension actionsSize = actions.getPreferredSize();
                    int actionsGap = JBUI.scale(4);
                    commentScrollPane.setBounds(
                        contentLeft,
                        contentTop,
                        Math.max(0, getWidth() - contentLeft - contentRight),
                        Math.max(
                            0,
                            getHeight()
                                - contentTop
                                - contentBottom
                                - actionsGap
                                - actionsSize.height
                        )
                    );
                    actions.setBounds(
                        getWidth() - contentRight - actionsSize.width,
                        getHeight() - contentBottom - actionsSize.height,
                        actionsSize.width,
                        actionsSize.height
                    );
                    Dimension errorSize = errorLabel.getPreferredSize();
                    errorLabel.setBounds(
                        contentLeft,
                        getHeight() - contentBottom - errorSize.height,
                        Math.max(
                            0,
                            actions.getX() - contentLeft - JBUI.scale(8)
                        ),
                        errorSize.height
                    );
                }
            };
            commentArea.setOpaque(false);
            commentArea.setBorder(new ShadowJava2DBorder(
                INPUT_CORNER_RADIUS,
                inputBackground,
                inputBorder
            ));
            commentArea.add(commentScrollPane, JLayeredPane.DEFAULT_LAYER);
            commentArea.add(errorLabel, JLayeredPane.PALETTE_LAYER);
            commentArea.add(actions, JLayeredPane.PALETTE_LAYER);
            updatePreferredInputHeight(commentArea, commentField, actions);

            JBLabel titleLabel = new JBLabel(
                IdeaAnnotationBundle.message("annotation.input.title"),
                UIUtil.ComponentStyle.SMALL
            ) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D copy = (Graphics2D) graphics.create();
                    try {
                        copy.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                        );
                        copy.setColor(editorBackground);
                        copy.fillRoundRect(
                            0,
                            0,
                            getWidth(),
                            getHeight(),
                            JBUI.scale(12),
                            JBUI.scale(12)
                        );
                    } finally {
                        copy.dispose();
                    }
                    super.paintComponent(graphics);
                }
            };
            titleLabel.setOpaque(false);
            titleLabel.setBorder(JBUI.Borders.empty(1, 6));
            Color activeTitleForeground = JBColor.namedColor(
                "Debugger.Logpoint.activeForeground",
                new JBColor(0xED820E, 0xF2C55C)
            );
            titleLabel.setForeground(activeTitleForeground);
            commentField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent event) {
                    titleLabel.setForeground(activeTitleForeground);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent event) {
                    titleLabel.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
                }
            });

            JPanel panel = new JPanel(null) {
                private int titleTopOverflow() {
                    return Math.max(
                        (int) Math.ceil(titleLabel.getPreferredSize().height * 0.6)
                            - JBUI.scale(4),
                        0
                    );
                }

                @Override
                public Dimension getPreferredSize() {
                    Dimension inputSize = commentArea.getPreferredSize();
                    return new Dimension(
                        inputSize.width,
                        inputSize.height + titleTopOverflow()
                    );
                }

                @Override
                public Dimension getMinimumSize() {
                    Dimension inputSize = commentArea.getMinimumSize();
                    return new Dimension(
                        inputSize.width,
                        inputSize.height + titleTopOverflow()
                    );
                }

                @Override
                public void doLayout() {
                    Dimension titleSize = titleLabel.getPreferredSize();
                    int topOverflow = titleTopOverflow();
                    commentArea.setBounds(
                        0,
                        topOverflow,
                        getWidth(),
                        Math.max(0, getHeight() - topOverflow)
                    );
                    Insets inputInsets = commentArea.getInsets();
                    titleLabel.setBounds(
                        Math.max(0, inputInsets.left + JBUI.scale(6)),
                        (int) Math.floor(
                            topOverflow + inputInsets.top - titleSize.height * 0.6
                        ),
                        titleSize.width,
                        titleSize.height
                    );
                }
            };
            panel.setOpaque(false);
            panel.add(commentArea);
            panel.add(titleLabel);
            panel.setComponentZOrder(titleLabel, 0);

            AnnotationContext.Selection anchorSelection = bottomSelection(context);
            Document document = editor.getDocument();
            int selectionBottomOffset = Math.max(
                anchorSelection.startOffset(),
                anchorSelection.endOffset() - 1
            );
            int targetVisualLine = editor.offsetToVisualPosition(selectionBottomOffset).line + 1;
            int targetLineOffset = editor.visualPositionToOffset(
                new VisualPosition(targetVisualLine, 0)
            );
            boolean hasFollowingVisualLine = targetLineOffset > selectionBottomOffset;
            int visualAnchorOffset;
            int inlayAnchorOffset;
            if (hasFollowingVisualLine) {
                int anchorLine = document.getLineNumber(targetLineOffset);
                visualAnchorOffset = firstNonWhitespaceOffset(document, anchorLine);
                inlayAnchorOffset = visualAnchorOffset;
            } else {
                visualAnchorOffset = document.getLineStartOffset(
                    anchorSelection.endLine() - 1
                );
                inlayAnchorOffset = document.getLineEndOffset(
                    anchorSelection.endLine() - 1
                );
            }

            EditorEx editorEx = (EditorEx) editor;
            JPanel inlayContainer = new JPanel() {
                @Override
                public void doLayout() {
                    Insets insets = getInsets();
                    int xOffset = editor.offsetToXY(visualAnchorOffset).x;
                    int gutterWidth = editorEx.getGutter() instanceof EditorGutterComponentEx gutter
                        ? gutter.getWidth()
                        : 0;
                    int verticalScrollBarWidth = editorEx.getScrollPane()
                        .getVerticalScrollBar()
                        .getWidth();
                    int availableWidth = Math.max(
                        MIN_INPUT_WIDTH,
                        editor.getComponent().getWidth()
                            - gutterWidth
                            - xOffset
                            - verticalScrollBarWidth
                            - JBUI.scale(8)
                    );
                    int width = Math.max(
                        MIN_INPUT_WIDTH,
                        Math.min(INPUT_WIDTH, availableWidth)
                    );
                    panel.setBounds(
                        insets.left + xOffset,
                        insets.top,
                        width,
                        panel.getPreferredSize().height
                    );
                }

                @Override
                public Dimension getPreferredSize() {
                    Insets insets = getInsets();
                    return new Dimension(
                        INPUT_WIDTH,
                        panel.getPreferredSize().height + insets.top + insets.bottom
                    );
                }

                @Override
                public Dimension getMinimumSize() {
                    Insets insets = getInsets();
                    return new Dimension(
                        MIN_INPUT_WIDTH,
                        panel.getPreferredSize().height + insets.top + insets.bottom
                    );
                }
            };
            inlayContainer.setOpaque(false);
            inlayContainer.setBorder(JBUI.Borders.empty(4, 0));
            inlayContainer.add(panel);

            InlayProperties properties = new InlayProperties()
                .showAbove(hasFollowingVisualLine)
                .showWhenFolded(true);
            Inlay<ComponentInlayRenderer<JPanel>> inlay = ComponentInlayKt.addComponentInlay(
                editor,
                inlayAnchorOffset,
                properties,
                inlayContainer,
                ComponentInlayAlignment.STRETCH_TO_CONTENT_WIDTH
            );
            if (inlay == null) {
                return;
            }
            inputInlay = inlay;
            ownedInputInlays.add(inlay);
            highlightSelections(context, activeTitleForeground);
            Disposer.register(inlay, () -> {
                ownedInputInlays.remove(inlay);
                if (inputInlay == inlay) {
                    inputInlay = null;
                    clearSelectionHighlights();
                }
            });
            commentField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    updateHeight();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    updateHeight();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    updateHeight();
                }

                private void updateHeight() {
                    SwingUtilities.invokeLater(() -> {
                        int previousHeight = commentArea.getPreferredSize().height;
                        updatePreferredInputHeight(commentArea, commentField, actions);
                        if (commentArea.getPreferredSize().height == previousHeight) {
                            return;
                        }
                        panel.revalidate();
                        inlayContainer.revalidate();
                        if (inlay.isValid()) {
                            inlay.update();
                        }
                    });
                }
            });

            DumbAwareAction.create(event -> closeInput()).registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),
                commentField
            );
            DumbAwareAction.create(event -> {
                var contents = CopyPasteManager.getInstance().getContents();
                if (contents != null) {
                    commentField.getTransferHandler().importData(commentField, contents);
                }
            }).registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(
                    KeyEvent.VK_V,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                )),
                commentField
            );
            boolean[] composingText = {false};
            commentField.addInputMethodListener(new InputMethodListener() {
                @Override
                public void inputMethodTextChanged(InputMethodEvent event) {
                    AttributedCharacterIterator text = event.getText();
                    int characterCount = text == null
                        ? 0
                        : text.getEndIndex() - text.getBeginIndex();
                    composingText[0] = characterCount > event.getCommittedCharacterCount();
                }

                @Override
                public void caretPositionChanged(InputMethodEvent event) {
                }
            });
            cancelButton.addActionListener(event -> closeInput());
            Runnable confirm = () -> {
                try {
                    String payload = AnnotationFormatter.format(
                        context,
                        commentField.getMarkdownText()
                    );
                    CopyPasteManager.copyTextToClipboard(payload);
                    closeInput();
                    showSuccess(context);
                } catch (RuntimeException exception) {
                    errorLabel.setText(
                        IdeaAnnotationBundle.message("annotation.copy.failed")
                    );
                    commentField.requestFocusInWindow();
                }
            };
            confirmButton.addActionListener(event -> confirm.run());
            DumbAwareAction.create(event -> {
                if (!composingText[0]) {
                    confirm.run();
                }
            }).registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER,
                    confirmWithShiftEnter ? InputEvent.SHIFT_DOWN_MASK : 0
                )),
                commentField
            );
            DumbAwareAction.create(event -> {
                if (!composingText[0]) {
                    commentField.replaceSelection("\n");
                }
            }).registerCustomShortcutSet(
                new CustomShortcutSet(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER,
                    confirmWithShiftEnter ? 0 : InputEvent.SHIFT_DOWN_MASK
                )),
                commentField
            );

            SwingUtilities.invokeLater(() -> {
                if (!inlay.isValid()) {
                    return;
                }
                inlay.update();
                inlayContainer.revalidate();
                editor.getContentComponent().revalidate();
                editor.getContentComponent().repaint();
                commentField.requestFocusInWindow();
            });
        }

        private Point successPoint(AnnotationContext context, Dimension balloonSize) {
            Document document = editor.getDocument();
            AnnotationContext.Selection selection = bottomSelection(context);
            int anchorOffset;
            if (selection.startLine() == selection.endLine()) {
                anchorOffset = selection.endOffset();
            } else {
                anchorOffset = document.getLineEndOffset(selection.startLine() - 1);
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

        private static void updatePreferredInputHeight(
            JLayeredPane commentArea,
            AnnotationCommentField commentField,
            JPanel actions
        ) {
            Insets inputInsets = commentArea.getInsets();
            int lineCount = Math.max(
                2,
                commentField.getDocument().getDefaultRootElement().getElementCount()
            );
            int lineHeight = commentField.getFontMetrics(commentField.getFont()).getHeight();
            int textHeight = Math.max(
                commentField.getPreferredSize().height,
                lineCount * lineHeight
            );
            int inputContentHeight = textHeight
                + JBUI.scale(4)
                + actions.getPreferredSize().height;
            commentArea.setPreferredSize(new Dimension(
                INPUT_WIDTH,
                inputInsets.top
                    + JBUI.scale(8)
                    + inputContentHeight
                    + JBUI.scale(8)
                    + inputInsets.bottom
            ));
        }

        private static int firstNonWhitespaceOffset(Document document, int line) {
            int lineStart = document.getLineStartOffset(line);
            int lineEnd = document.getLineEndOffset(line);
            CharSequence text = document.getCharsSequence();
            int offset = lineStart;
            while (offset < lineEnd) {
                char character = text.charAt(offset);
                if (character != ' ' && character != '\t') {
                    break;
                }
                offset++;
            }
            return offset;
        }

        private static AnnotationContext.Selection bottomSelection(
            AnnotationContext context
        ) {
            return context.selections().stream()
                .max(java.util.Comparator.comparingInt(
                    AnnotationContext.Selection::endOffset
                ))
                .orElseThrow();
        }

        private void highlightSelections(AnnotationContext context, Color borderColor) {
            TextAttributes attributes = new TextAttributes();
            attributes.setEffectColor(borderColor);
            attributes.setEffectType(EffectType.ROUNDED_BOX);
            for (AnnotationContext.Selection selection : context.selections()) {
                selectionHighlighters.add(editor.getMarkupModel().addRangeHighlighter(
                    selection.startOffset(),
                    selection.endOffset(),
                    HighlighterLayer.SELECTION,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE
                ));
            }
        }

        private void clearSelectionHighlights() {
            for (RangeHighlighter highlighter : selectionHighlighters) {
                if (highlighter.isValid()) {
                    highlighter.dispose();
                }
            }
            selectionHighlighters.clear();
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
            inputInlay = null;
            for (Inlay<?> inlay : List.copyOf(ownedInputInlays)) {
                inlay.dispose();
            }
            ownedInputInlays.clear();
            clearSelectionHighlights();
        }

        @Override
        public void dispose() {
            closeInput();
        }
    }
}
