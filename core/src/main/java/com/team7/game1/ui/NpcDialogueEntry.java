package com.team7.game1.ui;

public class NpcDialogueEntry {

    private final String name;
    private final String portraitPath;
    private final String[] lines;

    public NpcDialogueEntry(String name, String portraitPath, String[] lines) {
        this.name = name;
        this.portraitPath = portraitPath;
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    public String[] getLines() {
        return lines;
    }
}
