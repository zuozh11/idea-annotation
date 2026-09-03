package com.zuozhi.ideaannotation;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        List<SelectionGroup> groups = groupSelections(context.selections());
        StringBuilder payload = new StringBuilder();
        boolean multipleGroups = groups.size() > 1;
        for (int index = 0; index < groups.size(); index++) {
            SelectionGroup group = groups.get(index);
            payload.append("> _Source");
            if (multipleGroups) {
                payload.append(' ').append(index + 1);
            }
            payload.append(":_ ")
                .append(String.join(" ", group.selections().stream()
                    .map(selection -> selectionLink(context, selection))
                    .toList()))
                .append('\n');
            if (group.selections().stream().anyMatch(selection -> !selection.wholeLine())) {
                appendCodeBlock(payload, group.text(), context.language());
            }
        }

        payload.append("\n**_comment:_**");
        if (!comment.isEmpty()) {
            payload.append(' ').append(comment);
        }
        return payload.append('\n').toString();
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

    private static List<SelectionGroup> groupSelections(
        List<AnnotationContext.Selection> selections
    ) {
        Map<String, List<AnnotationContext.Selection>> selectionsByText = new LinkedHashMap<>();
        for (AnnotationContext.Selection selection : selections) {
            selectionsByText.computeIfAbsent(selection.text(), ignored -> new ArrayList<>())
                .add(selection);
        }
        return selectionsByText.entrySet().stream()
            .map(entry -> new SelectionGroup(entry.getKey(), entry.getValue()))
            .toList();
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

    private record SelectionGroup(
        String text,
        List<AnnotationContext.Selection> selections
    ) {
    }
}
