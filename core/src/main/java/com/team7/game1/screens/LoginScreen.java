package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.network.NetworkClient;
import com.team7.game1.network.NetworkCallback;
import com.team7.game1.utils.Constants;


public class LoginScreen implements Screen {

    private final DarkRomanceGame game;
    private Stage stage;
    private boolean isLoginMode = true;
    private Label title;

    private VisTextField usernameField;
    private Label errorLabel;

    private NetworkClient networkClient;
    private VisDialog loadingDialog;

    // 🎨 фон
    private Texture background;
    private SpriteBatch batch;

    public LoginScreen(DarkRomanceGame game) {
        this.game = game;
        this.networkClient = new NetworkClient();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        background = new Texture(Gdx.files.internal("images.jpeg"));
        batch = new SpriteBatch();

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.defaults().pad(10);
        stage.addActor(table);

        // 🔥 Заголовок
        title = new Label("DARK ROMANCE\nВход", DarkRomanceGame.skin);
        title.setFontScale(2.5f);
        title.setColor(Color.PINK);

        // поля
        usernameField = new VisTextField();
        usernameField.setMessageText("Введите имя");

        VisTextButton continueButton = new VisTextButton("Продолжить");
        errorLabel = new Label("", DarkRomanceGame.skin);
        errorLabel.setColor(Color.RED);

        table.add(usernameField).width(200).padBottom(20).row();
        table.add(continueButton).width(120).row();
        table.add(errorLabel).padTop(20);

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    errorLabel.setText("Введите имя");
                    return;
                }
                showConfirmDialog(username);
            }
        });

        // Диалог загрузки
        loadingDialog = new VisDialog("Подключение");
        loadingDialog.setModal(true);
        loadingDialog.add(new Label("Пожалуйста, подождите...", DarkRomanceGame.skin));
        loadingDialog.pack();
    }

    private void showConfirmDialog(String username) {
        VisDialog confirmDialog = new VisDialog("Подтверждение");
        confirmDialog.setModal(true);
        confirmDialog.add(new Label("Вы уверены, что хотите войти как " + username + "?", DarkRomanceGame.skin));
        confirmDialog.pack();

        VisTextButton yesButton = new VisTextButton("Да");
        VisTextButton noButton = new VisTextButton("Нет");
        confirmDialog.button(yesButton);
        confirmDialog.button(noButton);

        yesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.fadeOut();
                attemptLogin(username);
            }
        });

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.fadeOut();
            }
        });

        confirmDialog.show(stage);
    }

    private void attemptLogin(String username) {
        errorLabel.setText("");
        showLoading();

        networkClient.connect(Constants.SERVER_HOST, Constants.SERVER_PORT, new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                Gdx.app.postRunnable(() -> {
                    hideLoading();

                    if (response.equals("OK")) {
                        game.setScreen(new IntroScreen(game));
                    } else {
                        errorLabel.setText(response);
                    }

                    networkClient.disconnect();
                });
            }
        });
        networkClient.send("LOGIN:" + username);
    }

    private void showLoading() {
        loadingDialog.show(stage);
        usernameField.setDisabled(true);
    }

    private void hideLoading() {
        loadingDialog.fadeOut();
        usernameField.setDisabled(false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 🎨 фон
        batch.begin();
        batch.draw(background, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());

        // затемнение
        batch.setColor(0, 0, 0, 0.5f);
        batch.draw(background, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);
        batch.end();

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
        batch.dispose();
        background.dispose();
        networkClient.disconnect();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
