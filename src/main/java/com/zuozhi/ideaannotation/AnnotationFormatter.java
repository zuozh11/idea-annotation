package com.zuozhi.ideaannotation;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

final class AnnotationFormatter {
    private AnnotationFormatter() {
    }

    static String format(AnnotationContext context, String rawComment) {
        String comment = rawComment.strip();
        if (comment.isEmpty() && context.selections().stream().allMatch(
            AnnotationContext.Selection::wholeLine
        )) {
            return formatLinks(context.selections().stream()
                .map(selection -> selectionLink(context, selection))
                .toList());
        }

        StringBuilder payload = new StringBuilder("**_User comment:_**\n");
        if (!comment.isEmpty()) {
            payload.append(comment).append('\n');
        } else {
            payload.append('\n');
        }
        payload.append('\n');

        boolean multipleSelections = context.selections().size() > 1;
        for (int index = 0; index < context.selections().size(); index++) {
            if (index > 0) {
                payload.append(">\n");
            }
            AnnotationContext.Selection selection = context.selections().get(index);
            payload.append("> _Source");
            if (multipleSelections) {
                payload.append(' ').append(index + 1);
            }
            payload.append(":_\n")
                .append("> ")
                .append(selectionLink(context, selection))
                .append('\n');
            if (!selection.wholeLine()) {
                appendCodeBlock(payload, selection.text(), context.language());
            }
        }

        return payload.append("\n---\n\n").toString();
    }

    static String formatPaths(List<VirtualFile> files) {
        return formatLinks(files.stream()
            .map(file -> markdownLink(file.getName(), file.toNioPath().toString()))
            .toList());
    }

    private static String formatLinks(List<String> links) {
        if (links.size() == 1) {
            return " " + links.getFirst() + " ";
        }
        return String.join("\n", links);
    }

    private static String selectionLink(
        AnnotationContext context,
        AnnotationContext.Selection selection
    ) {
        String lineLabel = selection.startLine() == selection.endLine()
            ? "line " + selection.startLine()
            : "lines " + selection.startLine() + "-" + selection.endLine();
        return markdownLink(
            context.fileName() + " (" + lineLabel + ")",
            context.absolutePath()
        );
    }

    static String markdownLink(String text, String absolutePath) {
        String escapedText = text
            .replace("\\", "\\\\")
            .replace("[", "\\[")
            .replace("]", "\\]");
        String target = absolutePath.indexOf(' ') >= 0
            ? "<" + absolutePath.replace("<", "%3C").replace(">", "%3E") + ">"
            : absolutePath
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
        return "[" + escapedText + "](" + target + ")";
    }

    private static void appendCodeBlock(StringBuilder payload, String text, String language) {
        String fence = "`".repeat(Math.max(3, longestBacktickRun(text) + 1));
        payload.append("> ").append(fence).append(language).append('\n');
        Arrays.stream(text.split("\n", -1))
            .forEach(line -> payload.append("> ").append(line).append('\n'));
        payload.append("> ").append(fence).append('\n');
    }

    private static int longestBacktickRun(String text) {
        int longest = 0;
        int current = 0;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '`') {
                longest = Math.max(longest, ++current);
            } else {
                current = 0;
            }
        }
        return longest;
    }
}
