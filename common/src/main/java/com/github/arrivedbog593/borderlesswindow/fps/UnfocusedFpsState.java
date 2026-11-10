package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the FPS cap applied while the game window is not focused (alt-tabbed,
 * minimized, or another window in front). FramerateLimitTrackerMixin
 * consults it every frame through getFramerateLimit().
 * <p>
 * WHY VANILLA'S OPTION IS NOT ENOUGH. Minecraft's Inactivity FPS Limit
 * (1.21.2+) only throttles a minimized window and, in its AFK mode, a
 * minute without input. A window that is merely out of focus -- alt-tabbed
 * behind a browser, on a second monitor -- still renders at full speed,
 * burning GPU and power for frames nobody looks at.
 * <p>
 * SCALE: {@link #OFF} (0, the default) means no cap -- vanilla behavior.
 * Any other value is a cap in [MIN_LIMIT, MAX_LIMIT], moved in steps of
 * {@link #STEP}. The cap can only lower the frame rate: if the regular Max
 * Framerate is already below it, the regular limit wins.
 * <p>
 * DYNAMIC FPS hooks the same method (verified against 3.11.4 for NeoForge
 * 1.21.1; on 1.21.2+ the method moved to FramerateLimitTracker). With both installed the caps simply stack -- whichever is
 * lower wins -- and this option stays available on purpose: it keeps the
 * setting in Sodium's video menu, where Dynamic FPS's own needs Cloth
 * Config and the mod list. The same goes for every background option.
 * <p>
 * WHY NOT BELOW 5. runTick runs at most 10 client ticks per frame and drops
 * the rest, so under 2 FPS the client falls behind the 20 ticks per second
 * the game expects. 5 keeps a comfortable margin.
 * <p>
 * SLIDER SLOTS. Both UIs move in steps of 1, and a contiguous range from
 * {@link #OFF} would offer 1..4. Instead the slider starts at
 * {@link #OFF_SLOT} (MIN_LIMIT - 1), labelled "Off", and
 * {@link #toSlot}/{@link #fromSlot} translate -- the same trick as the
 * cloud height's "Default" slot. The stored value stays {@link #OFF}, so
 * existing configs keep working.
 * <p>
 * Split apply/persist like DistortionState, for the same reason: it is a
 * slider, and AbstractSliderButton fires applyValue on every step of a drag.
 */
public final class UnfocusedFpsState {

    /** Sentinel meaning "no cap while unfocused". */
    public static final int OFF = 0;

    public static final int MIN_LIMIT = 5;
    public static final int MAX_LIMIT = 60;
    public static final int STEP = 1;

    /** The slider position that stands for {@link #OFF}. */
    public static final int OFF_SLOT = MIN_LIMIT - 1;

    private static int limit = OFF;

    private UnfocusedFpsState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(int loadedLimit) {
        limit = sanitize(loadedLimit);
    }

    /** The cap, or {@link #OFF}. */
    public static int getLimit() {
        return limit;
    }

    /** Whether the mixin should cap anything at all. */
    public static boolean isEnabled() {
        return limit != OFF;
    }

    /** Stored value -> slider position. */
    public static int toSlot(int value) {
        return value == OFF ? OFF_SLOT : value;
    }

    /** Slider position -> stored value. */
    public static int fromSlot(int slot) {
        return slot <= OFF_SLOT ? OFF : slot;
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applyLimit(int newLimit) {
        limit = sanitize(newLimit);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setLimit(int newLimit) {
        applyLimit(newLimit);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * Anything at or below OFF is OFF; everything else is clamped into range
     * so a hand-edited config can never inject a 0 FPS cap (which would
     * divide by zero in RenderSystem.limitDisplayFPS) or a pointless one.
     */
    private static int sanitize(int value) {
        if (value <= OFF) {
            return OFF;
        }
        if (value < MIN_LIMIT) {
            return MIN_LIMIT;
        }
        return Math.min(value, MAX_LIMIT);
    }
}
