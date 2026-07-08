package com.github.arrivedbog593.borderlesswindow;

/**
 * Los 3 estados que puede tener la ventana. Cada estado tiene una clave
 * de traduccion que se resuelve contra los lang files en
 * assets/borderlesswindow/lang/ segun el idioma del juego.
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