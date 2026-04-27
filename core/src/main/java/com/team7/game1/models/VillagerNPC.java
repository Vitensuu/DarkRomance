package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class VillagerNPC extends NPC {

    private static final String DEFAULT_ASSET_FOLDER = "characters/mixed_metal";
    private static final String DEFAULT_ASSET_PREFIX = "mm";
    private boolean greetingTriggered;

    public VillagerNPC(float startX, float startY,
                       float patrolMinX, float patrolMinY,
                       float patrolMaxX, float patrolMaxY) {
        this(startX, startY, patrolMinX, patrolMinY, patrolMaxX, patrolMaxY,
            DEFAULT_ASSET_FOLDER, DEFAULT_ASSET_PREFIX);
    }

    public VillagerNPC(float startX, float startY,
                       float patrolMinX, float patrolMinY,
                       float patrolMaxX, float patrolMaxY,
                       String assetFolder, String assetPrefix) {
        super(startX, startY, patrolMinX, patrolMinY, patrolMaxX, patrolMaxY,
            95f, 170f, 44f, assetFolder, assetPrefix);
    }

    @Override
    public Color getBaseColor() {
        return isChasingPlayer() ? Color.valueOf("D98C5FFF") : Color.valueOf("8E6A42FF");
    }

    @Override
    protected void onReachPlayer(PlayerCharacter player) {
        greetingTriggered = true;
    }

    public boolean isGreetingTriggered() {
        return greetingTriggered;
    }
}
