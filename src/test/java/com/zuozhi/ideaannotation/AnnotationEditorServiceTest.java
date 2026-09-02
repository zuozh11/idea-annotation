package com.zuozhi.ideaannotation;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.ComponentInlayRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnnotationEditorServiceTest extends BasePlatformTestCase {
    private static final String CONTENT = "alpha one\nbeta two\ngamma three\n";

    private AnnotationEditorService service;
    private Editor editor;
    private Path sourceFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceFile = Files.createTempFile("selection-annotation-", ".txt").toRealPath();
        Files.writeString(sourceFile, CONTENT);
        VfsRootAccess.allowRootAccess(getTestRootDisposable(), sourceFile.toString());
        var file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourceFile);
        assertNotNull(file);
        myFixture.configureFromExistingVirtualFile(file);
        editor = myFixture.getEditor();
        service = new AnnotationEditorService();
        service.register(editor);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            if (service != null && editor != null) {
                service.unregister(editor);
            }
        } finally {
            try {
                super.tearDown();
            } finally {
                if (sourceFile != null) {
                    Files.deleteIfExists(sourceFile);
                }
            }
        }
    }

    @Override
    protected boolean runInDispatchThread() {
        return true;
    }

    public void testTwentyConsecutiveOpensKeepOneComponentInlay() {
        editor.getSelectionModel().setSelection(0, "alpha".length());

        for (int iteration = 0; iteration < 20; iteration++) {
            service.openInput(editor);
            assertEquals(1, componentInlays().size());
        }

        unregisterAndAssertClean();
    }

    public void testSingleSelectionKeepsOneExactBorder() {
        int start = CONTENT.indexOf("beta");
        int end = start + "beta".length();
        editor.getSelectionModel().setSelection(start, end);

        service.openInput(editor);
        assertBorderRanges(Set.of(range(start, end)));

        service.openInput(editor);
        assertBorderRanges(Set.of(range(start, end)));

        unregisterAndAssertClean();
    }

    public void testMultipleCaretsKeepEverySelectionBorder() {
        int firstStart = CONTENT.indexOf("alpha");
        int firstEnd = firstStart + "alpha".length();
        int secondStart = CONTENT.indexOf("gamma");
        int secondEnd = secondStart + "gamma".length();

        Caret primary = editor.getCaretModel().getPrimaryCaret();
        primary.setSelection(firstStart, firstEnd);
        Caret secondary = editor.getCaretModel().addCaret(
            editor.offsetToVisualPosition(secondStart)
        );
        assertNotNull(secondary);
        secondary.setSelection(secondStart, secondEnd);

        service.openInput(editor);
        assertBorderRanges(Set.of(
            range(firstStart, firstEnd),
            range(secondStart, secondEnd)
        ));

        service.openInput(editor);
        assertBorderRanges(Set.of(
            range(firstStart, firstEnd),
            range(secondStart, secondEnd)
        ));

        unregisterAndAssertClean();
    }

    private List<Inlay<?>> componentInlays() {
        return editor.getInlayModel()
            .getBlockElementsInRange(0, editor.getDocument().getTextLength())
            .stream()
            .filter(inlay -> inlay.getRenderer() instanceof ComponentInlayRenderer<?>)
            .toList();
    }

    private List<RangeHighlighter> selectionBorders() {
        return Arrays.stream(editor.getMarkupModel().getAllHighlighters())
            .filter(highlighter -> highlighter.getTextAttributes(
                editor.getColorsScheme()
            ).getEffectType() == EffectType.ROUNDED_BOX)
            .toList();
    }

    private void assertBorderRanges(Set<String> expected) {
        Set<String> actual = selectionBorders().stream()
            .map(highlighter -> range(
                highlighter.getStartOffset(),
                highlighter.getEndOffset()
            ))
            .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    private void unregisterAndAssertClean() {
        service.unregister(editor);
        service = null;
        assertEmpty(componentInlays());
        assertEmpty(selectionBorders());
    }

    private static String range(int start, int end) {
        return start + ":" + end;
    }
}
