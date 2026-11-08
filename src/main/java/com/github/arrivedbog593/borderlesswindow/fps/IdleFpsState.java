package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.Util;

/**
 * Holds the idle (AFK) FPS cap: after {@link #getDelayMinutes()} minutes
 * with no keyboard or mouse input, MinecraftMixin caps the frame rate at
 * {@link #getLimit()} -- even while the window is focused, which is what
 * sets it apart from UnfocusedFpsState. The next input lifts it at once.
 * <p>
 * INPUT is anything that reaches MouseHandler (move, button, scroll) or
 * KeyboardHandler.keyPress, recorded by InputActivityMixin, plus the
 * window regaining focus (MinecraftMixin). Typing always arrives through
 * keyPress too, so charTyped needs no hook of its own. Gamepad input from
 * controller mods does not go through either handler and does not count.
 * <p>
 * SCALE: the delay is in minutes, {@link #OFF} (0, the default) meaning
 * no idle cap -- vanilla behavior. The cap shares UnfocusedFpsState's
 * floor of 5 FPS, for the same reason: under 2 FPS the client falls
 * behind on ticks.
 * <p>
 * Split apply/persist like UnfocusedFpsState: both are sliders.
 */
public final class IdleFpsState {

    /** Sentinel meaning "no idle cap". */
    public static final int OFF = 0;

    public static final int MAX_DELAY_MINUTES = 30;
    public static final int DEFAULT_LIMIT = 30;

    private static int delayMinutes = OFF;
    private static int limit = DEFAULT_LIMIT;

    private static long lastInputMillis = Util.getMillis();

    private IdleFpsState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedDelayMinutes, int loadedLimit) {
        delayMinutes = sanitizeDelay(loadedDelayMinutes);
        limit = sanitizeLimit(loadedLimit);
    }

    public static int getDelayMinutes() {
        return delayMinutes;
    }

    public static int getLimit() {
        return limit;
    }

    /** Records user activity. Called from the input callbacks on the render thread. */
    public static void markInput() {
        lastInputMillis = Util.getMillis();
    }

    /** Whether the idle cap applies this frame. */
    public static boolean isIdle() {
        if (delayMinutes == OFF) {
            return false;
        }
        return Util.getMillis() - lastInputMillis >= delayMinutes * 60_000L;
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applyDelayMinutes(int minutes) {
        delayMinutes = sanitizeDelay(minutes);
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applyLimit(int newLimit) {
        limit = sanitizeLimit(newLimit);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setDelayMinutes(int minutes) {
        applyDelayMinutes(minutes);
        persist();
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

    private static int sanitizeDelay(int value) {
        return Math.clamp(value, OFF, MAX_DELAY_MINUTES);
    }

    /** No "off" slot here: turning the feature off is the delay's job. */
    private static int sanitizeLimit(int value) {
        return Math.clamp(value, UnfocusedFpsState.MIN_LIMIT, UnfocusedFpsState.MAX_LIMIT);
    }
}
