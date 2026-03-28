package com.team7.game1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.team7.game1.screens.LoginScreen;

public class DarkRomanceGame extends Game {
    public static Skin skin;

    @Override
    public void create() {
        // Загружаем стандартный скин VisUI
        VisUI.load();
        skin = VisUI.getSkin();   // получаем загруженный скин

        setScreen(new LoginScreen(this));
    }

    @Override
    public void dispose() {
        VisUI.dispose();   // обязательно освобождаем ресурсы
        super.dispose();
    }
}
