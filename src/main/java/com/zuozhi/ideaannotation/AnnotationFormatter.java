package com.zuozhi.ideaannotation;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

final class AnnotationFormatter {
    private AnnotationFormatter() {
    }

    static String format(AnnotationContext context, String rawComment) {
        StringBuilder payload = new StringBuilder();
        for (int index = 0; index < context.selections().size(); index++) {
            if (index > 0) {
                payload.append(">\n");
            }
            AnnotationContext.Selection selection = context.selections().get(index);
            payload.append("> ")
                .append(selectionLink(context, selection))
                .append('\n');
            appendCodeBlock(payload, selection.text(), context.language());
        }

        String comment = rawComment.strip();
        payload.append("_User comment:_\n");
        if (comment.isEmpty()) {
            return payload.append("\n\n\n---\n\n").toString();
        }
        return payload.append('\n')
            .append(comment)
            .append("\n\n---\n\n")
            .toString();
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
        String content = removeCommonIndent(text);
        String fence = "`".repeat(Math.max(3, longestBacktickRun(content) + 1));
        payload.append("> ").append(fence).append(language).append('\n');
        Arrays.stream(content.split("\n", -1))
            .forEach(line -> payload.append("> ").append(line).append('\n'));
        payload.append("> ").append(fence).append('\n');
    }

    private static String removeCommonIndent(String text) {
        String[] lines = text.split("\n", -1);
        int firstLine = 0;
        while (firstLine < lines.length && isBlankLine(lines[firstLine])) {
            firstLine++;
        }
        String commonIndent = null;
        for (int index = firstLine; index < lines.length; index++) {
            String line = lines[index];
            if (isBlankLine(line)) {
                continue;
            }
            int indentEnd = 0;
            while (indentEnd < line.length()
                && isIndentCharacter(line.charAt(indentEnd))) {
                indentEnd++;
            }
            String indent = line.substring(0, indentEnd);
            if (commonIndent == null) {
                commonIndent = indent;
            } else {
                int commonLength = 0;
                int maximum = Math.min(commonIndent.length(), indent.length());
                while (commonLength < maximum
                    && commonIndent.charAt(commonLength) == indent.charAt(commonLength)) {
                    commonLength++;
                }
                commonIndent = commonIndent.substring(0, commonLength);
            }
            if (commonIndent.isEmpty()) {
                break;
            }
        }

        int indentLength = commonIndent == null ? 0 : commonIndent.length();
        return String.join("\n", Arrays.stream(lines, firstLine, lines.length)
            .map(line -> isBlankLine(line) ? "" : line.substring(indentLength))
            .toList());
    }

    private static boolean isBlankLine(String line) {
        return line.chars().allMatch(character -> isIndentCharacter((char) character));
    }

    private static boolean isIndentCharacter(char character) {
        return character == ' ' || character == '\t';
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
