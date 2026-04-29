package com.team7.game1.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.team7.game1.models.PlayerData;

public class SaveManager {

    private static final String SAVE_DIRECTORY = "saves";
    private static final String SAVE_FILE_SUFFIX = ".json";

    private SaveManager() {
    }

    public static PlayerData loadOrCreatePlayer(String username) {
        PlayerData loaded = loadPlayer(username);
        return loaded != null ? loaded : new PlayerData(username);
    }

    public static PlayerData loadPlayer(String username) {
        FileHandle saveFile = getPlayerSaveFile(username);
        if (!saveFile.exists()) {
            return null;
        }

        try {
            JsonValue root = new JsonReader().parse(saveFile);
            PlayerData playerData = new PlayerData(root.getString("username", username));
            playerData.setMaxHealth(root.getInt("maxHealth", PlayerData.DEFAULT_MAX_HEALTH));
            playerData.setHealth(root.getInt("health", playerData.getMaxHealth()));
            playerData.setLevel(root.getInt("level", PlayerData.DEFAULT_STARTING_LEVEL));
            playerData.setScore(root.getInt("score", 0));
            playerData.setCoins(root.getInt("coins", 0));
            playerData.setWorldPosition(root.getFloat("worldX", 0f), root.getFloat("worldY", 0f));
            return playerData;
        } catch (Exception exception) {
            Gdx.app.error("SaveManager", "Failed to load player save: " + saveFile.path(), exception);
            return new PlayerData(username);
        }
    }

    public static void savePlayer(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        FileHandle saveFile = getPlayerSaveFile(playerData.getUsername());
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);

            SavePayload payload = new SavePayload();
            payload.username = playerData.getUsername();
            payload.health = playerData.getHealth();
            payload.maxHealth = playerData.getMaxHealth();
            payload.level = playerData.getLevel();
            payload.score = playerData.getScore();
            payload.coins = playerData.getCoins();
            payload.worldX = playerData.getWorldX();
            payload.worldY = playerData.getWorldY();

            saveFile.writeString(json.prettyPrint(payload), false, "UTF-8");
        } catch (Exception exception) {
            Gdx.app.error("SaveManager", "Failed to save player data: " + saveFile.path(), exception);
        }
    }

    private static FileHandle getPlayerSaveFile(String username) {
        FileHandle directory = Gdx.files.local(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.child(normalizeUsername(username) + SAVE_FILE_SUFFIX);
    }

    private static String normalizeUsername(String username) {
        String source = username == null ? "player" : username.trim().toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            if (Character.isLetterOrDigit(current)) {
                builder.append(current);
            } else if (current == ' ' || current == '-' || current == '_') {
                builder.append('_');
            }
        }

        if (builder.length() == 0) {
            return "player";
        }
        return builder.toString();
    }

    private static class SavePayload {
        public String username;
        public int health;
        public int maxHealth;
        public int level;
        public int score;
        public int coins;
        public float worldX;
        public float worldY;
    }
}
