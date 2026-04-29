package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;

public class IntroScreen implements Screen {

    private static final String INTRO_BACKGROUND_PATH = "backgraund/IntroScreenBackground.jpg";

    private final DarkRomanceGame game;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private SpriteBatch batch;
    private FitViewport viewport;
    private Texture background;

    public IntroScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        background = new Texture(Gdx.files.internal(INTRO_BACKGROUND_PATH));
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);

        BitmapFont hintFont = DarkRomanceGame.skin.getFont("default-font");
        hintFont.setColor(Color.valueOf("E8DEC9FF"));
        glyphLayout.setText(hintFont, "Press Enter to continue");
        hintFont.draw(
            batch,
            glyphLayout,
            (DarkRomanceGame.DESIGN_WIDTH - glyphLayout.width) * 0.5f,
            42f
        );
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
