package com.zuozhi.ideaannotation;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AnnotationContextTest extends BasePlatformTestCase {
    private Path sourceFile;

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            if (sourceFile != null) {
                Files.deleteIfExists(sourceFile);
            }
        }
    }

    public void testRestoresIndentMissingBeforeFirstContentCharacter() throws Exception {
        String content = "                .set(first)\n"
            + "                .set(second)\n"
            + "                .set(third)\n";
        int start = content.indexOf(".set(first)");
        int end = content.indexOf(".set(third)") + ".set(third)".length();

        assertEquals(
            ".set(first)\n.set(second)\n.set(third)",
            selectedText(content, start, end)
        );
    }

    public void testPreservesRelativeIndentFromSourceLines() throws Exception {
        String content = "            .set(first)\n"
            + "                .nested()\n";
        int start = content.indexOf(".set(first)");
        int end = content.indexOf(".nested()") + ".nested()".length();

        assertEquals(
            ".set(first)\n    .nested()",
            selectedText(content, start, end)
        );
    }

    public void testRestoresIndentWhenSelectionStartsInsideLeadingWhitespace() throws Exception {
        String content = "                .set(first)\n"
            + "                .set(second)\n";
        int firstLine = content.indexOf(".set(first)");
        int start = firstLine - 8;
        int end = content.indexOf(".set(second)") + ".set(second)".length();

        assertEquals(
            ".set(first)\n.set(second)",
            selectedText(content, start, end)
        );
    }

    public void testIgnoresPartialFirstLineWhenNormalizingFollowingLines() throws Exception {
        String content = "        String placeholder,\n"
            + "        Consumer<String> showError,\n"
            + "        Color background,\n"
            + "        Color foreground\n";
        int start = content.indexOf("ceholder,");
        int end = content.indexOf("Color foreground") + "Color foreground".length();

        assertEquals(
            "          ceholder,\nConsumer<String> showError,\nColor background,\nColor foreground",
            selectedText(content, start, end)
        );
    }

    private String selectedText(String content, int start, int end) throws Exception {
        sourceFile = Files.createTempFile("selection-annotation-context-", ".java").toRealPath();
        Files.writeString(sourceFile, content);
        VfsRootAccess.allowRootAccess(getTestRootDisposable(), sourceFile.toString());
        var file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourceFile);
        assertNotNull(file);
        myFixture.configureFromExistingVirtualFile(file);
        Editor editor = myFixture.getEditor();
        editor.getSelectionModel().setSelection(start, end);

        AnnotationContext context = AnnotationContext.from(editor);

        assertNotNull(context);
        assertEquals(1, context.selections().size());
        return context.selections().getFirst().text();
    }
}
