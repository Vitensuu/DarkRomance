package com.team7.game1.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CharacterAnimator {

    public enum AnimationState {
        BASE("", false, true),
        WAKE_UP("wakeup", true, false),
        ATTACK_MAGIC("magic", true, false),
        ATTACK_BOW("bow", true, false);

        private final String suffix;
        private final boolean actionAnimation;
        private final boolean looping;

        AnimationState(String suffix, boolean actionAnimation, boolean looping) {
            this.suffix = suffix;
            this.actionAnimation = actionAnimation;
            this.looping = looping;
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
    }

    private static final float FRAME_DURATION = 0.11f;
    private static final float ACTION_FRAME_DURATION = 0.09f;
    private static final float WAKE_UP_FRAME_DURATION = 0.35f;
    private static final float WAKE_UP_FIRST_FRAME_EXTRA_SECONDS = 5f;

    private final String basePath;
    private final String assetPrefix;
    private final float drawScale;
    private final ObjectMap<AnimationState, AnimationPack> packs = new ObjectMap<AnimationState, AnimationPack>();
    private final ObjectMap<String, Texture> textureCache = new ObjectMap<String, Texture>();

    public CharacterAnimator(AnimationPreset preset, float drawScale) {
        this(preset.getAssetFolder(), preset.getAssetPrefix(), drawScale);
    }

    public CharacterAnimator(String assetFolder, String assetPrefix, float drawScale) {
        this.basePath = normalizeFolder(assetFolder);
        this.assetPrefix = assetPrefix;
        this.drawScale = drawScale;
        packs.put(AnimationState.BASE, loadPack(AnimationState.BASE));
    }

    public TextureRegion getFrame(FacingDirection facing, boolean moving, float locomotionTime,
                                  float actionTime, AnimationState state) {
        AnimationPack pack = getPack(state);
        if (state.isActionAnimation() && pack.hasAction(facing)) {
            return pack.getAction(facing).getKeyFrame(actionTime, state.isLooping());
        }
        return moving ? pack.getWalk(facing).getKeyFrame(locomotionTime, true) : pack.getIdle(facing);
    }

    public float getDrawWidth(FacingDirection facing, boolean moving, float locomotionTime,
                              float actionTime, AnimationState state) {
        return getFrame(facing, moving, locomotionTime, actionTime, state).getRegionWidth() * drawScale;
    }

    public float getDrawHeight(FacingDirection facing, boolean moving, float locomotionTime,
                               float actionTime, AnimationState state) {
        return getFrame(facing, moving, locomotionTime, actionTime, state).getRegionHeight() * drawScale;
    }

    public void dispose() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        packs.clear();
    }

    private AnimationPack getPack(AnimationState state) {
        AnimationPack pack = packs.get(state);
        if (pack != null) {
            return pack;
        }

        AnimationPack loadedPack = loadPack(state);
        packs.put(state, loadedPack);
        return loadedPack;
    }

    private AnimationPack loadPack(AnimationState state) {
        AnimationPack basePack = packs.get(AnimationState.BASE);
        AnimationPack pack = new AnimationPack();

        for (FacingDirection facing : FacingDirection.values()) {
            String assetBaseName = buildDirectionalAssetName(facing.getAssetDirection(), state);
            String folder = resolveFolder(state, assetBaseName);

            if (state == AnimationState.BASE) {
                if (folder == null) {
                    throw missingAsset(
                        "Missing base animation folder",
                        buildFolderCandidates(state, assetBaseName).toString(", "),
                        facing,
                        state
                    );
                }
                loadBaseLocomotion(pack, facing, folder, assetBaseName, state);
                continue;
            }

            pack.copyFrom(basePack, facing);
            if (state == AnimationState.WAKE_UP) {
                loadWakeUpAnimation(pack, facing);
                continue;
            }
            if (folder != null) {
                loadActionAnimationIfPresent(pack, facing, state, folder, assetBaseName);
            }
        }

        return pack;
    }

    private void loadWakeUpAnimation(AnimationPack pack, FacingDirection facing) {
        String wakeupFolder = basePath + "wakeup/";
        int frameCount = countSequentialFrames(wakeupFolder, "wakeup", 32);
        if (frameCount == 0) {
            return;
        }
        int extraFirstFrameCopies = Math.max(0, (int) Math.ceil(WAKE_UP_FIRST_FRAME_EXTRA_SECONDS / WAKE_UP_FRAME_DURATION));
        TextureRegion[] actionFrames = new TextureRegion[frameCount + extraFirstFrameCopies];
        for (int i = 0; i < frameCount; i++) {
            int reversedIndex = frameCount - i;
            TextureRegion frame = getOrLoadRegion(wakeupFolder + "wakeup_" + reversedIndex + ".png");
            if (i == 0) {
                for (int copy = 0; copy <= extraFirstFrameCopies; copy++) {
                    actionFrames[copy] = frame;
                }
                continue;
            }
            actionFrames[extraFirstFrameCopies + i] = frame;
        }
        pack.putAction(facing, new Animation<TextureRegion>(WAKE_UP_FRAME_DURATION, actionFrames));
    }

    private void loadBaseLocomotion(AnimationPack pack, FacingDirection facing, String folder,
                                    String assetBaseName, AnimationState state) {
        String idlePath = folder + assetBaseName + ".png";
        FileHandle idleFile = Gdx.files.internal(idlePath);
        FileHandle firstWalkFile = Gdx.files.internal(folder + assetBaseName + "_walk_1.png");

        if (idleFile.exists() && firstWalkFile.exists()) {
            TextureRegion idle = getOrLoadRegion(idlePath);
            TextureRegion[] walkFrames = new TextureRegion[8];
            for (int i = 0; i < walkFrames.length; i++) {
                String framePath = folder + assetBaseName + "_walk_" + (i + 1) + ".png";
                FileHandle frameFile = Gdx.files.internal(framePath);
                if (!frameFile.exists()) {
                    throw missingAsset("Missing base walk frame", framePath, facing, state);
                }
                walkFrames[i] = getOrLoadRegion(framePath);
            }
            pack.put(facing, idle, new Animation<TextureRegion>(FRAME_DURATION, walkFrames));
            return;
        }

        int numberedFrameCount = countSequentialFrames(folder, assetBaseName, 32);
        if (numberedFrameCount > 0) {
            TextureRegion idle = getOrLoadRegion(folder + assetBaseName + "_1.png");
            TextureRegion[] walkFrames = new TextureRegion[8];
            for (int i = 0; i < walkFrames.length; i++) {
                int frameIndex = Math.min(i + 1, numberedFrameCount);
                walkFrames[i] = getOrLoadRegion(folder + assetBaseName + "_" + frameIndex + ".png");
            }
            pack.put(facing, idle, new Animation<TextureRegion>(FRAME_DURATION, walkFrames));
            return;
        }

        // Fallback for packs where BASE locomotion lives under ".../walk/<prefix>_<dir>_walk/"
        // and frames are named "<prefix>_<dir>_walk_1.png", "<prefix>_<dir>_walk_2.png", etc.
        String walkBaseName = assetBaseName + "_walk";
        int walkNumberedFrameCount = countSequentialFrames(folder, walkBaseName, 32);
        if (walkNumberedFrameCount > 0) {
            TextureRegion idle = getOrLoadRegion(folder + walkBaseName + "_1.png");
            TextureRegion[] walkFrames = new TextureRegion[8];
            for (int i = 0; i < walkFrames.length; i++) {
                int frameIndex = Math.min(i + 1, walkNumberedFrameCount);
                walkFrames[i] = getOrLoadRegion(folder + walkBaseName + "_" + frameIndex + ".png");
            }
            pack.put(facing, idle, new Animation<TextureRegion>(FRAME_DURATION, walkFrames));
            return;
        }

        throw missingAsset("Missing base idle frame", idlePath, facing, state);
    }

    private void loadActionAnimationIfPresent(AnimationPack pack, FacingDirection facing, AnimationState state,
                                              String folder, String assetBaseName) {
        if (!state.isActionAnimation()) {
            return;
        }

        int frameCount = countSequentialFrames(folder, assetBaseName, 32);
        if (frameCount == 0) {
            return;
        }

        TextureRegion[] actionFrames = new TextureRegion[frameCount];
        for (int i = 0; i < actionFrames.length; i++) {
            String actionFramePath = folder + assetBaseName + "_" + (i + 1) + ".png";
            actionFrames[i] = getOrLoadRegion(actionFramePath);
        }

        pack.putAction(facing, new Animation<TextureRegion>(ACTION_FRAME_DURATION, actionFrames));
    }

    private String buildDirectionalAssetName(String direction, AnimationState state) {
        String baseName = assetPrefix + "_" + direction;
        if (state.getSuffix().isEmpty()) {
            return baseName;
        }
        return baseName + "_" + state.getSuffix();
    }

    private String resolveFolder(AnimationState state, String assetBaseName) {
        Array<String> candidates = buildFolderCandidates(state, assetBaseName);
        for (String candidate : candidates) {
            if (hasFrames(candidate, assetBaseName)) {
                return candidate;
            }
        }
        return null;
    }

    private Array<String> buildFolderCandidates(AnimationState state, String assetBaseName) {
        Array<String> candidates = new Array<String>();
        candidates.add(basePath + assetBaseName + "/");
        candidates.add(basePath + assetPrefix + "/" + assetBaseName + "/");
        if (!state.getSuffix().isEmpty()) {
            candidates.add(basePath + state.getSuffix() + "/" + assetBaseName + "/");
            candidates.add(basePath + state.getSuffix() + " /" + assetBaseName + "/");
        } else {
            candidates.add(basePath + "walk/" + assetBaseName + "_walk/");
            candidates.add(basePath + assetPrefix + "/walk/" + assetBaseName + "_walk/");
        }
        return candidates;
    }

    private boolean hasFrames(String folder, String assetBaseName) {
        FileHandle idle = Gdx.files.internal(folder + assetBaseName + ".png");
        if (idle.exists()) {
            return true;
        }
        FileHandle walk = Gdx.files.internal(folder + assetBaseName + "_walk_1.png");
        if (walk.exists()) {
            return true;
        }
        FileHandle firstNumbered = Gdx.files.internal(folder + assetBaseName + "_1.png");
        return firstNumbered.exists();
    }

    private int countSequentialFrames(String folder, String assetBaseName, int maxFrames) {
        int count = 0;
        for (int i = 1; i <= maxFrames; i++) {
            FileHandle frame = Gdx.files.internal(folder + assetBaseName + "_" + i + ".png");
            if (!frame.exists()) {
                break;
            }
            count++;
        }
        return count;
    }

    private String normalizeFolder(String folder) {
        return folder.endsWith("/") ? folder : folder + "/";
    }

    private IllegalStateException missingAsset(String reason, String path, FacingDirection facing, AnimationState state) {
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
