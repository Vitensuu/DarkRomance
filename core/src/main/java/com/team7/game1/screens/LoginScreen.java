package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.network.NetworkCallback;
import com.team7.game1.network.NetworkClient;
import com.team7.game1.utils.Constants;

public class LoginScreen implements Screen {

    private final DarkRomanceGame game;
    private final NetworkClient networkClient;

    private Stage stage;
    private SpriteBatch batch;
    private Texture background;
    private Texture overlayPixel;
    private Texture transparentPixel;
    private Texture namePanelTexture;
    private Texture confirmPanelTexture;

    private Table entryTable;
    private Container<Table> confirmContainer;
    private TextField usernameField;
    private Label errorLabel;
    private Label confirmNameLabel;
    private ActionTextButton readyLabel;
    private ActionTextButton backLabel;
    private ActionTextButton yesLabel;
    private ActionTextButton noLabel;
    private boolean loginInProgress;

    public LoginScreen(DarkRomanceGame game) {
        this.game = game;
        this.networkClient = new NetworkClient();
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT));
        batch = new SpriteBatch();
        background = new Texture(Gdx.files.internal("backgraund/LoginScreenBackgraund.png"));
        overlayPixel = createSolidTexture(Color.valueOf("FFFFFFFF"));
        transparentPixel = createSolidTexture(Color.valueOf("00000000"));
        namePanelTexture = createNamePanelTexture(512, 120, 10, 0.3f, 0f);
        confirmPanelTexture = createNamePanelTexture(720, 388, 10, 0.3f, 0f);

        buildUi();
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage);
        stage.setKeyboardFocus(usernameField);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void buildUi() {
        // Получаем шрифты
        BitmapFont titleFont = DarkRomanceGame.skin.getFont("GuildensternTitle");   // 90
        BitmapFont smallFont = DarkRomanceGame.skin.getFont("GuildensternSmall");   // 64

        entryTable = new Table();
        entryTable.setFillParent(true);
        entryTable.center();
        entryTable.padTop(100f);
        entryTable.defaults().pad(10f);
        stage.addActor(entryTable);

        // Стиль для крупных заголовков
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;

        Label.LabelStyle titleShadowStyle = new Label.LabelStyle();
        titleShadowStyle.font = DarkRomanceGame.skin.getFont("GuildensternTitleShadow");
        titleShadowStyle.fontColor = Color.valueOf("00000040");

        // Стиль для яркого текста (имя в диалоге)
        Label.LabelStyle brightStyle = new Label.LabelStyle();
        brightStyle.font = smallFont;   // имя пользователя пишем маленьким шрифтом
        brightStyle.fontColor = Color.WHITE;

        // Стиль для ошибок (тоже маленький шрифт)
        Label.LabelStyle errorStyle = new Label.LabelStyle();
        errorStyle.font = smallFont;
        errorStyle.fontColor = Color.valueOf("F25959FF");

        // Стиль для текстового поля
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle();
        fieldStyle.font = smallFont;
        fieldStyle.fontColor = Color.WHITE;
        fieldStyle.messageFont = smallFont;
        fieldStyle.messageFontColor = Color.valueOf("FFFFFF73");
        fieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(overlayPixel));
        TextureRegionDrawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(transparentPixel));
        fieldStyle.background = transparentDrawable;
        fieldStyle.focusedBackground = transparentDrawable;
        fieldStyle.disabledBackground = transparentDrawable;
        fieldStyle.selection = transparentDrawable;

        // Создаём виджеты
        Stack promptLabel = createShadowLabel("Дайте имя герою", titleShadowStyle, titleStyle, 4f, -8f);

        usernameField = new TextField("", fieldStyle);
        usernameField.setMessageText("Имя");
        usernameField.setAlignment(Align.center);
        usernameField.setMaxLength(18);

        errorLabel = new Label("", errorStyle);
        errorLabel.setAlignment(Align.center);

        Container<TextField> fieldContainer = new Container<>(usernameField);
        fieldContainer.background(new TextureRegionDrawable(new TextureRegion(namePanelTexture)));
        fieldContainer.pad(30f, 34f, 24f, 34f);
        fieldContainer.width(360f);
        fieldContainer.height(50f);

        // Кнопки с маленьким шрифтом (создаются через метод createActionLabel, который мы тоже поправим)
        backLabel = createActionLabel("Назад", false, actor -> removeLastLetter());
        readyLabel = createActionLabel("Готово", true, actor -> handleReadyPressed());

        Table actionTable = new Table();
        actionTable.add(backLabel.stack).padRight(40f);
        actionTable.add(readyLabel.stack);

        entryTable.add(promptLabel).padBottom(20f).row();
        entryTable.add(fieldContainer).padBottom(18f).row();
        entryTable.add(actionTable).padBottom(8f).row();
        entryTable.add(errorLabel).width(430f);

        // --- Диалог подтверждения ---
        Table confirmTable = new Table();
        confirmTable.background(new TextureRegionDrawable(new TextureRegion(confirmPanelTexture)));
        confirmTable.defaults().pad(14f);

        Stack confirmTitle = createShadowLabel("Вы уверены в имени?", titleShadowStyle, titleStyle, 4f, -8f);

        confirmNameLabel = new Label("", brightStyle);
        confirmNameLabel.setAlignment(Align.center);

        // Кнопки диалога (тоже маленький шрифт)
        yesLabel = createActionLabel("Да", true, actor -> confirmLogin());
        noLabel = createActionLabel("Нет", false, actor -> closeConfirm());

        Table confirmActions = new Table();
        confirmActions.add(yesLabel.stack).padRight(80f);
        confirmActions.add(noLabel.stack);

        confirmTable.add(confirmTitle).padTop(24f).row();
        confirmTable.add(confirmNameLabel).padTop(6f).padBottom(16f).row();
        confirmTable.add(confirmActions).padBottom(24f);

        confirmContainer = new Container<>(confirmTable);
        confirmContainer.setFillParent(true);
        confirmContainer.center();
        confirmContainer.width(720f);
        confirmContainer.height(388f);
        confirmContainer.setVisible(false);
        stage.addActor(confirmContainer);
    }

    private ActionTextButton createActionLabel(String text, boolean bright, ClickAction action) {
        BitmapFont smallFont = DarkRomanceGame.skin.getFont("GuildensternSmall");
        BitmapFont smallShadowFont = DarkRomanceGame.skin.getFont("GuildensternSmallShadow");

        Label.LabelStyle actionStyle = new Label.LabelStyle();
        actionStyle.font = smallFont;

        Label.LabelStyle shadowStyle = new Label.LabelStyle();
        shadowStyle.font = smallShadowFont;
        shadowStyle.fontColor = Color.valueOf("00000040");

        Stack stack = createShadowLabel(text, shadowStyle, actionStyle, 4f, -8f);
        Label label = (Label) ((Container<?>) stack.getChildren().get(1)).getActor();

        stack.setTouchable(Touchable.enabled);
        stack.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!loginInProgress) {
                    label.setColor(Color.valueOf("F2F2F2FF"));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (!loginInProgress) {
                    label.setColor(Color.valueOf("F2F2F2FF"));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!loginInProgress) {
                    action.run(stack);
                }
            }
        });
        return new ActionTextButton(stack, label, bright);
    }

    private Stack createShadowLabel(String text, Label.LabelStyle shadowStyle, Label.LabelStyle mainStyle,
                                    float shadowX, float shadowY) {
        Label shadowLabel = new Label(text, shadowStyle);
        shadowLabel.setAlignment(Align.center);

        Label mainLabel = new Label(text, mainStyle);
        mainLabel.setAlignment(Align.center);

        Container<Label> shadowContainer = new Container<>(shadowLabel);
        shadowContainer.padTop(Math.max(0f, -shadowY));
        shadowContainer.padBottom(Math.max(0f, shadowY));
        shadowContainer.padLeft(Math.max(0f, shadowX));
        shadowContainer.padRight(Math.max(0f, -shadowX));

        Container<Label> mainContainer = new Container<>(mainLabel);
        mainContainer.padTop(Math.max(0f, shadowY));
        mainContainer.padBottom(Math.max(0f, -shadowY));
        mainContainer.padLeft(Math.max(0f, -shadowX));
        mainContainer.padRight(Math.max(0f, shadowX));

        Stack stack = new Stack();
        stack.add(shadowContainer);
        stack.add(mainContainer);
        return stack;
    }

    private void handleReadyPressed() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setFontScale(0.6f);
            errorLabel.setText("Введите имя");
            return;
        }

        errorLabel.setText("");
        confirmNameLabel.setText(username);
        confirmContainer.setVisible(true);
        entryTable.setVisible(false);
        entryTable.setTouchable(Touchable.disabled);
        stage.unfocusAll();
    }

    private void removeLastLetter() {
        String current = usernameField.getText();
        if (current == null || current.isEmpty()) {
            return;
        }

        int newEnd = current.offsetByCodePoints(current.length(), -1);
        usernameField.setText(current.substring(0, newEnd));
        usernameField.setCursorPosition(usernameField.getText().length());
        errorLabel.setText("");
    }

    private void closeConfirm() {
        confirmContainer.setVisible(false);
        entryTable.setVisible(true);
        entryTable.setTouchable(Touchable.enabled);
        stage.setKeyboardFocus(usernameField);
    }

    private void confirmLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            closeConfirm();
            errorLabel.setText("Введите имя");
            return;
        }
        attemptLogin(username);
    }

    private void attemptLogin(String username) {
        loginInProgress = true;
        errorLabel.setText("Подключение...");
        updateInteractiveState();

        networkClient.connect(Constants.SERVER_HOST, Constants.SERVER_PORT, new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                Gdx.app.postRunnable(() -> {
                    loginInProgress = false;
                    updateInteractiveState();

                    if ("OK".equals(response)) {
                        game.setScreen(new IntroScreen(game));
                    } else {
                        closeConfirm();
                        errorLabel.setText(response);
                    }

                    networkClient.disconnect();
                });
            }
        });
        networkClient.send("LOGIN:" + username);
    }

    private void updateInteractiveState() {
        boolean enabled = !loginInProgress;
        usernameField.setDisabled(!enabled);
        readyLabel.stack.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        backLabel.stack.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        yesLabel.stack.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        noLabel.stack.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        batch.setColor(0f, 0f, 0f, 0.08f);
        batch.draw(overlayPixel, 0, 0, DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        batch.setColor(Color.WHITE);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createNamePanelTexture(int width, int height, int radius, float baseAlpha, float shadowAlpha) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        Color clear = new Color(0f, 0f, 0f, 0f);
        Color fill = new Color(0f, 0f, 0f, baseAlpha);
        Color shadow = new Color(0f, 0f, 0f, shadowAlpha);
        int shadowSize = 6;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isInsideRoundedRect(x, y, width, height, radius)) {
                    pixmap.drawPixel(x, y, Color.rgba8888(clear));
                    continue;
                }

                float alpha = fill.a;
                int distanceToEdge = Math.min(Math.min(x, width - 1 - x), Math.min(y, height - 1 - y));
                if (distanceToEdge < shadowSize) {
                    float progress = 1f - (distanceToEdge / (float) shadowSize);
                    alpha += shadow.a * progress;
                }

                pixmap.drawPixel(x, y, Color.rgba8888(0f, 0f, 0f, MathUtils.clamp(alpha, 0f, 1f)));
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private boolean isInsideRoundedRect(int x, int y, int width, int height, int radius) {
        int left = radius;
        int right = width - radius - 1;
        int bottom = radius;
        int top = height - radius - 1;

        if (x >= left && x <= right) {
            return true;
        }
        if (y >= bottom && y <= top) {
            return true;
        }

        int circleX = x < left ? left : right;
        int circleY = y < bottom ? bottom : top;
        int dx = x - circleX;
        int dy = y - circleY;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void resize(int width, int height) {
        DarkRomanceGame.enforceDesktopAspect(width, height);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        background.dispose();
        overlayPixel.dispose();
        transparentPixel.dispose();
        namePanelTexture.dispose();
        confirmPanelTexture.dispose();
        networkClient.disconnect();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    private interface ClickAction {
        void run(Actor actor);
    }

    private static class ActionTextButton {
        final Stack stack;
        final Label label;
        final boolean bright;

        ActionTextButton(Stack stack, Label label, boolean bright) {
            this.stack = stack;
            this.label = label;
            this.bright = bright;
        }
    }
}
