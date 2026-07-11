package com.github.arrivedbog593.borderlesswindow;

/**
 * The 3 states the game window can be in. Each state has a translation
 * key resolved against the lang files in assets/borderlesswindow/lang/
 * according to the game's language.
 */
public enum ScreenMode {
    WINDOWED("borderlesswindow.screen_mode.windowed"),
    BORDERLESS("borderlesswindow.screen_mode.borderless"),
    FULLSCREEN("borderlesswindow.screen_mode.fullscreen");

    private final String translationKey;

    ScreenMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}