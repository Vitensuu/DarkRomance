package com.team7.game1.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class NPC implements Interactable {

    private static final float ARRIVAL_EPSILON = 4f;

    protected final Vector2 position = new Vector2();
    protected final Vector2 patrolMin = new Vector2();
    protected final Vector2 patrolMax = new Vector2();
    protected final Vector2 targetPoint = new Vector2();
    protected final Vector2 scratch = new Vector2();

    protected float moveSpeed;
    protected float detectionRadius;
    protected float interactionRadius;

    private final CharacterAnimator animator;
    private PatrolCorner currentCorner = PatrolCorner.BOTTOM_RIGHT;
    private boolean chasingPlayer;
    private FacingDirection facing = FacingDirection.DOWN;
    private CharacterAnimator.AnimationState animationState = CharacterAnimator.AnimationState.BASE;
    private float animationStateTimer;
    private float locomotionTime;
    private float actionTime;
    private boolean moving;

    protected NPC(float startX, float startY,
                  float patrolMinX, float patrolMinY,
                  float patrolMaxX, float patrolMaxY,
                  float moveSpeed, float detectionRadius, float interactionRadius,
                  AnimationPreset animationPreset) {
        position.set(startX, startY);
        patrolMin.set(patrolMinX, patrolMinY);
        patrolMax.set(patrolMaxX, patrolMaxY);
        this.moveSpeed = moveSpeed;
        this.detectionRadius = detectionRadius;
        this.interactionRadius = interactionRadius;
        targetPoint.set(patrolMaxX, patrolMinY);
        animator = new CharacterAnimator(animationPreset, 3.1f);
    }

    public final void update(float delta, PlayerCharacter player) {
        updateAnimationState(delta);

        float playerDistance = position.dst(player.getCenterX(), player.getCenterY());
        boolean movedThisFrame;

        if (shouldApproachPlayer(player, playerDistance)) {
            chasingPlayer = true;
            movedThisFrame = moveTowards(delta, player.getCenterX(), player.getCenterY());
            if (playerDistance <= interactionRadius) {
                onReachPlayer(player);
            }
        } else {
            chasingPlayer = false;
            movedThisFrame = updatePatrol(delta);
        }

        moving = movedThisFrame;
        if (movedThisFrame) {
            locomotionTime += delta;
        } else {
            locomotionTime = 0f;
        }

        if (animationState.isActionAnimation()) {
            actionTime += delta;
        }
    }

    public float getX() {
        return position.x - getWidth() / 2f;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return animator.getDrawWidth(facing, moving, locomotionTime, actionTime, animationState);
    }

    public float getHeight() {
        return animator.getDrawHeight(facing, moving, locomotionTime, actionTime, animationState);
    }

    public boolean isChasingPlayer() {
        return chasingPlayer;
    }

    public TextureRegion getCurrentFrame() {
        return animator.getFrame(facing, moving, locomotionTime, actionTime, animationState);
    }

    public TextureRegion getDialoguePortraitFrame() {
        return getCurrentFrame();
    }

    public String getDialogTitle() {
        return "NPC";
    }

    public String getDialogText() {
        return "Hello there.";
    }

    public String getDialogueId() {
        return "default";
    }

    public abstract Color getBaseColor();

    @Override
    public boolean canInteract(PlayerCharacter player) {
        if (player == null) {
            return false;
        }
        return distanceTo(player) <= getInteractionDistance();
    }

    @Override
    public void interact(PlayerCharacter player) {
        onInteract(player);
    }

    @Override
    public String getInteractionPrompt() {
        return "Talk";
    }

    protected boolean shouldApproachPlayer(PlayerCharacter player, float playerDistance) {
        return false;
    }

    protected void onInteract(PlayerCharacter player) {
        onReachPlayer(player);
    }

    protected void onReachPlayer(PlayerCharacter player) {
        // By default, common NPCs just come closer.
        // Specific NPCs can override this to talk, start quests, attack, etc.
    }

    protected float getInteractionDistance() {
        return Math.max(interactionRadius, 115f);
    }

    protected void triggerAnimationState(CharacterAnimator.AnimationState newState, float durationSeconds) {
        animationState = newState;
        animationStateTimer = durationSeconds;
        actionTime = 0f;
    }

    protected boolean isActionAnimationPlaying() {
        return animationState.isActionAnimation();
    }

    public void dispose() {
        animator.dispose();
    }

    private float distanceTo(PlayerCharacter player) {
        return position.dst(player.getCenterX(), player.getCenterY());
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

    private boolean updatePatrol(float delta) {
        boolean moved = moveTowards(delta, targetPoint.x, targetPoint.y);
        if (position.dst(targetPoint) <= ARRIVAL_EPSILON) {
            advanceCorner();
        }
        return moved;
    }

    private boolean moveTowards(float delta, float targetX, float targetY) {
        scratch.set(targetX - position.x, targetY - position.y);
        if (scratch.isZero(ARRIVAL_EPSILON)) {
            return false;
        }

        updateFacing(scratch.x, scratch.y);
        scratch.nor().scl(moveSpeed * delta);
        if (scratch.len() >= position.dst(targetX, targetY)) {
            position.set(targetX, targetY);
        } else {
            position.add(scratch);
        }
        return true;
    }

    private void advanceCorner() {
        switch (currentCorner) {
            case BOTTOM_RIGHT:
                currentCorner = PatrolCorner.TOP_RIGHT;
                targetPoint.set(patrolMax.x, patrolMax.y);
                break;
            case TOP_RIGHT:
                currentCorner = PatrolCorner.TOP_LEFT;
                targetPoint.set(patrolMin.x, patrolMax.y);
                break;
            case TOP_LEFT:
                currentCorner = PatrolCorner.BOTTOM_LEFT;
                targetPoint.set(patrolMin.x, patrolMin.y);
                break;
            case BOTTOM_LEFT:
            default:
                currentCorner = PatrolCorner.BOTTOM_RIGHT;
                targetPoint.set(patrolMax.x, patrolMin.y);
                break;
        }
    }

    private void updateFacing(float moveX, float moveY) {
        if (Math.abs(moveX) > Math.abs(moveY)) {
            facing = moveX < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT;
        } else if (Math.abs(moveY) > 0f) {
            facing = moveY < 0f ? FacingDirection.DOWN : FacingDirection.UP;
        }
    }

    private enum PatrolCorner {
        BOTTOM_RIGHT,
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT
    }
}
