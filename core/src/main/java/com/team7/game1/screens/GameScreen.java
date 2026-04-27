package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.models.CharacterAnimationState;
import com.team7.game1.models.NPCHero;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.models.NPC;
import com.team7.game1.DarkRomanceGame;

public class GameScreen implements Screen {

    private static final Color WORLD_COLOR = Color.valueOf("2F4A2CFF");
    private static final Color WORLD_ACCENT = Color.valueOf("405C34FF");

    private final DarkRomanceGame game;
    private SpriteBatch batch;
    private FitViewport viewport;
    private Texture pixel;
    private PlayerCharacter player;
    private Array<NPC> npcs;
    private MoveDirection lastPressedDirection = MoveDirection.DOWN;

    public GameScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        pixel = createSolidTexture(Color.WHITE);
        player = new PlayerCharacter(
            DarkRomanceGame.DESIGN_WIDTH / 2f - 32f,
            DarkRomanceGame.DESIGN_HEIGHT / 2f - 64f
        );
        npcs = new Array<NPC>();
        npcs.add(new NPCHero(220f, 160f, 120f, 120f, 420f, 280f, NPCHero.ROBE_ARCHER));
        npcs.add(new NPCHero(860f, 420f, 760f, 360f, 1080f, 620f, NPCHero.MIXED_METAL_ARCHER));
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(WORLD_COLOR.r, WORLD_COLOR.g, WORLD_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawGround();

        TextureRegion frame = player.getCurrentFrame();
        batch.setColor(Color.WHITE);
        drawPatrolBounds();
        drawNpcs();
        batch.draw(frame, player.getX(), player.getY(), player.getDrawWidth(), player.getDrawHeight());
        batch.end();
    }

    private void update(float delta) {
        updateLastPressedDirection();

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

        player.update(delta, moveX, moveY, DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        for (NPC npc : npcs) {
            npc.update(delta, player);
        }
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

    private void drawPatrolBounds() {
        batch.setColor(Color.valueOf("FFFFFF12"));
        batch.draw(pixel, 120f, 120f, 300f, 160f);
        batch.draw(pixel, 760f, 360f, 320f, 260f);
        batch.setColor(Color.WHITE);
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
        batch.dispose();
        pixel.dispose();
        player.dispose();
        for (NPC npc : npcs) {
            npc.dispose();
        }
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    private enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
