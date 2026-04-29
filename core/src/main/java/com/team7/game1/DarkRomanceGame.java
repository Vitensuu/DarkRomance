package com.team7.game1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.team7.game1.models.PlayerData;
import com.team7.game1.screens.SplashScreen;

public class DarkRomanceGame extends Game {

    public static final int DESIGN_WIDTH = 1235;
    public static final int DESIGN_HEIGHT = 775;
    public static Skin skin;

    private static final String FONT_PATH = "fonts/GuildensternNbp.ttf";
    private static final String CYRILLIC_CHARS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    private static boolean resizingWindow;

    private PlayerData currentPlayerData;

    @Override
    public void create() {
        VisUI.load();
        skin = VisUI.getSkin();
        registerCustomFonts();
        setScreen(new SplashScreen(this));
    }

    public PlayerData getCurrentPlayerData() {
        return currentPlayerData;
    }

    public void setCurrentPlayerData(PlayerData currentPlayerData) {
        this.currentPlayerData = currentPlayerData;
    }

    public static void enforceDesktopAspect(int width, int height) {
        if (resizingWindow || Gdx.app.getType() != Application.ApplicationType.Desktop) {
            return;
        }
        if (width <= 0 || height <= 0) {
            return;
        }

        float targetRatio = (float) DESIGN_WIDTH / DESIGN_HEIGHT;
        int newWidth = width;
        int newHeight = height;

        int heightFromWidth = Math.round(width / targetRatio);
        int widthFromHeight = Math.round(height * targetRatio);
        if (Math.abs(widthFromHeight - width) < Math.abs(heightFromWidth - height)) {
            newWidth = widthFromHeight;
        } else {
            newHeight = heightFromWidth;
        }

        if (Math.abs(newWidth - width) <= 2 && Math.abs(newHeight - height) <= 2) {
            return;
        }

        resizingWindow = true;
        Gdx.graphics.setWindowedMode(newWidth, newHeight);
        resizingWindow = false;
    }

    @Override
    public void dispose() {
        VisUI.dispose();
        super.dispose();
    }

    private void registerCustomFonts() {
        String fontChars = FreeTypeFontGenerator.DEFAULT_CHARS + CYRILLIC_CHARS;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
        registerFont(generator, "GuildensternTitle", 90, Color.valueOf("B0B6A5"), fontChars);
        registerFont(generator, "GuildensternTitleGreen", 90, Color.valueOf("074804"), fontChars);
        registerFont(generator, "GuildensternTitleRed", 90, Color.valueOf("420202"), fontChars);
        registerFont(generator, "GuildensternTitleShadow", 90, Color.BLACK, fontChars);
        registerFont(generator, "GuildensternSmall", 64, Color.valueOf("B0B6A5"), fontChars);
        registerFont(generator, "GuildensternSmallShadow", 64, Color.BLACK, fontChars);
        generator.dispose();
    }

    private void registerFont(FreeTypeFontGenerator generator,
                              String name,
                              int size,
                              Color color,
                              String characters) {
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = size;
        params.characters = characters;
        params.color = color;

        BitmapFont font = generator.generateFont(params);
        skin.add(name, font, BitmapFont.class);
    }
}
