package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.models.CharacterAnimationState;
import com.team7.game1.models.Interactable;
import com.team7.game1.models.NPC;
import com.team7.game1.models.NPCHero;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.models.PlayerData;
import com.team7.game1.ui.CharacterWindow;
import com.team7.game1.ui.NpcDialogueEntry;
import com.team7.game1.ui.NpcDialogueLibrary;
import com.team7.game1.utils.SaveManager;

public class GameScreen implements Screen {

    private static final Color WORLD_COLOR = Color.valueOf("2F4A2CFF");
    private static final Color WORLD_ACCENT = Color.valueOf("405C34FF");
    private static final float MAP_SCALE = 3.2f;
    private static final String DIALOG_FRAME_TEXTURE_PATH = "ui/dialog/UI_Flat_Frame01a.png";
    private static final String DIALOG_NAMEPLATE_TEXTURE_PATH = "ui/dialog/UI_Flat_FrameMarker01a.png";
    private static final String DIALOG_NEXT_BUTTON_TEXTURE_PATH = "ui/dialog/UI_Flat_Button02a_1.png";
    private static final float DIALOG_X = 90f;
    private static final float DIALOG_Y = 20f;
    private static final float DIALOG_WIDTH = DarkRomanceGame.DESIGN_WIDTH - 180f;
    private static final float DIALOG_HEIGHT = 210f;
    private static final float DIALOG_PADDING = 26f;
    private static final float DIALOG_PORTRAIT_SIZE = 120f;
    private static final float DIALOG_NEXT_BUTTON_WIDTH = 140f;
    private static final float DIALOG_NEXT_BUTTON_HEIGHT = 42f;
    private static final float DIALOG_NAMEPLATE_HEIGHT = 34f;

    private final DarkRomanceGame game;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Rectangle nextButtonBounds = new Rectangle();
    private final Vector2 worldTouchPoint = new Vector2();
    private SpriteBatch batch;
    private FitViewport viewport;
    private Texture pixel;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
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

    public GameScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        pixel = createSolidTexture(Color.WHITE);
        loadWorldMap();
        loadDialogTextures();
        dialogueLibrary = new NpcDialogueLibrary();
        playerData = game.getCurrentPlayerData();
        if (playerData == null) {
            playerData = new PlayerData();
            game.setCurrentPlayerData(playerData);
        }

