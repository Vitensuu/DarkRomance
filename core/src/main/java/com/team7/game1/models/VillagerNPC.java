package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class VillagerNPC extends NPC {

    private static final AnimationPreset DEFAULT_PRESET = AnimationPreset.MIXED_METAL;
    private boolean greetingTriggered;

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
        return "Villager";
    }

    @Override
    public String getDialogText() {
        return "Good evening. Strange things have been happening near the village.";
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
}
