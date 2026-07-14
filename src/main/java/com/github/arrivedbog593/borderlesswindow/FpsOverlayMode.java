package com.github.arrivedbog593.borderlesswindow;

/**
 * How much information the FPS overlay shows:
 * - OFF: overlay disabled (default -- existing users see no change).
 * - SIMPLE: only the current FPS ("144 FPS").
 * - EXTENDED: current FPS plus the average FPS and the 1% low FPS
 *   computed over the last 15 seconds of frames.
 */
public enum FpsOverlayMode {
    OFF("borderlesswindow.fps_overlay_mode.off"),
    SIMPLE("borderlesswindow.fps_overlay_mode.simple"),
    EXTENDED("borderlesswindow.fps_overlay_mode.extended");

    private final String translationKey;

    FpsOverlayMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
