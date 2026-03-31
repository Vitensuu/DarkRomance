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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.DarkRomanceGame;

public class GameScreen implements Screen {

    private static final Color WORLD_COLOR = Color.valueOf("2F4A2CFF");
    private static final Color WORLD_ACCENT = Color.valueOf("405C34FF");

    private final DarkRomanceGame game;
    private SpriteBatch batch;
    private FitViewport viewport;
    private Texture pixel;
    private PlayerCharacter player;

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
        batch.draw(frame, player.getX(), player.getY(), player.getDrawWidth(), player.getDrawHeight());
        batch.end();
    }

    private void update(float delta) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= 1f;
        }

        if (moveX != 0f && moveY != 0f) {
            moveY = 0f;
        }

        player.update(delta, moveX, moveY, DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
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
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
