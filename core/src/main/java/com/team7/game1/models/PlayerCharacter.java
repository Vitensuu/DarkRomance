package com.team7.game1.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PlayerCharacter {

    private static final String BASE_PATH = "characters/robe_frames/";
    private static final float FRAME_DURATION = 0.11f;
    private static final float DEFAULT_SPEED = 180f;
    private static final float DRAW_SCALE = 3.4f;

    private final Array<Texture> loadedTextures = new Array<Texture>();
    private final Animation<TextureRegion> walkDown;
    private final Animation<TextureRegion> walkLeft;
    private final Animation<TextureRegion> walkRight;
    private final Animation<TextureRegion> walkUp;
    private final TextureRegion idleDown;
    private final TextureRegion idleLeft;
    private final TextureRegion idleRight;
    private final TextureRegion idleUp;
    private final Vector2 position = new Vector2();
    private final Vector2 movement = new Vector2();

    private Facing facing = Facing.DOWN;
    private float stateTime;
    private float speed = DEFAULT_SPEED;
    private float maxFrameWidth;
    private float maxFrameHeight;

    public PlayerCharacter(float startX, float startY) {
        idleDown = loadFrame("front");
        idleLeft = loadFrame("left");
        idleRight = loadFrame("right");
        idleUp = loadFrame("back");

        walkDown = new Animation<TextureRegion>(FRAME_DURATION, loadWalkFrames("front"));
        walkLeft = new Animation<TextureRegion>(FRAME_DURATION, loadWalkFrames("left"));
        walkRight = new Animation<TextureRegion>(FRAME_DURATION, loadWalkFrames("right"));
        walkUp = new Animation<TextureRegion>(FRAME_DURATION, loadWalkFrames("back"));

        position.set(startX, startY);
    }

    public void update(float delta, float moveX, float moveY, float worldWidth, float worldHeight) {
        movement.set(moveX, moveY);
        boolean moving = movement.len2() > 0f;

        if (moving) {
            movement.nor().scl(speed * delta);
            position.add(movement);
            updateFacing(moveX, moveY);
            stateTime += delta;
        } else {
            stateTime = 0f;
        }

        float halfWidth = getMaxDrawWidth() / 2f;
        position.x = Math.max(halfWidth, Math.min(position.x, worldWidth - halfWidth));
        position.y = Math.max(0f, Math.min(position.y, worldHeight - getMaxDrawHeight()));
    }

    public TextureRegion getCurrentFrame() {
        switch (facing) {
            case LEFT:
                return stateTime > 0f ? walkLeft.getKeyFrame(stateTime, true) : idleLeft;
            case RIGHT:
                return stateTime > 0f ? walkRight.getKeyFrame(stateTime, true) : idleRight;
            case UP:
                return stateTime > 0f ? walkUp.getKeyFrame(stateTime, true) : idleUp;
            case DOWN:
            default:
                return stateTime > 0f ? walkDown.getKeyFrame(stateTime, true) : idleDown;
        }
    }

    public float getX() {
        return position.x - getDrawWidth() / 2f;
    }

    public float getY() {
        return position.y;
    }

    public float getDrawWidth() {
        return getCurrentFrame().getRegionWidth() * DRAW_SCALE;
    }

    public float getDrawHeight() {
        return getCurrentFrame().getRegionHeight() * DRAW_SCALE;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
    }

    private TextureRegion loadFrame(String direction) {
        return registerTexture(new Texture(Gdx.files.internal(BASE_PATH + "robe_" + direction + ".png")));
    }

    private TextureRegion[] loadWalkFrames(String direction) {
        TextureRegion[] frames = new TextureRegion[8];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = registerTexture(
                new Texture(Gdx.files.internal(BASE_PATH + "robe_" + direction + "_walk_" + (i + 1) + ".png"))
            );
        }
        return frames;
    }

    private TextureRegion registerTexture(Texture texture) {
        loadedTextures.add(texture);
        TextureRegion region = new TextureRegion(texture);
        maxFrameWidth = Math.max(maxFrameWidth, region.getRegionWidth());
        maxFrameHeight = Math.max(maxFrameHeight, region.getRegionHeight());
        return region;
    }

    private float getMaxDrawWidth() {
        return maxFrameWidth * DRAW_SCALE;
    }

    private float getMaxDrawHeight() {
        return maxFrameHeight * DRAW_SCALE;
    }

    private void updateFacing(float moveX, float moveY) {
        if (Math.abs(moveX) > Math.abs(moveY)) {
            facing = moveX < 0f ? Facing.LEFT : Facing.RIGHT;
        } else if (Math.abs(moveY) > 0f) {
            facing = moveY < 0f ? Facing.DOWN : Facing.UP;
        }
    }

    private enum Facing {
        DOWN,
        LEFT,
        RIGHT,
        UP
    }
}
