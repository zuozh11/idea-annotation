package com.zuozhi.ideaannotation;

final class AnnotationFormatter {
    private AnnotationFormatter() {
    }

    static String format(AnnotationContext context, String rawComment) {
        String comment = rawComment.strip();
        StringBuilder payload = new StringBuilder()
            .append("> **Source:**\n")
            .append("> ").append(context.source()).append('\n')
            .append(">\n")
            .append("> **Selected text:**\n");
        appendQuotedLines(payload, context.selectedText());
        payload.append(">\n")
            .append("> **User comment:**\n");
        if (!comment.isEmpty()) {
            appendQuotedLines(payload, comment);
        }
        return payload.append(">\n").toString();
    }

    private static void appendQuotedLines(StringBuilder payload, String text) {
        String[] lines = text.split("\n", -1);
        for (String line : lines) {
            payload.append("> ").append(line).append('\n');
        }
    }
}
