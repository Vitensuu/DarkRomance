package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.GameConfig;
import com.team7.game1.models.CharacterAnimator;
import com.team7.game1.models.NPC;
import com.team7.game1.models.NPCHero;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.models.PlayerData;
import com.team7.game1.ui.CharacterWindow;
import com.team7.game1.ui.GameMenuWindow;
import com.team7.game1.ui.NpcDialogueEntry;
import com.team7.game1.ui.NpcDialogueLibrary;
import com.team7.game1.utils.SaveManager;
import com.team7.game1.world.tiled.MapTransitionService;
import com.team7.game1.world.tiled.TiledWorld;
import com.team7.game1.world.tiled.TriggerActionRegistry;
import com.team7.game1.world.tiled.TriggerService;

public class GameScreen implements Screen {

    private static final String GAME_MUSIC_PATH = "music/game_theme.mp3";
    private static final float DEFAULT_MUSIC_VOLUME = 0.32f;
    private static final float MENU_VOLUME_STEP = 0.1f;
    private static final float ATTACK_ANIMATION_DURATION_SECONDS = 0.75f;

    private static final Color WORLD_COLOR = Color.valueOf("2F4A2CFF");
    private static final Color WORLD_ACCENT = Color.valueOf("405C34FF");

    private static final float MAP_SCALE = GameConfig.World.MAP_SCALE;
    private static final String DEFAULT_MAP_PATH = GameConfig.World.DEFAULT_MAP_PATH;

    private static final float NPC_TALK_DISTANCE = 115f;

    private static final float HERO_CAMP_MIN_X = 4860f;
    private static final float HERO_CAMP_MIN_Y = 4760f;
    private static final float HERO_CAMP_MAX_X = 5680f;
    private static final float HERO_CAMP_MAX_Y = 5560f;

    private static final float GUARDIAN_MIN_X = 3320f;
    private static final float GUARDIAN_MIN_Y = 5890f;
    private static final float GUARDIAN_MAX_X = 3750f;
    private static final float GUARDIAN_MAX_Y = 6230f;

    private final DarkRomanceGame game;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Rectangle nextButtonBounds = new Rectangle();
    private final Rectangle triggerProbeBounds = new Rectangle();
    private final Vector2 worldTouchPoint = new Vector2();
    private final MapTransitionService mapTransitionService = new MapTransitionService();
    private final TriggerActionRegistry triggerActionRegistry = new TriggerActionRegistry(mapTransitionService);

    private SpriteBatch batch;
    private Music backgroundMusic;
    private FitViewport viewport;
    private Texture pixel;

    private TiledWorld tiledWorld;
    private Texture dialogFrameTexture;
    private Texture dialogNameplateTexture;
    private Texture dialogNextButtonTexture;
    private NinePatch dialogFramePatch;

    private NpcDialogueLibrary dialogueLibrary;
    private PlayerCharacter player;
    private Array<NPC> npcs;
    private NPC activeDialogNpc;
    private int activeDialogLineIndex;

    private MoveDirection lastPressedDirection = MoveDirection.DOWN;
    private float worldWidth = DarkRomanceGame.DESIGN_WIDTH;
    private float worldHeight = DarkRomanceGame.DESIGN_HEIGHT;

    private PlayerData playerData;
    private CharacterWindow characterWindow;
    private GameMenuWindow gameMenuWindow;

    private float musicVolume = DEFAULT_MUSIC_VOLUME;
    private boolean musicMuted;
    private boolean startSequenceActive = true;
    private String activeTriggerId;
    private String currentMapPath = DEFAULT_MAP_PATH;

    public GameScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        pixel = createSolidTexture(Color.WHITE);

        startBackgroundMusic();
        loadMap(currentMapPath);
        loadDialogTextures();

        dialogueLibrary = new NpcDialogueLibrary();
        playerData = game.getCurrentPlayerData();
        if (playerData == null) {
            playerData = new PlayerData();
            game.setCurrentPlayerData(playerData);
        }

        player = new PlayerCharacter(worldWidth / 2f, worldHeight / 2f - 64f);
        placePlayerAtDefaultSpawn();

