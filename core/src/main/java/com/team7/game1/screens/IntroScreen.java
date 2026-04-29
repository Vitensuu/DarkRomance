package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.models.PlayerData;

public class IntroScreen implements Screen {
    private final DarkRomanceGame game;
    private Stage stage;

    public IntroScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        PlayerData playerData = game.getCurrentPlayerData();
        String playerName = playerData != null ? playerData.getUsername() : "Hero";

        Label introText = new Label(
            playerName + ", you wake up in a dark forest...\n" +
                "Your task is to find the way out and uncover the secret.\n\n" +
                "Press 'Play' to begin your journey.",
            DarkRomanceGame.skin
        );
        introText.setWrap(true);
        introText.setAlignment(Align.center);

        VisTextButton playButton = new VisTextButton("Play");
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

        table.add(introText).width(600).padBottom(40).row();
        table.add(playButton).width(200);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
