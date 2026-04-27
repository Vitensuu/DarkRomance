package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class NPCHero extends NPC {

    public static final HeroProfile ROBE_ARCHER = new HeroProfile(
        "characters/robe_frames",
        "robe",
        CharacterAnimationState.ATTACK_BOW,
        0.8f,
        Color.valueOf("6E5B45FF"),
        Color.valueOf("C08A4AFF")
    );

    public static final HeroProfile MIXED_METAL_ARCHER = new HeroProfile(
        "characters/mixed_metal",
        "mm",
        CharacterAnimationState.ATTACK_BOW,
        0.9f,
        Color.valueOf("5E6A74FF"),
        Color.valueOf("93A9BBFF")
    );

    private final HeroProfile profile;

    public NPCHero(float startX, float startY,
                   float patrolMinX, float patrolMinY,
                   float patrolMaxX, float patrolMaxY,
                   HeroProfile profile) {
        super(
            startX, startY, patrolMinX, patrolMinY, patrolMaxX, patrolMaxY,
            95f, 170f, 44f,
            profile.assetFolder, profile.assetPrefix
        );
        this.profile = profile;
    }

    @Override
    public Color getBaseColor() {
        return isChasingPlayer() ? profile.chaseColor : profile.idleColor;
    }

    @Override
    protected void onReachPlayer(PlayerCharacter player) {
        if (!isActionAnimationPlaying() && profile.contactAnimation != CharacterAnimationState.BASE) {
            triggerAnimationState(profile.contactAnimation, profile.contactAnimationDurationSeconds);
        }
    }

    public static class HeroProfile {
        private final String assetFolder;
        private final String assetPrefix;
        private final CharacterAnimationState contactAnimation;
        private final float contactAnimationDurationSeconds;
        private final Color idleColor;
        private final Color chaseColor;

        public HeroProfile(String assetFolder, String assetPrefix, CharacterAnimationState contactAnimation,
                           float contactAnimationDurationSeconds, Color idleColor, Color chaseColor) {
            this.assetFolder = assetFolder;
            this.assetPrefix = assetPrefix;
            this.contactAnimation = contactAnimation;
            this.contactAnimationDurationSeconds = contactAnimationDurationSeconds;
            this.idleColor = idleColor.cpy();
            this.chaseColor = chaseColor.cpy();
        }
    }
}
