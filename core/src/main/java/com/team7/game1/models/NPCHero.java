package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class NPCHero extends NPC {

    public static final HeroProfile ROBE_ARCHER = new HeroProfile(
        AnimationPreset.DARK_SKINNED,
        CharacterAnimator.AnimationState.ATTACK_BOW,
        0.8f,
        Color.valueOf("6E5B45FF"),
        Color.valueOf("C08A4AFF")
    );

    public static final HeroProfile MIXED_METAL_ARCHER = new HeroProfile(
        AnimationPreset.MIXED_METAL,
        CharacterAnimator.AnimationState.ATTACK_BOW,
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
            profile.animationPreset
        );
        this.profile = profile;
    }

    @Override
    public Color getBaseColor() {
        return isChasingPlayer() ? profile.chaseColor : profile.idleColor;
    }

    @Override
    public String getDialogTitle() {
        if (profile == ROBE_ARCHER) {
            return "Archer";
        }
        if (profile == MIXED_METAL_ARCHER) {
            return "Guardian";
        }
        return "Hero";
    }

    @Override
    public String getDialogText() {
        if (profile == ROBE_ARCHER) {
            return "Keep your voice down. The woods are listening tonight.";
        }
        if (profile == MIXED_METAL_ARCHER) {
            return "If you are heading deeper inside, stay ready and do not travel alone.";
        }
        return "Stay alert.";
    }

    @Override
    public String getDialogueId() {
        if (profile == ROBE_ARCHER) {
            return "archer";
        }
        if (profile == MIXED_METAL_ARCHER) {
            return "guardian";
        }
        return "default";
    }

    @Override
    protected void onReachPlayer(PlayerCharacter player) {
        // Heroes no longer attack the player on proximity.
    }

    public static class HeroProfile {
        private final AnimationPreset animationPreset;
        private final CharacterAnimator.AnimationState contactAnimation;
        private final float contactAnimationDurationSeconds;
        private final Color idleColor;
        private final Color chaseColor;

        public HeroProfile(AnimationPreset animationPreset, CharacterAnimator.AnimationState contactAnimation,
                           float contactAnimationDurationSeconds, Color idleColor, Color chaseColor) {
            this.animationPreset = animationPreset;
            this.contactAnimation = contactAnimation;
            this.contactAnimationDurationSeconds = contactAnimationDurationSeconds;
            this.idleColor = idleColor.cpy();
            this.chaseColor = chaseColor.cpy();
        }
    }
}
