package com.github.arrivedbog593.borderlesswindow.camera;

/**
 * How the zoom key behaves.
 * <p>
 * - HOLD: zoomed only while the key is held down (default, like
 *   OptiFine). Opening any screen releases every key, so it also ends
 *   the zoom.
 * - TOGGLE: one press zooms in, the next zooms out.
 */
public enum ZoomMode {
    HOLD("borderlesswindow.zoom_mode.hold"),
    TOGGLE("borderlesswindow.zoom_mode.toggle");

    private final String translationKey;

    ZoomMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
