package com.github.arrivedbog593.borderlesswindow.cloud;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the custom cloud height, mirroring the role FogState/FpsOverlayState
 * play for their features. Both UIs (the vanilla config screen and the
 * Sodium video settings integration) read and write through this class,
 * and CloudHeightMixin consults it every time the game asks a dimension
 * for its cloud height.
 * <p>
 * SENTINEL: {@link #DEFAULT_HEIGHT} means "don't override" -- the mixin
 * leaves the dimension's own cloud height untouched. Only an explicit
 * value in [MIN_HEIGHT, MAX_HEIGHT] overrides it. This keeps custom
 * dimensions from other mods (and vanilla's NaN "no clouds" signal for
 * the Nether/End) working normally until the user actually picks a height.
 * <p>
 * The slider is presented so its leftmost position is DEFAULT_HEIGHT
 * (shown as "Default"), and 64..320 above that are real heights.
 * <p>
 * WHY THIS HOLDER SPLITS APPLY FROM SAVE (unlike FogState/FpsOverlayState):
 * this is the first setting driven by a SLIDER instead of a cycle button.
 * Vanilla's AbstractSliderButton calls applyValue() on every value change
 * while dragging, not once on release -- with ~258 selectable steps, a
 * single drag across the widget would fire the setter (and therefore a
 * full config write) close to two hundred times. So the height is applied
 * in memory during the drag, which gives a live preview of the cloud
 * layer, and {@link #persist()} is called once when the drag ends.
 * {@link #setHeight(int)} still does both, so any caller that changes the
 * value in one discrete step (the Sodium storage flush) keeps working
 * exactly like the other holders.
 * <p>
 * init() is the only entry that neither saves nor needs a persist(): it
 * is called once at startup by BorderlessHandler.initializeFromConfig()
 * with the value just loaded from disk.
 */
public final class CloudState {

    /**
     * Vanilla Overworld cloud height. When the slider lands exactly on
     * this value, both UIs label it (e.g. "192 (Regular)") so the user
     * knows which height matches unmodified Minecraft.
     */
    public static final int VANILLA_HEIGHT = 192;

    /** Sentinel meaning "use the dimension's own height, don't override". */
    public static final int DEFAULT_HEIGHT = -1;

    /** Lowest selectable custom height (world build floor is -64, but
     *  clouds below the surface make no sense, so 64 is the ground). */
    public static final int MIN_HEIGHT = 64;

    /** Highest selectable custom height (world build ceiling in 1.21.1). */
    public static final int MAX_HEIGHT = 320;

    private static int height = DEFAULT_HEIGHT;

    private CloudState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(int loadedHeight) {
        height = sanitize(loadedHeight);
    }

    /**
     * Returns the configured height, or {@link #DEFAULT_HEIGHT} if the
     * user hasn't overridden it. The mixin checks for the sentinel.
     */
    public static int getHeight() {
        return height;
    }

    /**
     * Applies a height in memory WITHOUT touching the config file. The
     * change is visible on the very next frame (the mixin reads this
     * holder), which is what makes dragging the slider preview live.
     * <p>
     * Callers that use this MUST call {@link #persist()} once the value
     * has settled, or the change is lost on the next game start.
     */
    public static void applyHeight(int newHeight) {
        height = sanitize(newHeight);
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state, so an extra call
     * with nothing changed is a harmless no-op write.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setHeight(int newHeight) {
        applyHeight(newHeight);
        persist();
    }

    /** True when a real override is active (not the sentinel). */
    public static boolean isOverridden() {
        return height != DEFAULT_HEIGHT;
    }

    /**
     * Clamps to the valid range, but leaves the sentinel untouched. Any
     * out-of-range value that isn't the sentinel is coerced into range so
     * a hand-edited or corrupt config can never inject a wild height.
     */
    private static int sanitize(int value) {
        if (value == DEFAULT_HEIGHT) {
            return DEFAULT_HEIGHT;
        }
        if (value < MIN_HEIGHT) {
            return MIN_HEIGHT;
        }
        return Math.min(value, MAX_HEIGHT);
    }
}