package com.team7.game1.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.team7.game1.DarkRomanceGame;

public class SplashScreen implements Screen {

    private static final float BASE_TITLE_FONT_SCALE = 2.222f;
    private static final float START_BLACK_DURATION = 1.05f;
    private static final float RED_FADE_DURATION = 1.75f;
    private static final float TITLE_DURATION = 1.35f;
    private static final float BLACKOUT_DURATION = 0.4f;

    private final DarkRomanceGame game;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private SpriteBatch batch;
    private Viewport viewport;
    private Texture background;
    private Texture overlayPixel;
    private AudioDevice stingAudioDevice;

    private SplashState state;
    private float stateTime;

    private enum SplashState {
        BLACK,
        RED_FADE,
        TITLE,
        BLACKOUT
    }

    public SplashScreen(DarkRomanceGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DarkRomanceGame.DESIGN_WIDTH, DarkRomanceGame.DESIGN_HEIGHT);
        background = new Texture(Gdx.files.internal("backgraund/SplashScreenBackgraund.png"));
        overlayPixel = createSolidTexture(Color.valueOf("FFFFFFFF"));
        stingAudioDevice = Gdx.audio.newAudioDevice(44100, true);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        switchState(SplashState.BLACK, false);
    }

    @Override
    public void render(float delta) {
        updateSequence(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawCurrentState();
        batch.end();
    }

    private void updateSequence(float delta) {
        stateTime += delta;

        switch (state) {
            case BLACK:
                if (stateTime >= START_BLACK_DURATION) {
                    switchState(SplashState.RED_FADE, true);
                }
                break;
            case RED_FADE:
                if (stateTime >= RED_FADE_DURATION) {
                    switchState(SplashState.TITLE, true);
                }
                break;
            case TITLE:
                if (stateTime >= TITLE_DURATION) {
                    switchState(SplashState.BLACKOUT, false);
                }
                break;
            case BLACKOUT:
                if (stateTime >= BLACKOUT_DURATION) {
                    game.setScreen(new LoginScreen(game));
                }
                break;
            default:
                break;
        }
    }

    private void switchState(SplashState newState, boolean playSting) {
        state = newState;
        stateTime = 0f;

        if (playSting) {
            playStingSound();
        }
    }

    private void drawCurrentState() {
        float width = DarkRomanceGame.DESIGN_WIDTH;
        float height = DarkRomanceGame.DESIGN_HEIGHT;

        if (state == SplashState.BLACK || state == SplashState.BLACKOUT) {
            drawOverlay(Color.BLACK, 1f, width, height);
            return;
        }

        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, width, height);

        if (state == SplashState.RED_FADE) {
            float progress = Math.min(1f, stateTime / RED_FADE_DURATION);
            drawOverlay(Color.BLACK, 0.18f + progress * 0.2f, width, height);
            drawOverlay(Color.valueOf("420001FF"), progress * 0.25f, width, height);
            return;
        }

        drawOverlay(Color.BLACK, 0.38f, width, height);
        drawOverlay(Color.valueOf("420001FF"), 0.25f, width, height);
        drawTitle(width, height);
    }

    private void drawTitle(float width, float height) {
        BitmapFont greenFont = DarkRomanceGame.skin.getFont("GuildensternTitleGreen");
        BitmapFont redFont = DarkRomanceGame.skin.getFont("GuildensternTitleRed");
        BitmapFont shadowFont = DarkRomanceGame.skin.getFont("GuildensternTitleShadow");
        float originalGreenScaleX = greenFont.getData().scaleX;
        float originalGreenScaleY = greenFont.getData().scaleY;
        float originalRedScaleX = redFont.getData().scaleX;
        float originalRedScaleY = redFont.getData().scaleY;
        float originalShadowScaleX = shadowFont.getData().scaleX;
        float originalShadowScaleY = shadowFont.getData().scaleY;

        float scale = BASE_TITLE_FONT_SCALE * Math.min(
            width / DarkRomanceGame.DESIGN_WIDTH,
            height / DarkRomanceGame.DESIGN_HEIGHT
        );
        greenFont.getData().setScale(scale);
        redFont.getData().setScale(scale);
        shadowFont.getData().setScale(scale);
        glyphLayout.setText(greenFont, "DARK ROMANCE");
        float ratio = Math.min(
            width / DarkRomanceGame.DESIGN_WIDTH,
            height / DarkRomanceGame.DESIGN_HEIGHT
        );

        drawTitleLayer(redFont, shadowFont, 204f * ratio, height - (181f * ratio), ratio);
        drawTitleLayer(greenFont, shadowFont, 197f * ratio, height - (175f * ratio), ratio);

        greenFont.getData().setScale(originalGreenScaleX, originalGreenScaleY);
        redFont.getData().setScale(originalRedScaleX, originalRedScaleY);
        shadowFont.getData().setScale(originalShadowScaleX, originalShadowScaleY);
    }

    private void drawTitleLayer(BitmapFont font, BitmapFont shadowFont, float x, float topY, float ratio) {
        float baselineY = topY - glyphLayout.height;
        float shadowX = 4f * ratio;
        float shadowY = 8f * ratio;

        shadowFont.setColor(0f, 0f, 0f, 0.000005f);
        shadowFont.draw(batch, glyphLayout, x + shadowX, baselineY - shadowY);

        font.draw(batch, glyphLayout, x, baselineY);
        shadowFont.setColor(Color.WHITE);
    }

    private void drawOverlay(Color color, float alpha, float width, float height) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(overlayPixel, 0, 0, width, height);
        batch.setColor(Color.WHITE);
    }

    private void playStingSound() {
        if (stingAudioDevice == null) {
            return;
        }

        new Thread(() -> {
            short[] samples = new short[4410];
            for (int i = 0; i < samples.length; i++) {
                float progress = i / (float) samples.length;
                float envelope = (1f - progress) * (1f - progress);
                double wave = Math.sin(i * 0.58) + Math.sin(i * 1.07) * 0.55;
                double noise = ((i * 37) % 19 - 9) / 9.0;
                samples[i] = (short) ((wave * 0.65 + noise * 0.35) * envelope * 20000);
            }
            synchronized (this) {
                if (stingAudioDevice != null) {
                    stingAudioDevice.writeSamples(samples, 0, samples.length);
                }
            }
        }, "splash-sting").start();
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
        background.dispose();
        overlayPixel.dispose();
        synchronized (this) {
            if (stingAudioDevice != null) {
                stingAudioDevice.dispose();
                stingAudioDevice = null;
            }
        }
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
