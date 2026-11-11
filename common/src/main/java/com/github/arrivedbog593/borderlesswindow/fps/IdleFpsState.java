package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

/**
 * Holds the idle (AFK) FPS caps: after {@link #getDelayMinutes()} minutes
 * with no keyboard or mouse input, MinecraftMixin caps the frame rate at
 * {@link #getLimit()} -- even while the window is focused, which is what
 * sets it apart from UnfocusedFpsState. The next input lifts it at once.
 * <p>
 * TWO STAGES. The second delay and limit add a later, usually lower cap
 * (e.g. 30 FPS after 5 min, then 10 FPS after 20 min). The second stage
 * only exists on top of the first: it does nothing while the first delay
 * is {@link #OFF}, and its delay must be LONGER than the first one. Both
 * delays count from the same last input. When both apply, the lower cap
 * wins -- a second limit above the first changes nothing.
 * <p>
 * Raising the first delay to or past the second turns the second stage
 * off, the same thing Sodium does with an option whose value falls out of
 * its range (it resets it to the default). Turning the first stage off
 * keeps the second delay, ready for when it comes back.
 * <p>
 * INPUT is anything that reaches MouseHandler (move, button, scroll) or
 * KeyboardHandler.keyPress, recorded by MouseHandlerMixin and
 * KeyboardHandlerMixin, plus the window regaining focus (MinecraftMixin).
 * Typing always arrives through
 * keyPress too, so charTyped needs no hook of its own. Gamepad input from
 * controller mods does not go through either handler and does not count.
 * <p>
 * SCALE: the delays are in minutes, {@link #OFF} (0, the default) meaning
 * no cap -- vanilla behavior. The caps share UnfocusedFpsState's floor of
 * 5 FPS, for the same reason: under 2 FPS the client falls behind on
 * ticks.
 * <p>
 * Split apply/persist like UnfocusedFpsState: all four are sliders.
 */
public final class IdleFpsState {

    /** Sentinel meaning "no idle cap" (either delay). */
    public static final int OFF = 0;

    /** {@link #getCurrentCap()} when no idle cap applies. */
    public static final int NO_CAP = Integer.MAX_VALUE;

    public static final int MAX_DELAY_MINUTES = 30;
    public static final int MAX_SECOND_DELAY_MINUTES = 60;
    public static final int DEFAULT_LIMIT = 30;
    public static final int DEFAULT_SECOND_LIMIT = 10;

    private static int delayMinutes = OFF;
    private static int limit = DEFAULT_LIMIT;
    private static int secondDelayMinutes = OFF;
    private static int secondLimit = DEFAULT_SECOND_LIMIT;

    private static long lastInputMillis = Util.getMillis();

    private IdleFpsState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedDelayMinutes, int loadedLimit,
                            int loadedSecondDelayMinutes, int loadedSecondLimit) {
        delayMinutes = sanitizeDelay(loadedDelayMinutes);
        limit = sanitizeLimit(loadedLimit);
        secondDelayMinutes = sanitizeSecondDelay(loadedSecondDelayMinutes);
        secondLimit = sanitizeLimit(loadedSecondLimit);
        dropSecondStageIfNotLater();
    }

    public static int getDelayMinutes() {
        return delayMinutes;
    }

    public static int getLimit() {
        return limit;
    }

    public static int getSecondDelayMinutes() {
        return secondDelayMinutes;
    }

    public static int getSecondLimit() {
        return secondLimit;
    }

    /** Records user activity. Called from the input callbacks on the render thread. */
    public static void markInput() {
        lastInputMillis = Util.getMillis();
    }

    /**
     * The idle cap for this frame: the lower of the stages whose delay has
     * passed, or {@link #NO_CAP} when none has (or the feature is off).
     */
    public static int getCurrentCap() {
        if (delayMinutes == OFF) {
            return NO_CAP;
        }
        long idleMillis = Util.getMillis() - lastInputMillis;
        if (idleMillis < delayMinutes * 60_000L) {
            return NO_CAP;
        }
        if (secondDelayMinutes != OFF && idleMillis >= secondDelayMinutes * 60_000L) {
            return Math.min(limit, secondLimit);
        }
        return limit;
    }

    /**
     * SECOND-DELAY SLIDER. It starts at the first delay, so it can never
     * go below it, and that leftmost slot stands for {@link #OFF} -- the
     * same trick as UnfocusedFpsState's "Off" slot, except that here the
     * slot moves with the first delay. With the first stage off it is 0.
     * These two translate for the no-Sodium screen; the Sodium page does
     * the same through its own validator, SecondIdleDelayRange.
     */
    public static int toSecondDelaySlot(int secondDelay, int firstDelayMinutes) {
        return Math.max(secondDelay, firstDelayMinutes);
    }

    /** Slider position -> stored second delay. See toSecondDelaySlot. */
    public static int fromSecondDelaySlot(int slot, int firstDelayMinutes) {
        return slot <= firstDelayMinutes ? OFF : slot;
    }

    /** Stored delay -> label, shared by both UIs and both delays. */
    public static Component formatDelay(int minutes) {
        return minutes <= OFF
                ? Component.translatable("borderlesswindow.options.distortion.off")
                : Component.translatable("borderlesswindow.options.idle_fps_delay.minutes", minutes);
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applyDelayMinutes(int minutes) {
        delayMinutes = sanitizeDelay(minutes);
        dropSecondStageIfNotLater();
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applyLimit(int newLimit) {
        limit = sanitizeLimit(newLimit);
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applySecondDelayMinutes(int minutes) {
        secondDelayMinutes = sanitizeSecondDelay(minutes);
        dropSecondStageIfNotLater();
    }

    /** Memory only -- the change applies on the next frame. */
    public static void applySecondLimit(int newLimit) {
        secondLimit = sanitizeLimit(newLimit);
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

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setSecondDelayMinutes(int minutes) {
        applySecondDelayMinutes(minutes);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setSecondLimit(int newLimit) {
        applySecondLimit(newLimit);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /** See the class javadoc: a second delay not past the first is off. */
    private static void dropSecondStageIfNotLater() {
        if (delayMinutes != OFF && secondDelayMinutes <= delayMinutes) {
            secondDelayMinutes = OFF;
        }
    }

    private static int sanitizeDelay(int value) {
        return Math.clamp(value, OFF, MAX_DELAY_MINUTES);
    }

    private static int sanitizeSecondDelay(int value) {
        return Math.clamp(value, OFF, MAX_SECOND_DELAY_MINUTES);
    }

    /** No "off" slot here: turning a stage off is its delay's job. */
    private static int sanitizeLimit(int value) {
        return Math.clamp(value, UnfocusedFpsState.MIN_LIMIT, UnfocusedFpsState.MAX_LIMIT);
    }
}
