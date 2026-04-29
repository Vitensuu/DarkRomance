package com.team7.game1.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.team7.game1.GameConfig;

public class NpcDialogueLibrary implements Disposable {

    private final ObjectMap<String, NpcDialogueEntry> entries = new ObjectMap<String, NpcDialogueEntry>();

    public NpcDialogueLibrary() {
        loadEntries();
    }

    public NpcDialogueEntry getEntry(String dialogueId) {
        if (dialogueId != null) {
            NpcDialogueEntry entry = entries.get(dialogueId);
            if (entry != null) {
                return entry;
            }
        }
        return entries.get(GameConfig.Dialog.DEFAULT_DIALOGUE_ID);
    }

    @Override
    public void dispose() {
        entries.clear();
    }

    private void loadEntries() {
        FileHandle dialogueFile = Gdx.files.internal(GameConfig.Dialog.DATA_PATH);
        if (!dialogueFile.exists()) {
            throw new GdxRuntimeException("Dialogue file not found: " + GameConfig.Dialog.DATA_PATH);
        }

        JsonValue root = new JsonReader().parse(dialogueFile);
        for (JsonValue entryValue = root.child; entryValue != null; entryValue = entryValue.next) {
            String name = entryValue.getString("name", "НПС");
            String[] lines = readLines(entryValue.get("lines"));
            entries.put(entryValue.name, new NpcDialogueEntry(name, lines));
        }

        if (!entries.containsKey(GameConfig.Dialog.DEFAULT_DIALOGUE_ID)) {
            throw new GdxRuntimeException(
                "Dialogue file must contain '" + GameConfig.Dialog.DEFAULT_DIALOGUE_ID + "' entry"
            );
        }
    }

    private String[] readLines(JsonValue linesValue) {
        if (linesValue == null || linesValue.size == 0) {
            return new String[] {"..."};
        }

        String[] lines = new String[linesValue.size];
        int index = 0;
        for (JsonValue lineValue = linesValue.child; lineValue != null; lineValue = lineValue.next) {
            lines[index++] = lineValue.asString();
        }
        return lines;
    }
}
