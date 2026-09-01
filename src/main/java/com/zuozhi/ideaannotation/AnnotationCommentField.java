package com.zuozhi.ideaannotation;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class AnnotationCommentField extends JTextPane {
    private static final String FILE_LINK_ATTRIBUTE = "selectionAnnotation.fileLink";
    private static final char COMPONENT_CHARACTER = '\uFFFC';

    private final Project project;
    private final String placeholder;
    private final Consumer<String> showError;
    private boolean linkNavigation;

    AnnotationCommentField(
        Project project,
        String placeholder,
        Consumer<String> showError,
        Color background
    ) {
        super(new DefaultStyledDocument());
        this.project = project;
        this.placeholder = placeholder;
        this.showError = showError;
        setBackground(background);
        setBorder(JBUI.Borders.empty());
        setFont(UIUtil.getLabelFont());
        setTransferHandler(new CommentTransferHandler());
        addCaretListener(event -> {
            if (getSelectionStart() == getSelectionEnd()) {
                getInputAttributes().removeAttribute(FILE_LINK_ATTRIBUTE);
                getInputAttributes().removeAttribute(StyleConstants.ComponentAttribute);
            }
        });
    }

    String getMarkdownText() {
        return serialize(0, getDocument().getLength());
    }

    boolean consumeLinkNavigationFocusLoss() {
        if (!linkNavigation) {
            return false;
        }
        linkNavigation = false;
        return true;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getDocument().getLength() == 0) {
            graphics.setColor(UIUtil.getContextHelpForeground());
            graphics.setFont(getFont());
            graphics.drawString(
                placeholder,
                getInsets().left,
                getInsets().top + graphics.getFontMetrics().getAscent()
            );
        }
    }

    private void pasteFiles(Transferable transferable) throws Exception {
        Object value = transferable.getTransferData(DataFlavor.javaFileListFlavor);
        if (!(value instanceof List<?> values)) {
            showError.accept(IdeaAnnotationBundle.message("annotation.paste.itemsFailed"));
            return;
        }

        List<FileLink> candidates = new ArrayList<>();
        int invalidItems = 0;
        for (Object item : values) {
            if (!(item instanceof File file)) {
                invalidItems++;
                continue;
            }
            Path path = file.toPath().toAbsolutePath().normalize();
            Path name = path.getFileName();
            if (name == null) {
                invalidItems++;
                continue;
            }
            candidates.add(new FileLink(name.toString(), path));
        }

        PasteLocation location = pasteLocation();
        int initialFailures = invalidItems;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<FileLink> links = new ArrayList<>();
            int failures = initialFailures;
            for (FileLink candidate : candidates) {
                if (Files.exists(candidate.path())) {
                    links.add(candidate);
                } else {
                    failures++;
                }
            }
            int finalFailures = failures;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!links.isEmpty()) {
                    insertLinks(links, values.size() > 1, location);
                }
                showError.accept(finalFailures == 0
                    ? " "
                    : IdeaAnnotationBundle.message("annotation.paste.itemsFailed"));
            });
        });
    }

    private void pasteImage(Transferable transferable) throws Exception {
        Object value = transferable.getTransferData(DataFlavor.imageFlavor);
        if (!(value instanceof Image image)) {
            showError.accept(IdeaAnnotationBundle.message("annotation.paste.imageFailed"));
            return;
        }

        BufferedImage bufferedImage = toBufferedImage(image);
        PasteLocation location = pasteLocation();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Path temporaryFile = null;
            try {
                temporaryFile = Files.createTempFile(
                    "selection-annotation-clipboard-",
                    ".png"
                );
                if (!ImageIO.write(bufferedImage, "png", temporaryFile.toFile())) {
                    throw new IOException("No PNG writer is available");
                }
                Path imageFile = temporaryFile;
                ApplicationManager.getApplication().invokeLater(() -> {
                    insertLinks(List.of(new FileLink(
                        "剪贴板图片",
                        imageFile
                    )), false, location);
                    showError.accept(" ");
                });
            } catch (Exception exception) {
                if (temporaryFile != null) {
                    try {
                        Files.deleteIfExists(temporaryFile);
                    } catch (IOException ignored) {
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> showError.accept(
                    IdeaAnnotationBundle.message("annotation.paste.imageFailed")
                ));
            }
        });
    }

    private PasteLocation pasteLocation() throws BadLocationException {
        StyledDocument document = getStyledDocument();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        return new PasteLocation(
            document.createPosition(start),
            document.createPosition(end),
            document.getText(start, end - start)
        );
    }

    private void insertLinks(
        List<FileLink> links,
        boolean separateLines,
        PasteLocation location
    ) {
        StyledDocument document = getStyledDocument();
        int start = location.start().getOffset();
        int end = location.end().getOffset();
        try {
            if (document.getText(start, end - start).equals(location.selectedText())) {
                document.remove(start, end - start);
            }
            int offset = start;
            if (separateLines && offset > 0
                && document.getText(offset - 1, 1).charAt(0) != '\n') {
                document.insertString(offset++, "\n", null);
            }
            for (int index = 0; index < links.size(); index++) {
                if (index > 0) {
                    document.insertString(offset++, "\n", null);
                }
                FileLink link = links.get(index);
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                attributes.addAttribute(FILE_LINK_ATTRIBUTE, link);
                StyleConstants.setComponent(attributes, linkComponent(link));
                document.insertString(offset++, String.valueOf(COMPONENT_CHARACTER), attributes);
            }
            if (separateLines && offset < document.getLength()
                && document.getText(offset, 1).charAt(0) != '\n') {
                document.insertString(offset++, "\n", null);
            }
            setCaretPosition(offset);
        } catch (BadLocationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private JComponent linkComponent(FileLink link) {
        JBLabel label = new JBLabel("<html><a href=''>" + escapeHtml(link.name()) + "</a></html>");
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setToolTipText(link.path().toString());
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                openLink(link);
            }
        });
        return label;
    }

    private void openLink(FileLink link) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            VirtualFile file = Files.exists(link.path()) && Files.isReadable(link.path())
                ? VirtualFileManager.getInstance().refreshAndFindFileByNioPath(link.path())
                : null;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (file == null || !file.isValid() || project == null || project.isDisposed()) {
                    showError.accept(IdeaAnnotationBundle.message("annotation.link.missing"));
                    return;
                }
                showError.accept(" ");
                linkNavigation = true;
                if (file.isDirectory()) {
                    ProjectView.getInstance(project).select(null, file, false);
                } else {
                    new OpenFileDescriptor(project, file).navigate(false);
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (isFocusOwner()) {
                        linkNavigation = false;
                    }
                });
            });
        });
    }

    private String serialize(int start, int end) {
        StyledDocument document = getStyledDocument();
        StringBuilder text = new StringBuilder();
        try {
            for (int offset = start; offset < end; offset++) {
                AttributeSet attributes = document.getCharacterElement(offset)
                    .getAttributes();
                Object value = attributes.getAttribute(FILE_LINK_ATTRIBUTE);
                if (value instanceof FileLink link) {
                    text.append(AnnotationFormatter.markdownLink(
                        link.name(),
                        link.path().toString()
                    ));
                } else {
                    text.append(document.getText(offset, 1));
                }
            }
        } catch (BadLocationException exception) {
            throw new IllegalStateException(exception);
        }
        return text.toString();
    }

    private static BufferedImage toBufferedImage(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Clipboard image has no dimensions");
        }
        BufferedImage bufferedImage = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = bufferedImage.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return bufferedImage;
    }

    private static String escapeHtml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private record FileLink(String name, Path path) {
    }

    private record PasteLocation(Position start, Position end, String selectedText) {
    }

    private final class CommentTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return !support.isDrop()
                && (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                || support.isDataFlavorSupported(DataFlavor.imageFlavor)
                || support.isDataFlavorSupported(DataFlavor.stringFlavor));
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            Transferable transferable = support.getTransferable();
            try {
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    pasteFiles(transferable);
                } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    pasteImage(transferable);
                } else {
                    String text = (String) transferable.getTransferData(
                        DataFlavor.stringFlavor
                    );
                    StyledDocument document = getStyledDocument();
                    int start = getSelectionStart();
                    document.remove(start, getSelectionEnd() - start);
                    document.insertString(start, text, null);
                    setCaretPosition(start + text.length());
                    showError.accept(" ");
                }
                return true;
            } catch (Exception exception) {
                showError.accept(IdeaAnnotationBundle.message("annotation.paste.itemsFailed"));
                return false;
            }
        }

        @Override
        protected Transferable createTransferable(JComponent component) {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            return start == end ? null : new StringSelection(serialize(start, end));
        }

        @Override
        public int getSourceActions(JComponent component) {
            return COPY;
        }
    }
}
