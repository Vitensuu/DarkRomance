package com.team7.game1.models;

public enum AnimationPreset {
    ROBE("characters/robe_frames", "robe"),
    DARK_SKINNED("characters/dark_skinned", "ds"),
    MIXED_METAL("characters/mixed_metal", "mm");

    private final String assetFolder;
    private final String assetPrefix;

    AnimationPreset(String assetFolder, String assetPrefix) {
        this.assetFolder = assetFolder;
        this.assetPrefix = assetPrefix;
    }

    public String getAssetFolder() {
        return assetFolder;
    }

    public String getAssetPrefix() {
        return assetPrefix;
    }
}
