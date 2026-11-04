package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.neoforged.fml.ModList;

/**
 * Holds the FPS cap applied while the game window is not focused (alt-tabbed,
 * minimized, or another window in front). MinecraftMixin consults it every
 * frame through getFramerateLimit().
 * <p>
 * WHY 1.21.1 NEEDS THIS. Minecraft 1.21.2 added its own Inactivity FPS Limit;
 * 1.21.1 renders at full speed in the background, burning GPU and power for
 * frames nobody sees. Drop this feature when porting to 1.21.2 or later --
 * vanilla's option covers it.
 * <p>
 * SCALE: {@link #OFF} (0, the default) means no cap -- vanilla behavior.
 * Any other value is a cap in [MIN_LIMIT, MAX_LIMIT], moved in steps of
 * {@link #STEP}. The cap can only lower the frame rate: if the regular Max
 * Framerate is already below it, the regular limit wins.
 * <p>
 * Split apply/persist like DistortionState, for the same reason: it is a
 * slider, and AbstractSliderButton fires applyValue on every step of a drag.
 */
public final class UnfocusedFpsState {

    /** Sentinel meaning "no cap while unfocused". */
    public static final int OFF = 0;

    public static final int MIN_LIMIT = 5;
    public static final int MAX_LIMIT = 60;
    public static final int STEP = 5;

    /**
     * Dynamic FPS does this and more (idle detection, stopping rendering
     * while minimized, muting) and hooks the same two methods: its own
     * mixins sit on Minecraft.getFramerateLimit and
     * Window.getFramerateLimit (verified against 3.11.4 for NeoForge
     * 1.21.1). Both caps stacking would not crash -- the lower one would
     * win -- but two settings fighting over one behavior is confusing, so
     * with Dynamic FPS installed this option steps aside entirely and both
     * UIs grey it out.
     */
    private static final String DYNAMIC_FPS_ID = "dynamic_fps";

    private static int limit = OFF;

    /** Resolved on first use: ModList is complete by the first frame. */
    private static Boolean deferred;

    private UnfocusedFpsState() {
    }

    /** True when another mod owns this behavior and ours must stay out. */
    public static boolean isDeferredToOtherMod() {
        if (deferred == null) {
            deferred = ModList.get().isLoaded(DYNAMIC_FPS_ID);
        }
        return deferred;
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
        return limit != OFF && !isDeferredToOtherMod();
    }

    /** The tooltip to show: the regular one, or why the option is greyed out. */
    public static String getTooltipKey() {
        return isDeferredToOtherMod()
                ? "borderlesswindow.options.unfocused_fps_limit.tooltip_dynamic_fps"
                : "borderlesswindow.options.unfocused_fps_limit.tooltip";
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
