package com.team7.game1.models;

public enum CharacterAnimationState {
    BASE("", false, true, 0),
    WAKE_UP("wakeup", true, false, 6),
    ATTACK_MAGIC("magic", true, false, 8),
    ATTACK_BOW("bow", true, false, 8);

    private final String suffix;
    private final boolean actionAnimation;
    private final boolean looping;
    private final int actionFrameCount;

    CharacterAnimationState(String suffix, boolean actionAnimation, boolean looping, int actionFrameCount) {
        this.suffix = suffix;
        this.actionAnimation = actionAnimation;
        this.looping = looping;
        this.actionFrameCount = actionFrameCount;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean isActionAnimation() {
        return actionAnimation;
    }

    public boolean isLooping() {
        return looping;
    }

    public int getActionFrameCount() {
        return actionFrameCount;
    }
}
