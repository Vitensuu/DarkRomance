package com.team7.game1.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.team7.game1.GameConfig;

public class NpcDialogueLibrary implements Disposable {

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
        return entries.get(GameConfig.Dialog.DEFAULT_DIALOGUE_ID);
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
        FileHandle dialogueFile = Gdx.files.internal(GameConfig.Dialog.DATA_PATH);
        if (!dialogueFile.exists()) {
            throw new GdxRuntimeException("Dialogue file not found: " + GameConfig.Dialog.DATA_PATH);
        }

        JsonValue root = new JsonReader().parse(dialogueFile);
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            String name = entry.getString("name", "НПС");
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

        if (!entries.containsKey(GameConfig.Dialog.DEFAULT_DIALOGUE_ID)) {
            throw new GdxRuntimeException("Dialogue file must contain '" + GameConfig.Dialog.DEFAULT_DIALOGUE_ID + "' entry");
        }
    }
}
