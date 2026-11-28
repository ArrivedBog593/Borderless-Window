package com.github.arrivedbog593.borderlesswindow.visual;

/**
 * How Night Vision behaves in its last 10 seconds.
 * <p>
 * Vanilla (GameRenderer.nightVisionScale) keeps the brightness at full
 * until the effect has 200 ticks left, then makes it pulse between 40%
 * and 100% (0.7 +/- 0.3, a sine) until it runs out. The pulse is a
 * warning, but a flashing screen is uncomfortable to many players.
 * <p>
 * - FLICKER: vanilla, the pulse (default).
 * - FADE: the brightness goes down smoothly over those 10 seconds and
 *   reaches darkness exactly as the effect ends. The warning is kept,
 *   without the flashing.
 * - STEADY: full brightness until the very end.
 */
public enum NightVisionMode {
    FLICKER("borderlesswindow.night_vision_mode.flicker"),
    FADE("borderlesswindow.night_vision_mode.fade"),
    STEADY("borderlesswindow.night_vision_mode.steady");

    private final String translationKey;

    NightVisionMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
