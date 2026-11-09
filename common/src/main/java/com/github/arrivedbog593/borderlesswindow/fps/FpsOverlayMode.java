package com.github.arrivedbog593.borderlesswindow.fps;

/**
 * How much information the FPS overlay shows:
 * - OFF: overlay disabled (default -- existing users see no change).
 * - SIMPLE: only the current FPS ("144 FPS").
 * - EXTENDED: current FPS plus the min / avg / max over the last 15
 *   seconds.
 * <p>
 * Those three are derived from one-second samples of vanilla's own FPS
 * counter, so "min" is the lowest one-second average in the window --
 * NOT a 1% low percentile, despite what earlier versions of this
 * comment (and the tooltip) claimed. Counters that do compute
 * percentiles will therefore report a different number. See the
 * MEASUREMENT APPROACH note in FpsOverlay for why sampling beats
 * timing frames here, and why the raw minimum is already
 * hitch-resistant.
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