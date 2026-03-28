package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
    private VisTextField usernameField;
    private VisTextField passwordField;
    private Label errorLabel;
    private NetworkClient networkClient;
    private VisDialog loadingDialog;

    public LoginScreen(DarkRomanceGame game) {
        this.game = game;
        this.networkClient = new NetworkClient();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        usernameField = new VisTextField();
        usernameField.setMessageText("Логин");
        passwordField = new VisTextField();
        passwordField.setPasswordMode(true);
        passwordField.setMessageText("Пароль");

        VisTextButton loginButton = new VisTextButton("Войти");
        VisTextButton registerButton = new VisTextButton("Регистрация");
        errorLabel = new Label("", DarkRomanceGame.skin);
        errorLabel.setColor(Color.RED);

        table.add(usernameField).width(200).padBottom(10).row();
        table.add(passwordField).width(200).padBottom(20).row();
        table.add(loginButton).width(100).padRight(10);
        table.add(registerButton).width(100).row();
        table.add(errorLabel).colspan(2).padTop(20);

        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptLogin();
            }
        });

        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptRegister();
            }
        });

        // Диалог ожидания
        loadingDialog = new VisDialog("Подключение");
        loadingDialog.setModal(true);
        loadingDialog.add(new Label("Пожалуйста, подождите...", DarkRomanceGame.skin));
        loadingDialog.pack();
    }

    private void showLoading() {
        loadingDialog.show(stage);
        // блокируем ввод в поля, чтобы не нажимали повторно
        usernameField.setDisabled(true);
        passwordField.setDisabled(true);
    }

    private void hideLoading() {
        loadingDialog.fadeOut();
        usernameField.setDisabled(false);
        passwordField.setDisabled(false);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля");
            return;
        }
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
        networkClient.send("LOGIN:" + username + ":" + password);
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля");
            return;
        }
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
        networkClient.send("REGISTER:" + username + ":" + password);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
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
        networkClient.disconnect();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
