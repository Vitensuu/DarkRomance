package com.team7.game1.ui;

import java.util.Arrays;

public class NpcDialogueEntry {

    private static final String[] DEFAULT_LINES = {"..."};

    private final String name;
    private final String[] lines;

    public NpcDialogueEntry(String name, String[] lines) {
        this.name = normalizeName(name);
        this.lines = normalizeLines(lines);
    }

    public String getName() {
        return name;
    }

    public String[] getLines() {
        return Arrays.copyOf(lines, lines.length);
    }

    public String getLine(int index) {
        if (index <= 0) {
            return lines[0];
        }
        if (index >= lines.length) {
            return lines[lines.length - 1];
        }
        return lines[index];
    }

    public int getLineCount() {
        return lines.length;
    }

    private String normalizeName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "НПС";
        }
        return rawName.trim();
    }

    private String[] normalizeLines(String[] sourceLines) {
        if (sourceLines == null || sourceLines.length == 0) {
            return DEFAULT_LINES;
        }

        String[] copy = new String[sourceLines.length];
        int filledCount = 0;
        for (String sourceLine : sourceLines) {
            if (sourceLine == null) {
                continue;
            }
            String normalized = sourceLine.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            copy[filledCount++] = normalized;
        }

        if (filledCount == 0) {
            return DEFAULT_LINES;
        }

        if (filledCount == copy.length) {
            return copy;
        }

        String[] compact = new String[filledCount];
        System.arraycopy(copy, 0, compact, 0, filledCount);
        return compact;
    }
}
