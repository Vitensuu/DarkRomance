package com.team7.game1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.kotcrab.vis.ui.VisUI;
import com.team7.game1.models.PlayerData;
import com.team7.game1.screens.SplashScreen;

public class DarkRomanceGame extends Game {
    public static final int DESIGN_WIDTH = 1235;
    public static final int DESIGN_HEIGHT = 775;
    public static Skin skin;
    private static boolean resizingWindow;
    private PlayerData currentPlayerData;

    @Override
    public void create() {
        VisUI.load();
        skin = VisUI.getSkin();
        registerCustomFonts();

        setScreen(new SplashScreen(this));
    }

    private void registerCustomFonts() {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/GuildensternNbp.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleFontParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleFontParams.size = 90;
        titleFontParams.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        titleFontParams.color = Color.valueOf("B0B6A5");

        FreeTypeFontGenerator.FreeTypeFontParameter smallFontParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        smallFontParams.size = 64;
        smallFontParams.characters = titleFontParams.characters;
        smallFontParams.color = Color.valueOf("B0B6A5");

        FreeTypeFontGenerator.FreeTypeFontParameter titleGreenParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleGreenParams.size = titleFontParams.size;
        titleGreenParams.characters = titleFontParams.characters;
        titleGreenParams.color = Color.valueOf("074804");

        FreeTypeFontGenerator.FreeTypeFontParameter titleRedParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleRedParams.size = titleFontParams.size;
        titleRedParams.characters = titleFontParams.characters;
        titleRedParams.color = Color.valueOf("420202");

        FreeTypeFontGenerator.FreeTypeFontParameter titleShadowParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleShadowParams.size = titleFontParams.size;
        titleShadowParams.characters = titleFontParams.characters;
        titleShadowParams.color = Color.BLACK;

        FreeTypeFontGenerator.FreeTypeFontParameter smallShadowParams =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        smallShadowParams.size = smallFontParams.size;
        smallShadowParams.characters = titleFontParams.characters;
        smallShadowParams.color = Color.BLACK;

        BitmapFont titleFont = generator.generateFont(titleFontParams);
        BitmapFont titleGreenFont = generator.generateFont(titleGreenParams);
        BitmapFont titleRedFont = generator.generateFont(titleRedParams);
        BitmapFont titleShadowFont = generator.generateFont(titleShadowParams);
        BitmapFont smallFont = generator.generateFont(smallFontParams);
        BitmapFont smallShadowFont = generator.generateFont(smallShadowParams);
        generator.dispose();

        skin.add("GuildensternTitle", titleFont, BitmapFont.class);
        skin.add("GuildensternTitleGreen", titleGreenFont, BitmapFont.class);
        skin.add("GuildensternTitleRed", titleRedFont, BitmapFont.class);
        skin.add("GuildensternTitleShadow", titleShadowFont, BitmapFont.class);
        skin.add("GuildensternSmall", smallFont, BitmapFont.class);
        skin.add("GuildensternSmallShadow", smallShadowFont, BitmapFont.class);
    }

    public PlayerData getCurrentPlayerData() {
        return currentPlayerData;
    }

    public void setCurrentPlayerData(PlayerData currentPlayerData) {
        this.currentPlayerData = currentPlayerData;
    }

    public static void enforceDesktopAspect(int width, int height) {
        // Защита от рекурсии: если мы уже внутри изменения размера, не делаем ничего
        if (resizingWindow || Gdx.app.getType() != Application.ApplicationType.Desktop) {
            return;
        }
        if (width <= 0 || height <= 0) {
            return;
        }

        float targetRatio = (float) DESIGN_WIDTH / DESIGN_HEIGHT;
        int newWidth = width;
        int newHeight = height;

        // Какое измерение подгонять? Выбираем то, которое даст наименьшее отклонение от желаемого размера
        int heightFromWidth = Math.round(width / targetRatio);
        int widthFromHeight = Math.round(height * targetRatio);

        // Если изменение ширины даёт меньшую разницу, меняем ширину, иначе высоту
        if (Math.abs(widthFromHeight - width) < Math.abs(heightFromWidth - height)) {
            newWidth = widthFromHeight;
            // newHeight = height; // оставляем текущую высоту
        } else {
            newHeight = heightFromWidth;
            // newWidth = width; // оставляем текущую ширину
        }

        // Если разница незначительная (1-2 пикселя), не дёргаем окно
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
}
