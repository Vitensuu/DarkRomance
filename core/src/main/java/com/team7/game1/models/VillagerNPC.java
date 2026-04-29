package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class VillagerNPC extends NPC {

    private static final AnimationPreset DEFAULT_PRESET = AnimationPreset.MIXED_METAL;
    private boolean greetingTriggered;
    private String playerName = "Player";

    public VillagerNPC(float startX, float startY,
                       float patrolMinX, float patrolMinY,
                       float patrolMaxX, float patrolMaxY) {
        this(startX, startY, patrolMinX, patrolMinY, patrolMaxX, patrolMaxY,
            DEFAULT_PRESET);
    }

    public VillagerNPC(float startX, float startY,
                       float patrolMinX, float patrolMinY,
                       float patrolMaxX, float patrolMaxY,
                       AnimationPreset animationPreset) {
        super(startX, startY, patrolMinX, patrolMinY, patrolMaxX, patrolMaxY,
            95f, 170f, 44f, animationPreset);
    }

    @Override
    public Color getBaseColor() {
        return isChasingPlayer() ? Color.valueOf("D98C5FFF") : Color.valueOf("8E6A42FF");
    }

    @Override
    public String getDialogTitle() {
        return "Житель?";
    }

    @Override
    public String getDialogText() {
        return "Приветствую вас, " + playerName + "!";
    }

    @Override
    public String getDialogueId() {
        return "villager";
    }

    @Override
    protected void onReachPlayer(PlayerCharacter player) {
        greetingTriggered = true;
    }

    public boolean isGreetingTriggered() {
        return greetingTriggered;
    }

    public void setPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            this.playerName = "Player";
            return;
        }
        this.playerName = playerName.trim();
    }
}
