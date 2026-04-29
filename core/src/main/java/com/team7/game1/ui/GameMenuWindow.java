package com.team7.game1.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.GameConfig;

public class GameMenuWindow {

    public enum MenuAction {
        NONE,
        CONTINUE,
        TOGGLE_MUTE,
        VOLUME_DOWN,
        VOLUME_UP,
        EXIT_GAME
    }

    private static final float WINDOW_WIDTH = 760f;
    private static final float WINDOW_HEIGHT = 350f;
    private static final float TITLE_TOP_OFFSET = 88f;
    private static final float OPTION_START_OFFSET = 156f;
    private static final float OPTION_GAP = 56f;
    private static final float OPTION_WIDTH = 360f;
    private static final float OPTION_HEIGHT = 40f;

    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Rectangle[] optionBounds = new Rectangle[] {
        new Rectangle(),
        new Rectangle(),
        new Rectangle(),
        new Rectangle()
    };
    private final Vector2 touchPoint = new Vector2();
    private final Texture windowTexture;
    private boolean visible;
    private int selectedIndex;

    public GameMenuWindow() {
        windowTexture = Gdx.files.internal(GameConfig.Ui.CHARACTER_WINDOW_TEXTURE_PATH).exists()
            ? new Texture(Gdx.files.internal(GameConfig.Ui.CHARACTER_WINDOW_TEXTURE_PATH))
            : null;
    }

    public void open() {
        visible = true;
        selectedIndex = 0;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public MenuAction handleInput(FitViewport viewport) {
        if (!visible) {
            return MenuAction.NONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex + optionBounds.length - 1) % optionBounds.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % optionBounds.length;
        }
        if (selectedIndex == 2 && Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            return MenuAction.VOLUME_DOWN;
        }
        if (selectedIndex == 2 && Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            return MenuAction.VOLUME_UP;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            float worldX = touchPoint.x;
            float worldY = touchPoint.y;
            for (int i = 0; i < optionBounds.length; i++) {
                if (optionBounds[i].contains(worldX, worldY)) {
                    selectedIndex = i;
                    if (i == 2) {
                        float centerX = optionBounds[i].x + optionBounds[i].width * 0.5f;
                        return worldX < centerX ? MenuAction.VOLUME_DOWN : MenuAction.VOLUME_UP;
                    }
                    return menuActionByIndex(i);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return menuActionByIndex(selectedIndex);
        }

        return MenuAction.NONE;
    }

    public void draw(SpriteBatch batch, Texture pixel, FitViewport viewport, boolean muted, float volume) {
        if (!visible) {
            return;
        }

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float x = camera.position.x - viewport.getWorldWidth() * 0.5f + (viewport.getWorldWidth() - WINDOW_WIDTH) * 0.5f;
        float y = camera.position.y - viewport.getWorldHeight() * 0.5f + (viewport.getWorldHeight() - WINDOW_HEIGHT) * 0.5f;

        drawPanel(batch, pixel, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);

        BitmapFont titleFont = DarkRomanceGame.skin.getFont("GuildensternSmall");
        BitmapFont bodyFont = DarkRomanceGame.skin.getFont("default-font");
        float originalScaleX = titleFont.getData().scaleX;
        float originalScaleY = titleFont.getData().scaleY;
        titleFont.getData().setScale(0.6f);

        titleFont.setColor(Color.valueOf("513729FF"));
        bodyFont.setColor(Color.valueOf("4B3427FF"));

        String title = "Game Menu";
        glyphLayout.setText(titleFont, title);
        titleFont.draw(batch, glyphLayout, x + (WINDOW_WIDTH - glyphLayout.width) * 0.5f, y + WINDOW_HEIGHT - TITLE_TOP_OFFSET);

        drawOption(batch, pixel, titleFont, x, y, 0, "Continue");
        drawOption(batch, pixel, titleFont, x, y, 1, muted ? "Sound: Off" : "Sound: On");
        drawOption(batch, pixel, titleFont, x, y, 2, "Volume: " + Math.round(volume * 100f) + "%");
        drawOption(batch, pixel, titleFont, x, y, 3, "Exit Game");

        String hint = "Esc - close | Enter - select | Left/Right - volume";
        glyphLayout.setText(bodyFont, hint);
        bodyFont.draw(batch, glyphLayout, x + (WINDOW_WIDTH - glyphLayout.width) * 0.5f, y + 36f);

        titleFont.getData().setScale(originalScaleX, originalScaleY);
    }

    public void dispose() {
        if (windowTexture != null) {
            windowTexture.dispose();
        }
    }

    private void drawPanel(SpriteBatch batch, Texture pixel, float x, float y, float width, float height) {
        batch.setColor(Color.valueOf("00000080"));
        batch.draw(pixel, x + 10f, y - 10f, width, height);
        batch.setColor(Color.WHITE);

        if (windowTexture != null) {
            batch.draw(windowTexture, x, y, width, height);
            return;
        }

        batch.setColor(Color.valueOf("CDB79CFF"));
        batch.draw(pixel, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawOption(SpriteBatch batch, Texture pixel, BitmapFont font, float panelX, float panelY,
                            int index, String label) {
        float optionX = panelX + (WINDOW_WIDTH - OPTION_WIDTH) * 0.5f;
        float optionY = panelY + WINDOW_HEIGHT - OPTION_START_OFFSET - index * OPTION_GAP;
        optionBounds[index].set(optionX, optionY, OPTION_WIDTH, OPTION_HEIGHT);

        if (index == selectedIndex) {
            batch.setColor(Color.valueOf("A88460CC"));
            batch.draw(pixel, optionX, optionY, OPTION_WIDTH, OPTION_HEIGHT);
            batch.setColor(Color.valueOf("5D4030FF"));
            batch.draw(pixel, optionX, optionY, OPTION_WIDTH, 2f);
            batch.draw(pixel, optionX, optionY + OPTION_HEIGHT - 2f, OPTION_WIDTH, 2f);
            batch.draw(pixel, optionX, optionY, 2f, OPTION_HEIGHT);
            batch.draw(pixel, optionX + OPTION_WIDTH - 2f, optionY, 2f, OPTION_HEIGHT);
        } else {
            batch.setColor(Color.valueOf("E6D2B9A6"));
            batch.draw(pixel, optionX, optionY, OPTION_WIDTH, OPTION_HEIGHT);
        }
        batch.setColor(Color.WHITE);

        glyphLayout.setText(font, label);
        font.draw(
            batch,
            glyphLayout,
            optionX + (OPTION_WIDTH - glyphLayout.width) * 0.5f,
            optionY + OPTION_HEIGHT * 0.5f + glyphLayout.height * 0.5f - 2f
        );
    }

    private MenuAction menuActionByIndex(int index) {
        if (index == 0) {
            return MenuAction.CONTINUE;
        }
        if (index == 1) {
            return MenuAction.TOGGLE_MUTE;
        }
        if (index == 2) {
            return MenuAction.VOLUME_UP;
        }
        return MenuAction.EXIT_GAME;
    }
}
