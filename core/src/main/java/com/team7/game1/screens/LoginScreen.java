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
    private VisTextField passwordField;
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
        usernameField.setMessageText("Логин");

        passwordField = new VisTextField();
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordField.setMessageText("Пароль");

        // кнопки
        Label loginButton = new Label("Войти", DarkRomanceGame.skin);
        loginButton.setColor(Color.WHITE);

        Label registerLink = new Label("Нет аккаунта? Регистрация", DarkRomanceGame.skin);
        registerLink.setColor(Color.CYAN);

        Label loginLink = new Label("Уже есть аккаунт? Войти", DarkRomanceGame.skin);
        loginLink.setColor(Color.CYAN);

        errorLabel = new Label("", DarkRomanceGame.skin);
        errorLabel.setColor(Color.RED);

        // 📐 UI
        table.add(title).colspan(2).padBottom(40).row();
        table.add(usernameField).width(250).row();
        table.add(passwordField).width(250).row();
        table.add(loginButton).padTop(10).row();
        table.add(registerLink).padTop(5).row();
        table.add(loginLink).padTop(5).row();
        table.add(errorLabel).padTop(20).row();

        // 🎯 события
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isLoginMode) {
                    attemptLogin();
                } else {
                    attemptRegister();
                }
            }
        });

        registerLink.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isLoginMode = false;

                loginButton.setText("Зарегистрироваться");
                title.setText("DARK ROMANCE\nРегистрация"); // 🔥 ВАЖНО
                errorLabel.setText("Введите данные для регистрации");
            }
        });

        loginLink.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isLoginMode = true;

                loginButton.setText("Войти");
                title.setText("DARK ROMANCE\nВход"); // 🔥 ВАЖНО
                errorLabel.setText("Введите данные для входа");
            }
        });

        // ⏳ диалог загрузки
        loadingDialog = new VisDialog("Подключение");
        loadingDialog.setModal(true);
        loadingDialog.add(new Label("Пожалуйста, подождите...", DarkRomanceGame.skin));
        loadingDialog.pack();
    }

    private void showLoading() {
        loadingDialog.show(stage);
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
