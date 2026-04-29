package com.team7.game1;

import com.badlogic.gdx.graphics.Color;

public final class GameConfig {

    private GameConfig() {
    }

    public static final class Network {
        public static final String SERVER_HOST = "172.20.10.2";
        public static final int SERVER_PORT = 12345;

        private Network() {
        }
    }

    public static final class Save {
        public static final String DIRECTORY = "saves";
        public static final String FILE_SUFFIX = ".json";

        private Save() {
        }
    }

    public static final class World {
        public static final String DEFAULT_MAP_PATH = "maps/Main_map.tmx";
        public static final float MAP_SCALE = 3.2f;

        private World() {
        }
    }

    public static final class Dialog {
        public static final String DATA_PATH = "ui/dialog/dialogues.json";
        public static final String DEFAULT_DIALOGUE_ID = "default";

        public static final String FRAME_TEXTURE_PATH = "ui/dialog/UI_Flat_Frame01a.png";
        public static final String NAMEPLATE_TEXTURE_PATH = "ui/dialog/UI_Flat_FrameMarker01a.png";
        public static final String NEXT_BUTTON_TEXTURE_PATH = "ui/dialog/UI_Flat_Button02a_1.png";

        public static final float X = 90f;
        public static final float Y = 20f;
        public static final float WIDTH = DarkRomanceGame.DESIGN_WIDTH - 180f;
        public static final float HEIGHT = 210f;
        public static final float PADDING = 26f;

        public static final float PORTRAIT_SIZE = 120f;
        public static final float NEXT_BUTTON_WIDTH = 140f;
        public static final float NEXT_BUTTON_HEIGHT = 42f;
        public static final float NAMEPLATE_HEIGHT = 34f;

        public static final Color FALLBACK_PANEL_BG = Color.valueOf("15110FCC");
        public static final Color FALLBACK_PANEL_BORDER = Color.valueOf("BBA58DFF");
        public static final Color PORTRAIT_BG = Color.valueOf("D8DDE5FF");
        public static final Color PORTRAIT_BORDER = Color.valueOf("53606FFF");
        public static final Color TITLE = Color.valueOf("284357FF");
        public static final Color TITLE_SHADOW = Color.valueOf("E9EDF2FF");
        public static final Color BODY_TEXT = Color.valueOf("2B2623FF");
        public static final Color FALLBACK_NAMEPLATE_BG = Color.valueOf("D6DDE5FF");
        public static final Color FALLBACK_BUTTON_BG = Color.valueOf("F4F0E8FF");

        public static final String HINT_TEXT = "Esc - close   F / Enter / Space - next";

        private Dialog() {
        }
    }

    public static final class Ui {
        public static final String CHARACTER_WINDOW_TEXTURE_PATH = "ui/character/character_window_frame.png";
        public static final String CHARACTER_PORTRAIT_TEXTURE_PATH = "ui/dialog/portraits/archer_portrait.png";

        private Ui() {
        }
    }
}