        float startX = playerData.getWorldX() > 0f ? playerData.getWorldX() : worldWidth / 2f;
        float startY = playerData.getWorldY() > 0f ? playerData.getWorldY() : worldHeight / 2f - 64f;
        player = new PlayerCharacter(startX, startY);
        characterWindow = new CharacterWindow();
        npcs = new Array<NPC>();
        npcs.add(new NPCHero(220f, 160f, 120f, 120f, 420f, 280f, NPCHero.ROBE_ARCHER));
        npcs.add(new NPCHero(860f, 420f, 760f, 360f, 1080f, 620f, NPCHero.MIXED_METAL_ARCHER));
    }

    @Override
    public void render(float delta) {
        update(delta);
        updateCamera();

        Gdx.gl.glClearColor(WORLD_COLOR.r, WORLD_COLOR.g, WORLD_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        if (mapRenderer != null) {
            mapRenderer.setView((OrthographicCamera) viewport.getCamera());
            mapRenderer.render();
        }
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (mapRenderer == null) {
            drawGround();
        }

        TextureRegion frame = player.getCurrentFrame();
        batch.setColor(Color.WHITE);
        drawPatrolBounds();
        drawNpcs();
        batch.draw(frame, player.getX(), player.getY(), player.getDrawWidth(), player.getDrawHeight());
        drawDialog();
        characterWindow.draw(batch, pixel, viewport, playerData);
        batch.end();
    }

    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            characterWindow.toggle();
        }
        updateLastPressedDirection();
        handleDialogInput();

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            player.triggerAnimationState(CharacterAnimationState.ATTACK_MAGIC, 0.75f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            player.triggerAnimationState(CharacterAnimationState.ATTACK_BOW, 0.75f);
        }

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
        playerData.setWorldPosition(player.getCenterX(), player.getY());
        for (NPC npc : npcs) {
            npc.update(delta, player);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            activeDialogNpc = null;
            activeDialogLineIndex = 0;
            return;
        }

        if (activeDialogNpc != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                advanceDialog();
                return;
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                viewport.unproject(worldTouchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
                if (nextButtonBounds.contains(worldTouchPoint)) {
                    advanceDialog();
                    return;
                }
            }

            return;
        }

        if (!Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            return;
        }

        Interactable nearestInteractable = findNearestInteractable();
        if (nearestInteractable instanceof NPC) {
            nearestInteractable.interact(player);
            activeDialogNpc = (NPC) nearestInteractable;
            activeDialogLineIndex = 0;
            return;
        }

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
        float panelX = camera.position.x - viewport.getWorldWidth() * 0.5f + DIALOG_X;
        float panelY = camera.position.y - viewport.getWorldHeight() * 0.5f + DIALOG_Y;

        if (dialogFramePatch != null) {
            batch.setColor(Color.WHITE);
            dialogFramePatch.draw(batch, panelX, panelY, DIALOG_WIDTH, DIALOG_HEIGHT);
        } else {
            batch.setColor(Color.valueOf("15110FCC"));
            batch.draw(pixel, panelX, panelY, DIALOG_WIDTH, DIALOG_HEIGHT);
            batch.setColor(Color.valueOf("BBA58DFF"));
            batch.draw(pixel, panelX, panelY + DIALOG_HEIGHT - 4f, DIALOG_WIDTH, 4f);
            batch.draw(pixel, panelX, panelY, DIALOG_WIDTH, 4f);
            batch.draw(pixel, panelX, panelY, 4f, DIALOG_HEIGHT);
            batch.draw(pixel, panelX + DIALOG_WIDTH - 4f, panelY, 4f, DIALOG_HEIGHT);
        }

        BitmapFont titleFont = DarkRomanceGame.skin.getFont("GuildensternSmall");
        BitmapFont titleShadowFont = DarkRomanceGame.skin.getFont("GuildensternSmallShadow");
        BitmapFont bodyFont = DarkRomanceGame.skin.getFont("default-font");
        float portraitX = panelX + DIALOG_PADDING;
        float portraitY = panelY + DIALOG_HEIGHT - DIALOG_PADDING - DIALOG_PORTRAIT_SIZE;
        float portraitInset = 8f;
        float textX = portraitX + DIALOG_PORTRAIT_SIZE + 24f;
        float titleTopY = panelY + DIALOG_HEIGHT - DIALOG_PADDING;
        float bodyTopY = titleTopY - 54f;
        float textWidth = DIALOG_WIDTH - (textX - panelX) - DIALOG_PADDING;
        float hintY = panelY + 24f;
        NpcDialogueEntry dialogueEntry = dialogueLibrary.getEntry(activeDialogNpc.getDialogueId());
        String[] dialogLines = dialogueEntry.getLines();
        String dialogText = dialogLines[Math.min(activeDialogLineIndex, dialogLines.length - 1)];
        Texture portraitTexture = dialogueLibrary.getPortrait(activeDialogNpc.getDialogueId());

        batch.setColor(Color.valueOf("D8DDE5FF"));
        batch.draw(pixel, portraitX, portraitY, DIALOG_PORTRAIT_SIZE, DIALOG_PORTRAIT_SIZE);
        batch.setColor(Color.valueOf("53606FFF"));
        batch.draw(pixel, portraitX, portraitY + DIALOG_PORTRAIT_SIZE - 4f, DIALOG_PORTRAIT_SIZE, 4f);
        batch.draw(pixel, portraitX, portraitY, DIALOG_PORTRAIT_SIZE, 4f);
        batch.draw(pixel, portraitX, portraitY, 4f, DIALOG_PORTRAIT_SIZE);
        batch.draw(pixel, portraitX + DIALOG_PORTRAIT_SIZE - 4f, portraitY, 4f, DIALOG_PORTRAIT_SIZE);
        batch.setColor(Color.WHITE);
        if (portraitTexture != null) {
            batch.draw(
                portraitTexture,
                portraitX + portraitInset,
                portraitY + portraitInset,
                DIALOG_PORTRAIT_SIZE - portraitInset * 2f,
                DIALOG_PORTRAIT_SIZE - portraitInset * 2f
            );
        } else {
            batch.draw(
                activeDialogNpc.getCurrentFrame(),
                portraitX + portraitInset,
                portraitY + portraitInset,
                DIALOG_PORTRAIT_SIZE - portraitInset * 2f,
                DIALOG_PORTRAIT_SIZE - portraitInset * 2f
            );
        }

        String npcName = dialogueEntry.getName();
        titleFont.setColor(Color.valueOf("284357FF"));
        titleShadowFont.setColor(Color.valueOf("E9EDF2FF"));
        bodyFont.setColor(Color.valueOf("2B2623FF"));
        glyphLayout.setText(titleFont, npcName);
        float nameplateWidth = Math.max(190f, glyphLayout.width + 48f);
        float nameplateX = textX - 8f;
        float nameplateY = panelY + DIALOG_HEIGHT - DIALOG_NAMEPLATE_HEIGHT - 16f;
        if (dialogNameplateTexture != null) {
            batch.draw(dialogNameplateTexture, nameplateX, nameplateY, nameplateWidth, DIALOG_NAMEPLATE_HEIGHT);
        } else {
            batch.setColor(Color.valueOf("D6DDE5FF"));
            batch.draw(pixel, nameplateX, nameplateY, nameplateWidth, DIALOG_NAMEPLATE_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        float nameTextX = nameplateX + 18f;
        float nameTextY = nameplateY + 25f;
        titleShadowFont.draw(batch, npcName, nameTextX + 1f, nameTextY - 1f);
        titleFont.draw(batch, npcName, nameTextX, nameTextY);
        bodyFont.draw(batch, dialogText, textX, bodyTopY, textWidth, Align.left, true);
        glyphLayout.setText(bodyFont, "Esc - close   F / Enter / Space - next");
        bodyFont.draw(batch, glyphLayout, panelX + DIALOG_PADDING, hintY);

        float buttonX = panelX + DIALOG_WIDTH - DIALOG_PADDING - DIALOG_NEXT_BUTTON_WIDTH;
        float buttonY = panelY + 18f;
        nextButtonBounds.set(buttonX, buttonY, DIALOG_NEXT_BUTTON_WIDTH, DIALOG_NEXT_BUTTON_HEIGHT);
        if (dialogNextButtonTexture != null) {
            batch.draw(dialogNextButtonTexture, buttonX, buttonY, DIALOG_NEXT_BUTTON_WIDTH, DIALOG_NEXT_BUTTON_HEIGHT);
        } else {
            batch.setColor(Color.valueOf("F4F0E8FF"));
            batch.draw(pixel, buttonX, buttonY, DIALOG_NEXT_BUTTON_WIDTH, DIALOG_NEXT_BUTTON_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        glyphLayout.setText(titleFont, isLastDialogLine() ? "Close" : "Next");
        titleFont.draw(
            batch,
            glyphLayout,
            buttonX + (DIALOG_NEXT_BUTTON_WIDTH - glyphLayout.width) / 2f,
            buttonY + DIALOG_NEXT_BUTTON_HEIGHT / 2f + glyphLayout.height / 2f - 4f
        );
        batch.setColor(Color.WHITE);
    }

    private void drawPatrolBounds() {
        batch.setColor(Color.valueOf("FFFFFF12"));
        batch.draw(pixel, 120f, 120f, 300f, 160f);
        batch.draw(pixel, 760f, 360f, 320f, 260f);
        batch.setColor(Color.WHITE);
    }

    private Interactable findNearestInteractable() {
        Interactable nearestInteractable = null;
        float nearestDistance = Float.MAX_VALUE;

        for (NPC npc : npcs) {
            if (!npc.canInteract(player)) {
                continue;
            }
            float npcCenterX = npc.getX() + npc.getWidth() / 2f;
            float npcCenterY = npc.getY() + npc.getHeight() / 2f;
            float distance = playerDistanceTo(npcCenterX, npcCenterY);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestInteractable = npc;
            }
        }

        return nearestInteractable;
    }

    private float playerDistanceTo(float x, float y) {
        float deltaX = player.getCenterX() - x;
        float deltaY = player.getCenterY() - y;
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void loadWorldMap() {
        try {
            tiledMap = new TmxMapLoader().load("maps/map oop.tmx");
            mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, MAP_SCALE, batch);
            Integer mapTilesWide = tiledMap.getProperties().get("width", Integer.class);
            Integer mapTilesHigh = tiledMap.getProperties().get("height", Integer.class);
            Integer tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
            Integer tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
            if (mapTilesWide != null && mapTilesHigh != null && tileWidth != null && tileHeight != null) {
                worldWidth = mapTilesWide * tileWidth * MAP_SCALE;
                worldHeight = mapTilesHigh * tileHeight * MAP_SCALE;
            }
        } catch (Exception exception) {
            Gdx.app.error("GameScreen", "Failed to load map maps/map oop.tmx", exception);
            tiledMap = null;
            mapRenderer = null;
            worldWidth = DarkRomanceGame.DESIGN_WIDTH;
            worldHeight = DarkRomanceGame.DESIGN_HEIGHT;
        }
    }

    private void loadDialogTextures() {
        if (Gdx.files.internal(DIALOG_FRAME_TEXTURE_PATH).exists()) {
            dialogFrameTexture = new Texture(Gdx.files.internal(DIALOG_FRAME_TEXTURE_PATH));
            dialogFramePatch = new NinePatch(new TextureRegion(dialogFrameTexture), 8, 8, 8, 8);
        } else {
            Gdx.app.log("GameScreen", "Dialog frame texture not found: " + DIALOG_FRAME_TEXTURE_PATH);
        }

        if (Gdx.files.internal(DIALOG_NAMEPLATE_TEXTURE_PATH).exists()) {
            dialogNameplateTexture = new Texture(Gdx.files.internal(DIALOG_NAMEPLATE_TEXTURE_PATH));
        } else {
            Gdx.app.log("GameScreen", "Dialog nameplate texture not found: " + DIALOG_NAMEPLATE_TEXTURE_PATH);
        }

        if (Gdx.files.internal(DIALOG_NEXT_BUTTON_TEXTURE_PATH).exists()) {
            dialogNextButtonTexture = new Texture(Gdx.files.internal(DIALOG_NEXT_BUTTON_TEXTURE_PATH));
        } else {
            Gdx.app.log("GameScreen", "Dialog next button texture not found: " + DIALOG_NEXT_BUTTON_TEXTURE_PATH);
        }
    }

    private void advanceDialog() {
        if (activeDialogNpc == null) {
            return;
        }

        if (isLastDialogLine()) {
            activeDialogNpc = null;
            activeDialogLineIndex = 0;
            return;
        }

        activeDialogLineIndex++;
    }

    private boolean isLastDialogLine() {
        if (activeDialogNpc == null) {
            return true;
        }

        NpcDialogueEntry dialogueEntry = dialogueLibrary.getEntry(activeDialogNpc.getDialogueId());
        return activeDialogLineIndex >= dialogueEntry.getLines().length - 1;
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
        SaveManager.savePlayer(playerData);
        batch.dispose();
        pixel.dispose();
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
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (characterWindow != null) {
            characterWindow.dispose();
        }
        player.dispose();
        for (NPC npc : npcs) {
            npc.dispose();
        }
    }

    @Override
    public void hide() {
        SaveManager.savePlayer(playerData);
    }
    @Override public void pause() {}
    @Override public void resume() {}

    private enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
