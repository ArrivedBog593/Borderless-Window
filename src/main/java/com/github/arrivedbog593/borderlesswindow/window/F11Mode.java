package com.github.arrivedbog593.borderlesswindow.window;

/**
 * What the F11 key does. BORDERLESS and FULLSCREEN toggle between
 * Windowed and that mode; CYCLE steps through all 3 screen modes
 * (Windowed -> Borderless -> Fullscreen -> Windowed).
 * <p>
 * Note: the BORDERLESS/FULLSCREEN constant names intentionally match
 * ScreenMode's, so config files written by older versions of the mod
 * (which stored a ScreenMode name in "f11_mode") keep parsing without
 * any migration.
 */
public enum F11Mode {
    BORDERLESS("borderlesswindow.f11_mode.borderless"),
    FULLSCREEN("borderlesswindow.f11_mode.fullscreen"),
    CYCLE("borderlesswindow.f11_mode.cycle");

    private final String translationKey;

    F11Mode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}