        float wakeUpDurationSeconds = player.getActionAnimationDuration(CharacterAnimator.AnimationState.WAKE_UP);
        player.triggerAnimationState(CharacterAnimator.AnimationState.WAKE_UP, wakeUpDurationSeconds);

        characterWindow = new CharacterWindow();
        gameMenuWindow = new GameMenuWindow();
        npcs = createNpcs();
    }

    @Override
    public void render(float delta) {
        update(delta);
        updateCamera();

        Gdx.gl.glClearColor(WORLD_COLOR.r, WORLD_COLOR.g, WORLD_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        tiledWorld.renderBelowPlayer((OrthographicCamera) viewport.getCamera());

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        tiledWorld.drawObjectTileLayers(batch);
        if (!tiledWorld.isLoaded()) {
            drawGround();
        }

        drawPatrolBounds();
        drawNpcs();

        TextureRegion playerFrame = player.getCurrentFrame();
        batch.setColor(Color.WHITE);
        batch.draw(playerFrame, player.getX(), player.getY(), player.getDrawWidth(), player.getDrawHeight());
        batch.end();

        tiledWorld.renderAbovePlayer((OrthographicCamera) viewport.getCamera());

        batch.begin();
        drawDialog();
        characterWindow.draw(batch, pixel, viewport, playerData, player.getEquippedWeapon());
        gameMenuWindow.draw(batch, pixel, viewport, musicMuted, musicVolume);
        batch.end();
    }

    private void update(float delta) {
        if (handleEscapeInput()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && !gameMenuWindow.isVisible()) {
            characterWindow.toggle();
        }

        if (updateMenu(delta)) {
            return;
        }

        if (updateWakeUpSequence(delta)) {
            return;
        }

        updateLastPressedDirection();
        handleDialogInput();
        handleAttackInput();

        float moveX = 0f;
        float moveY = 0f;

        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (leftPressed) {
            moveX -= 1f;
        }
        if (rightPressed) {
            moveX += 1f;
        }
        if (upPressed) {
            moveY += 1f;
        }
        if (downPressed) {
            moveY -= 1f;
        }

        if (moveX != 0f && moveY != 0f) {
            switch (lastPressedDirection) {
                case LEFT:
                    if (leftPressed) {
                        moveX = -1f;
                        moveY = 0f;
                    }
                    break;
                case RIGHT:
                    if (rightPressed) {
                        moveX = 1f;
                        moveY = 0f;
                    }
                    break;
                case UP:
                    if (upPressed) {
                        moveX = 0f;
                        moveY = 1f;
                    }
                    break;
                case DOWN:
                    if (downPressed) {
                        moveX = 0f;
                        moveY = -1f;
                    }
                    break;
                default:
                    break;
            }
        }

        if (characterWindow.isVisible()) {
            moveX = 0f;
            moveY = 0f;
        }

        player.update(delta, moveX, moveY, worldWidth, worldHeight);
        updateTriggers();
        applyPendingMapTransition();
        updateNpcs(delta);
    }

    private boolean handleEscapeInput() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return false;
        }

        if (gameMenuWindow.isVisible()) {
            gameMenuWindow.close();
            return true;
        }

        if (activeDialogNpc != null) {
            closeDialog();
            return true;
        }

        gameMenuWindow.open();
        return true;
    }

    private boolean updateMenu(float delta) {
        if (!gameMenuWindow.isVisible()) {
            return false;
        }

        GameMenuWindow.MenuAction action = gameMenuWindow.handleInput(viewport);
        switch (action) {
            case CONTINUE:
                gameMenuWindow.close();
                return true;
            case TOGGLE_MUTE:
                toggleMusicMute();
                return true;
            case VOLUME_DOWN:
                setMusicVolume(musicVolume - MENU_VOLUME_STEP);
                return true;
            case VOLUME_UP:
                setMusicVolume(musicVolume + MENU_VOLUME_STEP);
                return true;
            case EXIT_GAME:
                SaveManager.savePlayer(playerData);
                Gdx.app.exit();
                return true;
            case NONE:
            default:
                player.update(delta, 0f, 0f, worldWidth, worldHeight);
                return true;
        }
    }

    private boolean updateWakeUpSequence(float delta) {
        if (!startSequenceActive) {
            return false;
        }

        if (!player.isAnimationStateActive(CharacterAnimator.AnimationState.WAKE_UP)) {
            startSequenceActive = false;
        }

        player.update(delta, 0f, 0f, worldWidth, worldHeight);
        return true;
    }

    private void handleAttackInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            player.triggerAnimationState(
                CharacterAnimator.AnimationState.ATTACK_MAGIC,
                ATTACK_ANIMATION_DURATION_SECONDS
            );
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            player.triggerAnimationState(
                CharacterAnimator.AnimationState.ATTACK_BOW,
                ATTACK_ANIMATION_DURATION_SECONDS
            );
        }
    }

    private void updateNpcs(float delta) {
        for (NPC npc : npcs) {
            if (npc == activeDialogNpc) {
                continue;
            }
            npc.update(delta, player);
        }
    }

    private void loadMap(String mapPath) {
        if (tiledWorld != null) {
            tiledWorld.dispose();
        }

        tiledWorld = new TiledWorld(mapPath, MAP_SCALE, batch, worldWidth, worldHeight);
        worldWidth = tiledWorld.getWorldWidth();
        worldHeight = tiledWorld.getWorldHeight();
        currentMapPath = mapPath;
    }

    private void applyPendingMapTransition() {
        if (!mapTransitionService.hasPendingTransition()) {
            return;
        }

        MapTransitionService.MapTransitionRequest request = mapTransitionService.consumePendingTransition();
        if (request == null) {
            return;
        }

        loadMap(request.getMapPath());

        Vector2 playerSpawn = resolveTransitionSpawn(request);
        if (playerSpawn != null) {
            player.setBottomLeft(playerSpawn.x, playerSpawn.y);
        }

        closeDialog();
        activeTriggerId = null;
    }

    private Vector2 resolveTransitionSpawn(MapTransitionService.MapTransitionRequest request) {
        String marker = request.getTargetMarker();
        if (marker != null && !marker.trim().isEmpty()) {
            Vector2 markerSpawn = tiledWorld.findSpawnByMarker(marker, player.getDrawWidth(), player.getDrawHeight());
            if (markerSpawn != null) {
                return markerSpawn;
            }
        }

        if (request.getSpawnX() != 0f || request.getSpawnY() != 0f) {
            return new Vector2(request.getSpawnX(), request.getSpawnY());
        }

        return tiledWorld.findPlayerSpawn(player.getDrawWidth(), player.getDrawHeight());
    }

    private void updateTriggers() {
        TriggerService triggerService = tiledWorld.getTriggerService();
        if (triggerService == null) {
            activeTriggerId = null;
            return;
        }

        player.getCollisionBounds(triggerProbeBounds);
        TriggerService.TriggerZone triggeredZone = triggerService.findTriggeredZone(triggerProbeBounds);
        if (triggeredZone == null) {
            activeTriggerId = null;
            return;
        }

        if (!triggeredZone.getId().equals(activeTriggerId)) {
            activeTriggerId = triggeredZone.getId();
            triggerActionRegistry.onZoneEntered(triggeredZone, player, playerData);
        }
    }

    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float halfViewportWidth = viewport.getWorldWidth() * 0.5f;
        float halfViewportHeight = viewport.getWorldHeight() * 0.5f;

        float targetX = player.getX() + player.getDrawWidth() * 0.5f;
        float targetY = player.getY() + player.getDrawHeight() * 0.5f;

        float minX = halfViewportWidth;
        float maxX = Math.max(halfViewportWidth, worldWidth - halfViewportWidth);
        float minY = halfViewportHeight;
        float maxY = Math.max(halfViewportHeight, worldHeight - halfViewportHeight);

        camera.position.x = MathUtils.clamp(targetX, minX, maxX);
        camera.position.y = MathUtils.clamp(targetY, minY, maxY);
        camera.update();
    }

    private void handleDialogInput() {
        if (activeDialogNpc != null) {
            if (isDialogAdvancePressed()) {
                advanceDialog();
                return;
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                viewport.unproject(worldTouchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
                if (nextButtonBounds.contains(worldTouchPoint)) {
                    advanceDialog();
                }
            }
            return;
        }

        if (!Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            return;
        }

        NPC nearestNpc = findNearestNpcWithinTalkDistance();
        if (nearestNpc == null) {
            closeDialog();
            return;
        }

        activeDialogNpc = nearestNpc;
        activeDialogLineIndex = 0;
    }

    private boolean isDialogAdvancePressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.F)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    private void closeDialog() {
        activeDialogNpc = null;
        activeDialogLineIndex = 0;
    }

    private void updateLastPressedDirection() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            lastPressedDirection = MoveDirection.LEFT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            lastPressedDirection = MoveDirection.RIGHT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            lastPressedDirection = MoveDirection.UP;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            lastPressedDirection = MoveDirection.DOWN;
        }
    }

    private void drawGround() {
        batch.setColor(WORLD_ACCENT);
        float tile = 96f;
        for (float x = 0; x < DarkRomanceGame.DESIGN_WIDTH; x += tile * 2f) {
            for (float y = 0; y < DarkRomanceGame.DESIGN_HEIGHT; y += tile * 2f) {
                batch.draw(pixel, x, y, tile, tile);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawNpcs() {
        for (NPC npc : npcs) {
            batch.setColor(Color.valueOf("00000033"));
            batch.draw(pixel, npc.getX() + 10f, npc.getY() - 10f, npc.getWidth() - 20f, 8f);
            batch.setColor(Color.WHITE);
            batch.draw(npc.getCurrentFrame(), npc.getX(), npc.getY(), npc.getWidth(), npc.getHeight());
        }
        batch.setColor(Color.WHITE);
    }

    private void drawDialog() {
        if (activeDialogNpc == null) {
            return;
        }

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float panelX = camera.position.x - viewport.getWorldWidth() * 0.5f + GameConfig.Dialog.X;
        float panelY = camera.position.y - viewport.getWorldHeight() * 0.5f + GameConfig.Dialog.Y;

        if (dialogFramePatch != null) {
            batch.setColor(Color.WHITE);
            dialogFramePatch.draw(batch, panelX, panelY, GameConfig.Dialog.WIDTH, GameConfig.Dialog.HEIGHT);
        } else {
            batch.setColor(GameConfig.Dialog.FALLBACK_PANEL_BG);
            batch.draw(pixel, panelX, panelY, GameConfig.Dialog.WIDTH, GameConfig.Dialog.HEIGHT);
            batch.setColor(GameConfig.Dialog.FALLBACK_PANEL_BORDER);
            batch.draw(pixel, panelX, panelY + GameConfig.Dialog.HEIGHT - 4f, GameConfig.Dialog.WIDTH, 4f);
            batch.draw(pixel, panelX, panelY, GameConfig.Dialog.WIDTH, 4f);
            batch.draw(pixel, panelX, panelY, 4f, GameConfig.Dialog.HEIGHT);
            batch.draw(pixel, panelX + GameConfig.Dialog.WIDTH - 4f, panelY, 4f, GameConfig.Dialog.HEIGHT);
        }

        BitmapFont titleFont = DarkRomanceGame.skin.getFont("GuildensternSmall");
        BitmapFont titleShadowFont = DarkRomanceGame.skin.getFont("GuildensternSmallShadow");
        BitmapFont bodyFont = DarkRomanceGame.skin.getFont("default-font");

        float portraitX = panelX + GameConfig.Dialog.PADDING;
        float portraitY = panelY + GameConfig.Dialog.HEIGHT - GameConfig.Dialog.PADDING - GameConfig.Dialog.PORTRAIT_SIZE;
        float portraitInset = 8f;

        float textX = portraitX + GameConfig.Dialog.PORTRAIT_SIZE + 24f;
        float titleTopY = panelY + GameConfig.Dialog.HEIGHT - GameConfig.Dialog.PADDING;
        float bodyTopY = titleTopY - 54f;
        float textWidth = GameConfig.Dialog.WIDTH - (textX - panelX) - GameConfig.Dialog.PADDING;
        float hintY = panelY + 24f;

        NpcDialogueEntry dialogueEntry = dialogueLibrary.getEntry(activeDialogNpc.getDialogueId());
        String dialogText = applyDialogueTokens(dialogueEntry.getLine(activeDialogLineIndex));

        batch.setColor(GameConfig.Dialog.PORTRAIT_BG);
        batch.draw(pixel, portraitX, portraitY, GameConfig.Dialog.PORTRAIT_SIZE, GameConfig.Dialog.PORTRAIT_SIZE);
        batch.setColor(GameConfig.Dialog.PORTRAIT_BORDER);
        batch.draw(pixel, portraitX, portraitY + GameConfig.Dialog.PORTRAIT_SIZE - 4f, GameConfig.Dialog.PORTRAIT_SIZE, 4f);
        batch.draw(pixel, portraitX, portraitY, GameConfig.Dialog.PORTRAIT_SIZE, 4f);
        batch.draw(pixel, portraitX, portraitY, 4f, GameConfig.Dialog.PORTRAIT_SIZE);
        batch.draw(pixel, portraitX + GameConfig.Dialog.PORTRAIT_SIZE - 4f, portraitY, 4f, GameConfig.Dialog.PORTRAIT_SIZE);

        batch.setColor(Color.WHITE);
        batch.draw(
            activeDialogNpc.getDialoguePortraitFrame(),
            portraitX + portraitInset,
            portraitY + portraitInset,
            GameConfig.Dialog.PORTRAIT_SIZE - portraitInset * 2f,
            GameConfig.Dialog.PORTRAIT_SIZE - portraitInset * 2f
        );

        String npcName = dialogueEntry.getName();
        titleFont.setColor(GameConfig.Dialog.TITLE);
        titleShadowFont.setColor(GameConfig.Dialog.TITLE_SHADOW);
        bodyFont.setColor(GameConfig.Dialog.BODY_TEXT);

        glyphLayout.setText(titleFont, npcName);
        float nameplateWidth = Math.max(190f, glyphLayout.width + 48f);
        float nameplateX = textX - 8f;
        float nameplateY = panelY + GameConfig.Dialog.HEIGHT - GameConfig.Dialog.NAMEPLATE_HEIGHT - 16f;

        if (dialogNameplateTexture != null) {
            batch.draw(dialogNameplateTexture, nameplateX, nameplateY, nameplateWidth, GameConfig.Dialog.NAMEPLATE_HEIGHT);
        } else {
            batch.setColor(GameConfig.Dialog.FALLBACK_NAMEPLATE_BG);
            batch.draw(pixel, nameplateX, nameplateY, nameplateWidth, GameConfig.Dialog.NAMEPLATE_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        float nameTextX = nameplateX + 18f;
        float nameTextY = nameplateY + 25f;
        titleShadowFont.draw(batch, npcName, nameTextX + 1f, nameTextY - 1f);
        titleFont.draw(batch, npcName, nameTextX, nameTextY);

        bodyFont.draw(batch, dialogText, textX, bodyTopY, textWidth, Align.left, true);
        glyphLayout.setText(bodyFont, GameConfig.Dialog.HINT_TEXT);
        bodyFont.draw(batch, glyphLayout, panelX + GameConfig.Dialog.PADDING, hintY);

        float buttonX = panelX + GameConfig.Dialog.WIDTH - GameConfig.Dialog.PADDING - GameConfig.Dialog.NEXT_BUTTON_WIDTH;
        float buttonY = panelY + 18f;
        nextButtonBounds.set(buttonX, buttonY, GameConfig.Dialog.NEXT_BUTTON_WIDTH, GameConfig.Dialog.NEXT_BUTTON_HEIGHT);

        if (dialogNextButtonTexture != null) {
            batch.draw(dialogNextButtonTexture, buttonX, buttonY, GameConfig.Dialog.NEXT_BUTTON_WIDTH, GameConfig.Dialog.NEXT_BUTTON_HEIGHT);
        } else {
            batch.setColor(GameConfig.Dialog.FALLBACK_BUTTON_BG);
            batch.draw(pixel, buttonX, buttonY, GameConfig.Dialog.NEXT_BUTTON_WIDTH, GameConfig.Dialog.NEXT_BUTTON_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        glyphLayout.setText(titleFont, isLastDialogLine() ? "Закрыть" : "Далее");
        titleFont.draw(
            batch,
            glyphLayout,
            buttonX + (GameConfig.Dialog.NEXT_BUTTON_WIDTH - glyphLayout.width) / 2f,
            buttonY + GameConfig.Dialog.NEXT_BUTTON_HEIGHT / 2f + glyphLayout.height / 2f - 4f
        );

        batch.setColor(Color.WHITE);
    }

    private void drawPatrolBounds() {
        batch.setColor(Color.valueOf("FFFFFF12"));
        batch.draw(
            pixel,
            HERO_CAMP_MIN_X,
            HERO_CAMP_MIN_Y,
            HERO_CAMP_MAX_X - HERO_CAMP_MIN_X,
            HERO_CAMP_MAX_Y - HERO_CAMP_MIN_Y
        );
        batch.draw(
            pixel,
            GUARDIAN_MIN_X,
            GUARDIAN_MIN_Y,
            GUARDIAN_MAX_X - GUARDIAN_MIN_X,
            GUARDIAN_MAX_Y - GUARDIAN_MIN_Y
        );
        batch.setColor(Color.WHITE);
    }

    private NPC findNearestNpcWithinTalkDistance() {
        NPC nearestNpc = null;
        float nearestDistance = NPC_TALK_DISTANCE;

        for (NPC npc : npcs) {
            if (!npc.canInteract(player)) {
                continue;
            }

            float npcCenterX = npc.getX() + npc.getWidth() * 0.5f;
            float npcCenterY = npc.getY() + npc.getHeight() * 0.5f;
            float distance = Vector2.dst(player.getCenterX(), player.getCenterY(), npcCenterX, npcCenterY);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearestNpc = npc;
            }
        }

        return nearestNpc;
    }

    private String applyDialogueTokens(String dialogueLine) {
        if (dialogueLine == null) {
            return "";
        }

        String username = "Игрок";
        if (playerData != null && playerData.getUsername() != null && !playerData.getUsername().trim().isEmpty()) {
            username = playerData.getUsername().trim();
        }

        return dialogueLine
            .replace("{playerName}", username)
            .replace("playerName", username);
    }

    private void loadDialogTextures() {
        if (Gdx.files.internal(GameConfig.Dialog.FRAME_TEXTURE_PATH).exists()) {
            dialogFrameTexture = new Texture(Gdx.files.internal(GameConfig.Dialog.FRAME_TEXTURE_PATH));
            dialogFramePatch = new NinePatch(new TextureRegion(dialogFrameTexture), 8, 8, 8, 8);
        } else {
            Gdx.app.log("GameScreen", "Dialog frame texture not found: " + GameConfig.Dialog.FRAME_TEXTURE_PATH);
        }

        if (Gdx.files.internal(GameConfig.Dialog.NAMEPLATE_TEXTURE_PATH).exists()) {
            dialogNameplateTexture = new Texture(Gdx.files.internal(GameConfig.Dialog.NAMEPLATE_TEXTURE_PATH));
        } else {
            Gdx.app.log("GameScreen", "Dialog nameplate texture not found: " + GameConfig.Dialog.NAMEPLATE_TEXTURE_PATH);
        }

        if (Gdx.files.internal(GameConfig.Dialog.NEXT_BUTTON_TEXTURE_PATH).exists()) {
            dialogNextButtonTexture = new Texture(Gdx.files.internal(GameConfig.Dialog.NEXT_BUTTON_TEXTURE_PATH));
        } else {
            Gdx.app.log("GameScreen", "Dialog next button texture not found: " + GameConfig.Dialog.NEXT_BUTTON_TEXTURE_PATH);
        }
    }

    private void advanceDialog() {
        if (activeDialogNpc == null) {
            return;
        }

        if (isLastDialogLine()) {
            closeDialog();
            return;
        }

        activeDialogLineIndex++;
    }

    private boolean isLastDialogLine() {
        if (activeDialogNpc == null) {
            return true;
        }

        NpcDialogueEntry dialogueEntry = dialogueLibrary.getEntry(activeDialogNpc.getDialogueId());
        return activeDialogLineIndex >= dialogueEntry.getLineCount() - 1;
    }

    private void placePlayerAtDefaultSpawn() {
        Vector2 playerSpawn = tiledWorld.findPlayerSpawn(player.getDrawWidth(), player.getDrawHeight());
        if (playerSpawn != null) {
            player.setBottomLeft(playerSpawn.x, playerSpawn.y);
        }
    }

    private Array<NPC> createNpcs() {
        Array<NPC> worldNpcs = new Array<NPC>();
        worldNpcs.add(
            new NPCHero(
                (HERO_CAMP_MIN_X + HERO_CAMP_MAX_X) * 0.5f,
                (HERO_CAMP_MIN_Y + HERO_CAMP_MAX_Y) * 0.5f,
                HERO_CAMP_MIN_X,
                HERO_CAMP_MIN_Y,
                HERO_CAMP_MAX_X,
                HERO_CAMP_MAX_Y,
                NPCHero.HERO_PROFILE
            )
        );
        worldNpcs.add(
            new NPCHero(
                (GUARDIAN_MIN_X + GUARDIAN_MAX_X) * 0.5f,
                (GUARDIAN_MIN_Y + GUARDIAN_MAX_Y) * 0.5f,
                GUARDIAN_MIN_X,
                GUARDIAN_MIN_Y,
                GUARDIAN_MAX_X,
                GUARDIAN_MAX_Y,
                NPCHero.GUARDIAN_PROFILE
            )
        );
        return worldNpcs;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void resize(int width, int height) {
        DarkRomanceGame.enforceDesktopAspect(width, height);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void dispose() {
        stopBackgroundMusic();
        SaveManager.savePlayer(playerData);

        if (batch != null) {
            batch.dispose();
        }
        if (pixel != null) {
            pixel.dispose();
        }

        if (dialogFrameTexture != null) {
            dialogFrameTexture.dispose();
        }
        if (dialogNameplateTexture != null) {
            dialogNameplateTexture.dispose();
        }
        if (dialogNextButtonTexture != null) {
            dialogNextButtonTexture.dispose();
        }

        if (dialogueLibrary != null) {
            dialogueLibrary.dispose();
        }
        if (tiledWorld != null) {
            tiledWorld.dispose();
        }
        if (characterWindow != null) {
            characterWindow.dispose();
        }
        if (gameMenuWindow != null) {
            gameMenuWindow.dispose();
        }

        if (player != null) {
            player.dispose();
        }
        if (npcs != null) {
            for (NPC npc : npcs) {
                npc.dispose();
            }
        }
    }

    @Override
    public void hide() {
        stopBackgroundMusic();
        SaveManager.savePlayer(playerData);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private void startBackgroundMusic() {
        if (backgroundMusic != null || !Gdx.files.internal(GAME_MUSIC_PATH).exists()) {
            return;
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(GAME_MUSIC_PATH));
        backgroundMusic.setLooping(true);
        applyMusicVolume();
        backgroundMusic.play();
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic == null) {
            return;
        }

        backgroundMusic.stop();
        backgroundMusic.dispose();
        backgroundMusic = null;
    }

    private void toggleMusicMute() {
        musicMuted = !musicMuted;
        applyMusicVolume();
    }

    private void setMusicVolume(float targetVolume) {
        musicVolume = MathUtils.clamp(targetVolume, 0f, 1f);
        applyMusicVolume();
    }

    private void applyMusicVolume() {
        if (backgroundMusic == null) {
            return;
        }
        backgroundMusic.setVolume(musicMuted ? 0f : musicVolume);
    }

    private enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
