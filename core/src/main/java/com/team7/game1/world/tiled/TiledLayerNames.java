package com.team7.game1.world.tiled;

public final class TiledLayerNames {

    public static final String GROUND = "Ground";
    public static final String DECORATION = "Decoration";
    public static final String COLLISION = "Collision";
    public static final String OBJECTS = "Objects";
    public static final String ABOVE_PLAYER = "AbovePlayer";
    public static final String NPC = "NPC";
    public static final String TRIGGERS = "Triggers";
    public static final String CLIMB = "Climb";

    public static final String[] BELOW_PLAYER_LAYERS = {
        GROUND,
        DECORATION,
        COLLISION,
        OBJECTS,
        NPC,
        TRIGGERS
    };

    public static final String[] ABOVE_PLAYER_LAYERS = {
        ABOVE_PLAYER
    };

    public static final String START_GROUP = "start";
    public static final String START_OBJECT_LAYER = "object";

    private TiledLayerNames() {
    }
}
