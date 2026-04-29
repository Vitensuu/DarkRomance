package com.team7.game1.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.team7.game1.DarkRomanceGame;
import com.team7.game1.models.PlayerData;

public class CharacterWindow {

    private static final String WINDOW_TEXTURE_PATH = "ui/character/character_window_frame.png";
    private static final String PORTRAIT_TEXTURE_PATH = "ui/dialog/portraits/archer_portrait.png";
    private static final float WINDOW_WIDTH = 620f;
    private static final float WINDOW_HEIGHT = 405f;
    private static final float PORTRAIT_X = 74f;
    private static final float PORTRAIT_Y = 116f;
    private static final float PORTRAIT_SIZE = 128f;
    private static final float CONTENT_X = 244f;
    private static final float CONTENT_TOP_OFFSET = 110f;
    private static final float LINE_GAP = 46f;
    private static final float HEALTH_LABEL_WIDTH = 88f;
    private static final float BAR_WIDTH = 190f;
    private static final float BAR_HEIGHT = 14f;
    private static final float FOOTER_RIGHT_PADDING = 72f;
    private static final float FIELD_FONT_SCALE = 0.58f;

    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Texture windowTexture;
    private final Texture portraitTexture;
    private boolean visible;

    public CharacterWindow() {
        windowTexture = Gdx.files.internal(WINDOW_TEXTURE_PATH).exists()
            ? new Texture(Gdx.files.internal(WINDOW_TEXTURE_PATH))
            : null;
        portraitTexture = Gdx.files.internal(PORTRAIT_TEXTURE_PATH).exists()
            ? new Texture(Gdx.files.internal(PORTRAIT_TEXTURE_PATH))
            : null;
    }

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void draw(SpriteBatch batch, Texture pixel, FitViewport viewport, PlayerData playerData) {
        if (!visible || playerData == null) {
            return;
        }

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float x = camera.position.x - viewport.getWorldWidth() * 0.5f + (viewport.getWorldWidth() - WINDOW_WIDTH) * 0.5f;
        float y = camera.position.y - viewport.getWorldHeight() * 0.5f + (viewport.getWorldHeight() - WINDOW_HEIGHT) * 0.5f;

        drawPanel(batch, pixel, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);

        BitmapFont titleFont = DarkRomanceGame.skin.getFont("GuildensternSmall");
        BitmapFont bodyFont = DarkRomanceGame.skin.getFont("default-font");
        float originalTitleScaleX = titleFont.getData().scaleX;
        float originalTitleScaleY = titleFont.getData().scaleY;
        titleFont.getData().setScale(FIELD_FONT_SCALE);

        titleFont.setColor(Color.valueOf("513729FF"));
        bodyFont.setColor(Color.valueOf("5A3F2FFF"));

        drawPortrait(batch, pixel, x + PORTRAIT_X, y + PORTRAIT_Y, PORTRAIT_SIZE);

        float textX = x + CONTENT_X;
        float textY = y + WINDOW_HEIGHT - CONTENT_TOP_OFFSET;
        drawFieldLine(batch, titleFont, "Name", playerData.getUsername(), textX, textY);
        drawFieldLine(batch, titleFont, "Level", String.valueOf(playerData.getLevel()), textX, textY - LINE_GAP);
        float healthY = textY - LINE_GAP * 2f;
        titleFont.draw(batch, "Health", textX, healthY);
        drawFieldLine(batch, titleFont, "Score", String.valueOf(playerData.getScore()), textX, textY - LINE_GAP * 3f);

        drawHealthBar(batch, pixel, textX + HEALTH_LABEL_WIDTH, healthY - BAR_HEIGHT + 1f, BAR_WIDTH, BAR_HEIGHT, playerData);

        glyphLayout.setText(bodyFont, "Press C to close");
        bodyFont.draw(batch, glyphLayout, x + WINDOW_WIDTH - FOOTER_RIGHT_PADDING - glyphLayout.width, y + 44f);
        titleFont.getData().setScale(originalTitleScaleX, originalTitleScaleY);
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

    private void drawFieldLine(SpriteBatch batch, BitmapFont font, String label, String value, float x, float y) {
        font.draw(batch, label, x, y);
        font.setColor(Color.valueOf("3F2A1EFF"));
        font.draw(batch, value, x + 120f, y);
        font.setColor(Color.valueOf("513729FF"));
    }

    private void drawPortrait(SpriteBatch batch, Texture pixel, float x, float y, float size) {
        batch.setColor(Color.valueOf("4A3125AA"));
        batch.draw(pixel, x - 6f, y - 6f, size + 12f, size + 12f);
        batch.setColor(Color.valueOf("6D4A37FF"));
        batch.draw(pixel, x - 2f, y - 2f, size + 4f, size + 4f);
        batch.setColor(Color.valueOf("E1D0B6FF"));
        batch.draw(pixel, x, y, size, size);
        batch.setColor(Color.WHITE);

        if (portraitTexture != null) {
            batch.draw(portraitTexture, x + 6f, y + 6f, size - 12f, size - 12f);
        }
    }

    private void drawHealthBar(SpriteBatch batch, Texture pixel, float x, float y, float width, float height, PlayerData playerData) {
        float ratio = playerData.getMaxHealth() == 0 ? 0f : playerData.getHealth() / (float) playerData.getMaxHealth();
        float clampedRatio = MathUtils.clamp(ratio, 0f, 1f);

        batch.setColor(Color.valueOf("6A4A37FF"));
        batch.draw(pixel, x, y, width, height);

        batch.setColor(Color.valueOf("3B140FFF"));
        batch.draw(pixel, x + 2f, y + 2f, width - 4f, height - 4f);

        batch.setColor(Color.valueOf("7B141CFF"));
        batch.draw(pixel, x + 3f, y + 3f, Math.max(0f, (width - 6f) * clampedRatio), height - 6f);

        batch.setColor(Color.valueOf("B93A3DCC"));
        batch.draw(pixel, x + 3f, y + height * 0.5f, Math.max(0f, (width - 6f) * clampedRatio), height * 0.22f);

        batch.setColor(Color.valueOf("4C2F22FF"));
        batch.draw(pixel, x, y + height, width, 2f);
        batch.draw(pixel, x, y - 2f, width, 2f);
        batch.draw(pixel, x - 2f, y - 2f, 2f, height + 4f);
        batch.draw(pixel, x + width, y - 2f, 2f, height + 4f);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (windowTexture != null) {
            windowTexture.dispose();
        }
        if (portraitTexture != null) {
            portraitTexture.dispose();
        }
    }
}
