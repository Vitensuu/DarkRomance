package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;

public class NPCHero extends NPC {

    public static final HeroProfile HERO_PROFILE = new HeroProfile(
        AnimationPreset.DARK_SKINNED,
        Color.valueOf("6E5B45FF"),
        Color.valueOf("C08A4AFF"),
        "Герой",
        "Hero",
        "Так ты и есть тот самый темный маг...."
    );

    public static final HeroProfile GUARDIAN_PROFILE = new HeroProfile(
        AnimationPreset.MIXED_METAL,
        Color.valueOf("5E6A74FF"),
        Color.valueOf("93A9BBFF"),
        "Страж",
        "guardian",
        "Приветствую вас, игрок."
    );

    private final HeroProfile profile;

    public NPCHero(float startX, float startY,
                   float patrolMinX, float patrolMinY,
                   float patrolMaxX, float patrolMaxY,
                   HeroProfile profile) {
        super(
            startX,
            startY,
            patrolMinX,
            patrolMinY,
            patrolMaxX,
            patrolMaxY,
            95f,
            170f,
            44f,
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
        return profile.dialogTitle;
    }

    @Override
    public String getDialogText() {
        return profile.fallbackDialogLine;
    }

    @Override
    public String getDialogueId() {
        return profile.dialogueId;
    }

    @Override
    protected void onReachPlayer(PlayerCharacter player) {
        // Hero/guardian NPCs do not auto-attack on proximity.
    }

    public static class HeroProfile {
        private final AnimationPreset animationPreset;
        private final Color idleColor;
        private final Color chaseColor;
        private final String dialogTitle;
        private final String dialogueId;
        private final String fallbackDialogLine;

        public HeroProfile(AnimationPreset animationPreset,
                           Color idleColor,
                           Color chaseColor,
                           String dialogTitle,
                           String dialogueId,
                           String fallbackDialogLine) {
            this.animationPreset = animationPreset;
            this.idleColor = idleColor.cpy();
            this.chaseColor = chaseColor.cpy();
            this.dialogTitle = dialogTitle;
            this.dialogueId = dialogueId;
            this.fallbackDialogLine = fallbackDialogLine;
        }
    }
}
