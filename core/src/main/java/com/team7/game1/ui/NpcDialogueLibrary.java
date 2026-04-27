package com.team7.game1.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class NpcDialogueLibrary implements Disposable {

    private static final String DIALOGUE_FILE_PATH = "ui/dialog/dialogues.json";
    private static final String DEFAULT_DIALOGUE_ID = "default";

    private final ObjectMap<String, NpcDialogueEntry> entries = new ObjectMap<String, NpcDialogueEntry>();
    private final ObjectMap<String, Texture> portraitCache = new ObjectMap<String, Texture>();

    public NpcDialogueLibrary() {
        loadEntries();
    }

    public NpcDialogueEntry getEntry(String dialogueId) {
        NpcDialogueEntry entry = entries.get(dialogueId);
        if (entry != null) {
            return entry;
        }
        return entries.get(DEFAULT_DIALOGUE_ID);
    }

    public Texture getPortrait(String dialogueId) {
        NpcDialogueEntry entry = getEntry(dialogueId);
        if (entry == null || entry.getPortraitPath() == null || entry.getPortraitPath().isEmpty()) {
            return null;
        }

        Texture cachedTexture = portraitCache.get(entry.getPortraitPath());
        if (cachedTexture != null) {
            return cachedTexture;
        }

        FileHandle portraitFile = Gdx.files.internal(entry.getPortraitPath());
        if (!portraitFile.exists()) {
            Gdx.app.log("NpcDialogueLibrary", "Portrait texture not found: " + entry.getPortraitPath());
            return null;
        }

        Texture portraitTexture = new Texture(portraitFile);
        portraitCache.put(entry.getPortraitPath(), portraitTexture);
        return portraitTexture;
    }

    @Override
    public void dispose() {
        for (Texture texture : portraitCache.values()) {
            texture.dispose();
        }
        portraitCache.clear();
    }

    private void loadEntries() {
        FileHandle dialogueFile = Gdx.files.internal(DIALOGUE_FILE_PATH);
        if (!dialogueFile.exists()) {
            throw new GdxRuntimeException("Dialogue file not found: " + DIALOGUE_FILE_PATH);
        }

        JsonValue root = new JsonReader().parse(dialogueFile);
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            String name = entry.getString("name", "NPC");
            String portraitPath = entry.getString("portrait", null);
            JsonValue linesValue = entry.get("lines");
            String[] lines = new String[] { "..." };
            if (linesValue != null && linesValue.size > 0) {
                lines = new String[linesValue.size];
                int index = 0;
                for (JsonValue line = linesValue.child; line != null; line = line.next) {
                    lines[index++] = line.asString();
                }
            }
            entries.put(entry.name, new NpcDialogueEntry(name, portraitPath, lines));
        }

        if (!entries.containsKey(DEFAULT_DIALOGUE_ID)) {
            throw new GdxRuntimeException("Dialogue file must contain '" + DEFAULT_DIALOGUE_ID + "' entry");
        }
    }
}
