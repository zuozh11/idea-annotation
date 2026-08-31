package com.zuozhi.ideaannotation;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

final class AnnotationFormatter {
    private AnnotationFormatter() {
    }

    static String format(AnnotationContext context, String rawComment) {
        StringBuilder payload = new StringBuilder();
        boolean multiple = context.selections().size() > 1;
        for (int index = 0; index < context.selections().size(); index++) {
            if (index > 0) {
                payload.append(">\n");
            }
            AnnotationContext.Selection selection = context.selections().get(index);
            payload.append("> **Selection");
            if (multiple) {
                payload.append(' ').append(index + 1);
            }
            payload.append(":**\n")
                .append("> ")
                .append(selectionLink(context, selection))
                .append('\n');
            appendCodeBlock(payload, selection.text(), context.language());
        }

        String comment = rawComment.strip();
        if (!comment.isEmpty()) {
            payload.append(">\n> **User comment:**\n");
            appendCodeBlock(payload, comment, "");
        }
        return payload.append("\n---\n\n").toString();
    }

    static String formatPaths(List<VirtualFile> files) {
        List<String> links = files.stream()
            .map(file -> markdownLink(file.getName(), file.toNioPath().toString()))
            .toList();
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

    private static String markdownLink(String text, String absolutePath) {
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
