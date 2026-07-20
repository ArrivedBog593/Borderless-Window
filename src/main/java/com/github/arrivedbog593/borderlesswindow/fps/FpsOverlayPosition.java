package com.github.arrivedbog593.borderlesswindow.fps;

/**
 * Corner of the screen where the FPS overlay is drawn. The two booleans
 * are consumed by FpsOverlay to compute the anchor: text grows downward
 * from top corners and upward from bottom corners, and is right-aligned
 * on right corners.
 */
public enum FpsOverlayPosition {
    TOP_LEFT("borderlesswindow.fps_overlay_position.top_left", true, true),
    TOP_RIGHT("borderlesswindow.fps_overlay_position.top_right", true, false),
    BOTTOM_LEFT("borderlesswindow.fps_overlay_position.bottom_left", false, true),
    BOTTOM_RIGHT("borderlesswindow.fps_overlay_position.bottom_right", false, false);

    private final String translationKey;
    private final boolean top;
    private final boolean left;

    FpsOverlayPosition(String translationKey, boolean top, boolean left) {
        this.translationKey = translationKey;
        this.top = top;
        this.left = left;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isLeft() {
        return left;
    }
}
