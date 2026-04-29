package com.team7.game1.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class PlayerCharacter {

    private static final float DEFAULT_SPEED = 180f;

    private final CharacterAnimator animator;
    private final Vector2 position = new Vector2();
    private final Vector2 movement = new Vector2();

    private FacingDirection facing = FacingDirection.DOWN;
    private CharacterAnimator.AnimationState animationState = CharacterAnimator.AnimationState.BASE;
    private String equippedWeapon = "Unarmed";
    private float animationStateTimer;
    private float locomotionTime;
    private float actionTime;
    private float speed = DEFAULT_SPEED;

    public PlayerCharacter(float startX, float startY) {
        this(startX, startY, AnimationPreset.ROBE);
    }

    public PlayerCharacter(float startX, float startY, AnimationPreset preset) {
        animator = new CharacterAnimator(preset, 3.4f);
        position.set(startX, startY);
    }

    public void update(float delta, float moveX, float moveY, float worldWidth, float worldHeight) {
        updateAnimationState(delta);

        movement.set(moveX, moveY);
        boolean moving = movement.len2() > 0f;

        if (moving) {
            movement.nor().scl(speed * delta);
            position.add(movement);
            updateFacing(moveX, moveY);
            locomotionTime += delta;
        } else {
            locomotionTime = 0f;
        }

        if (animationState.isActionAnimation()) {
            actionTime += delta;
        }

        float halfWidth = getDrawWidth() / 2f;
        position.x = Math.max(halfWidth, Math.min(position.x, worldWidth - halfWidth));
        position.y = Math.max(0f, Math.min(position.y, worldHeight - getDrawHeight()));
    }

    public TextureRegion getCurrentFrame() {
        return animator.getFrame(facing, isMoving(), locomotionTime, actionTime, animationState);
    }

    public float getX() {
        return position.x - getDrawWidth() / 2f;
    }

    public float getY() {
        return position.y;
    }

    public float getCenterX() {
        return position.x;
    }

    public float getCenterY() {
        return position.y + getDrawHeight() / 2f;
    }

    public float getSpeed() {
        return speed;
    }

    public Rectangle getCollisionBounds(Rectangle outBounds) {
        outBounds.set(getX(), getY(), getDrawWidth(), getDrawHeight());
        return outBounds;
    }

    public Rectangle getCollisionBoundsAt(float bottomLeftX, float bottomLeftY, Rectangle outBounds) {
        outBounds.set(bottomLeftX, bottomLeftY, getDrawWidth(), getDrawHeight());
        return outBounds;
    }

    public void setBottomLeft(float x, float y) {
        position.x = x + getDrawWidth() / 2f;
        position.y = y;
    }

    public float getDrawWidth() {
        return animator.getDrawWidth(facing, isMoving(), locomotionTime, actionTime, animationState);
    }

    public float getDrawHeight() {
        return animator.getDrawHeight(facing, isMoving(), locomotionTime, actionTime, animationState);
    }

    public void triggerAnimationState(CharacterAnimator.AnimationState newState, float durationSeconds) {
        animationState = newState;
        animationStateTimer = durationSeconds;
        actionTime = 0f;
        if (newState == CharacterAnimator.AnimationState.ATTACK_BOW) {
            equippedWeapon = "Bow";
        } else if (newState == CharacterAnimator.AnimationState.ATTACK_MAGIC) {
            equippedWeapon = "Magic";
        }
    }

    public boolean isAnimationStateActive(CharacterAnimator.AnimationState state) {
        return animationState == state;
    }

    public String getEquippedWeapon() {
        return equippedWeapon;
    }

    public float getActionAnimationDuration(CharacterAnimator.AnimationState state) {
        return animator.getActionDuration(state, facing);
    }

    public void dispose() {
        animator.dispose();
    }

    private boolean isMoving() {
        return movement.len2() > 0f;
    }

    private void updateAnimationState(float delta) {
        if (animationState == CharacterAnimator.AnimationState.BASE) {
            return;
        }

        animationStateTimer -= delta;
        if (animationStateTimer <= 0f) {
            animationState = CharacterAnimator.AnimationState.BASE;
            animationStateTimer = 0f;
            actionTime = 0f;
        }
    }

    private void updateFacing(float moveX, float moveY) {
        if (Math.abs(moveX) > Math.abs(moveY)) {
            facing = moveX < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT;
        } else if (Math.abs(moveY) > 0f) {
            facing = moveY < 0f ? FacingDirection.DOWN : FacingDirection.UP;
        }
    }
}
