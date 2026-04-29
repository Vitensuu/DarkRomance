package com.team7.game1.models;

public enum FacingDirection {
    DOWN("front"),
    LEFT("left"),
    RIGHT("right"),
    UP("back");

    private final String assetDirection;

    FacingDirection(String assetDirection) {
        this.assetDirection = assetDirection;
    }

    public String getAssetDirection() {
        return assetDirection;
    }
}
