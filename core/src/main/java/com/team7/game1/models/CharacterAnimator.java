package com.team7.game1.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

public class CharacterAnimator {

    private static final float FRAME_DURATION = 0.11f;
    private static final float ACTION_FRAME_DURATION = 0.09f;

    private final String basePath;
    private final String assetPrefix;
    private final float drawScale;
    private final ObjectMap<CharacterAnimationState, AnimationPack> packs = new ObjectMap<CharacterAnimationState, AnimationPack>();
    private final ObjectMap<String, Texture> textureCache = new ObjectMap<String, Texture>();

    public CharacterAnimator(String assetFolder, String assetPrefix, float drawScale) {
        this.basePath = normalizeFolder(assetFolder);
        this.assetPrefix = assetPrefix;
        this.drawScale = drawScale;
        packs.put(CharacterAnimationState.BASE, loadPack(CharacterAnimationState.BASE));
    }

    public TextureRegion getFrame(FacingDirection facing, boolean moving, float locomotionTime,
                                  float actionTime, CharacterAnimationState state) {
        AnimationPack pack = getPack(state);
        if (state.isActionAnimation() && pack.hasAction(facing)) {
            return pack.getAction(facing).getKeyFrame(actionTime, state.isLooping());
        }
        return moving ? pack.getWalk(facing).getKeyFrame(locomotionTime, true) : pack.getIdle(facing);
    }

    public float getDrawWidth(FacingDirection facing, boolean moving, float locomotionTime,
                              float actionTime, CharacterAnimationState state) {
        return getFrame(facing, moving, locomotionTime, actionTime, state).getRegionWidth() * drawScale;
    }

    public float getDrawHeight(FacingDirection facing, boolean moving, float locomotionTime,
                               float actionTime, CharacterAnimationState state) {
        return getFrame(facing, moving, locomotionTime, actionTime, state).getRegionHeight() * drawScale;
    }

    public void dispose() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        packs.clear();
    }

    private AnimationPack getPack(CharacterAnimationState state) {
        AnimationPack pack = packs.get(state);
        if (pack != null) {
            return pack;
        }

        AnimationPack loadedPack = loadPack(state);
        packs.put(state, loadedPack);
        return loadedPack;
    }

    private AnimationPack loadPack(CharacterAnimationState state) {
        AnimationPack basePack = packs.get(CharacterAnimationState.BASE);
        AnimationPack pack = new AnimationPack();

        for (FacingDirection facing : FacingDirection.values()) {
            String assetBaseName = buildDirectionalAssetName(facing.getAssetDirection(), state);
            String folder = basePath + assetBaseName + "/";
            FileHandle idleFile = Gdx.files.internal(folder + assetBaseName + ".png");

            if (!idleFile.exists() && state != CharacterAnimationState.BASE) {
                pack.copyFrom(basePack, facing);
                continue;
            }
            if (!idleFile.exists()) {
                throw missingAsset("Missing base idle frame", idleFile.path(), facing, state);
            }

            TextureRegion idle = getOrLoadRegion(idleFile.path());
            TextureRegion[] walkFrames = new TextureRegion[8];
            for (int i = 0; i < walkFrames.length; i++) {
                String framePath = folder + assetBaseName + "_walk_" + (i + 1) + ".png";
                FileHandle frameFile = Gdx.files.internal(framePath);
                if (!frameFile.exists() && state != CharacterAnimationState.BASE && basePack != null) {
                    walkFrames[i] = basePack.getWalk(facing).getKeyFrames()[i];
                } else {
                    if (!frameFile.exists()) {
                        throw missingAsset("Missing base walk frame", framePath, facing, state);
                    }
                    walkFrames[i] = getOrLoadRegion(framePath);
                }
            }

            pack.put(facing, idle, new Animation<TextureRegion>(FRAME_DURATION, walkFrames));
            loadActionAnimationIfPresent(pack, facing, state, folder, assetBaseName);
        }

        return pack;
    }

    private void loadActionAnimationIfPresent(AnimationPack pack, FacingDirection facing, CharacterAnimationState state,
                                              String folder, String assetBaseName) {
        if (!state.isActionAnimation()) {
            return;
        }

        String firstActionFramePath = folder + assetBaseName + "_1.png";
        FileHandle firstActionFrame = Gdx.files.internal(firstActionFramePath);
        if (!firstActionFrame.exists()) {
            return;
        }

        TextureRegion[] actionFrames = new TextureRegion[state.getActionFrameCount()];
        for (int i = 0; i < actionFrames.length; i++) {
            String actionFramePath = folder + assetBaseName + "_" + (i + 1) + ".png";
            FileHandle actionFrameFile = Gdx.files.internal(actionFramePath);
            if (!actionFrameFile.exists()) {
                throw new IllegalStateException(
                    "Missing action frame '" + actionFramePath + "' for state " + state + " facing " + facing
                );
            }
            actionFrames[i] = getOrLoadRegion(actionFramePath);
        }

        pack.putAction(facing, new Animation<TextureRegion>(ACTION_FRAME_DURATION, actionFrames));
    }

    private String buildDirectionalAssetName(String direction, CharacterAnimationState state) {
        String baseName = assetPrefix + "_" + direction;
        if (state.getSuffix().isEmpty()) {
            return baseName;
        }
        return baseName + "_" + state.getSuffix();
    }

    private String normalizeFolder(String folder) {
        return folder.endsWith("/") ? folder : folder + "/";
    }

    private IllegalStateException missingAsset(String reason, String path, FacingDirection facing, CharacterAnimationState state) {
        return new IllegalStateException(reason + ": '" + path + "' for state " + state + " facing " + facing);
    }

    private TextureRegion getOrLoadRegion(String path) {
        Texture texture = textureCache.get(path);
        if (texture == null) {
            texture = new Texture(Gdx.files.internal(path));
            textureCache.put(path, texture);
        }
        return new TextureRegion(texture);
    }

    private static class AnimationPack {
        private final ObjectMap<FacingDirection, TextureRegion> idleFrames = new ObjectMap<FacingDirection, TextureRegion>();
        private final ObjectMap<FacingDirection, Animation<TextureRegion>> walkAnimations =
            new ObjectMap<FacingDirection, Animation<TextureRegion>>();
        private final ObjectMap<FacingDirection, Animation<TextureRegion>> actionAnimations =
            new ObjectMap<FacingDirection, Animation<TextureRegion>>();

        void put(FacingDirection facing, TextureRegion idle, Animation<TextureRegion> walk) {
            idleFrames.put(facing, idle);
            walkAnimations.put(facing, walk);
        }

        void copyFrom(AnimationPack source, FacingDirection facing) {
            idleFrames.put(facing, source.getIdle(facing));
            walkAnimations.put(facing, source.getWalk(facing));
            Animation<TextureRegion> action = source.getAction(facing);
            if (action != null) {
                actionAnimations.put(facing, action);
            }
        }

        TextureRegion getIdle(FacingDirection facing) {
            return idleFrames.get(facing);
        }

        Animation<TextureRegion> getWalk(FacingDirection facing) {
            return walkAnimations.get(facing);
        }

        void putAction(FacingDirection facing, Animation<TextureRegion> action) {
            actionAnimations.put(facing, action);
        }

        Animation<TextureRegion> getAction(FacingDirection facing) {
            return actionAnimations.get(facing);
        }

        boolean hasAction(FacingDirection facing) {
            return actionAnimations.containsKey(facing);
        }
    }
}
