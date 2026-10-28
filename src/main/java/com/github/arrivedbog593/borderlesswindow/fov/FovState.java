package com.github.arrivedbog593.borderlesswindow.fov;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the four FOV effect percentages, one per source.
 * <p>
 * THIS IS A SPLIT OF VANILLA'S SLIDER, not a replacement. Minecraft's
 * "FOV Effects" option applies to all four sources, it just does so as
 * one number. Finding where took some digging: it appears nowhere in
 * AbstractClientPlayer.getFieldOfViewModifier, and only once in
 * GameRenderer.getFov (the fluid squeeze). The other application lives
 * in NeoForge's ComputeFovModifierEvent constructor, which replaced
 * vanilla's own lerp when the hook was patched in.
 * <p>
 * That places our three injections BEFORE vanilla's slider: they scale
 * each source inside getFieldOfViewModifier, and the slider then scales
 * the combined result on the way out. So all four options sit under it,
 * exactly like the bobbing options sit under View Bobbing -- with FOV
 * Effects at Off, nothing here has any effect. Every tooltip says so.
 * <p>
 * SCALING: each source is a multiplier around 1.0 (flying is 1.1, a
 * fully drawn bow 0.85, and so on), so a percentage scales the DISTANCE
 * from 1.0 rather than the multiplier itself:
 * {@code lerp(factor, 1.0, multiplier)}. At 0% the multiplier becomes
 * exactly 1.0 -- no effect -- and at 100% it is untouched. Linear here,
 * unlike the distortion sliders, because vanilla treats these as
 * straight multipliers with no squaring anywhere.
 * <p>
 * THE SPYGLASS IS DELIBERATELY ABSENT. It also runs through
 * getFieldOfViewModifier, but with an early {@code return 0.1F} that
 * skips everything else, and that number IS the zoom rather than a
 * cosmetic pull. Making it adjustable would change how far a spyglass
 * lets you see, which is a gameplay change and not a comfort option.
 * <p>
 * Split apply/persist like CloudState and DistortionState, for the same
 * reason: sliders fire applyValue on every step of a drag.
 */
public final class FovState {

    public static final int MIN_PERCENT = 0;
    public static final int MAX_PERCENT = 100;
    public static final int DEFAULT_PERCENT = 100;

    private static int speedPercent = DEFAULT_PERCENT;
    private static int flyingPercent = DEFAULT_PERCENT;
    private static int bowPercent = DEFAULT_PERCENT;
    private static int fluidPercent = DEFAULT_PERCENT;

    private FovState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedSpeed, int loadedFlying,
                            int loadedBow, int loadedFluid) {
        speedPercent = sanitize(loadedSpeed);
        flyingPercent = sanitize(loadedFlying);
        bowPercent = sanitize(loadedBow);
        fluidPercent = sanitize(loadedFluid);
    }

    public static int getSpeedPercent() {
        return speedPercent;
    }

    public static int getFlyingPercent() {
        return flyingPercent;
    }

    public static int getBowPercent() {
        return bowPercent;
    }

    public static int getFluidPercent() {
        return fluidPercent;
    }

    public static void applySpeedPercent(int percent) {
        speedPercent = sanitize(percent);
    }

    public static void applyFlyingPercent(int percent) {
        flyingPercent = sanitize(percent);
    }

    public static void applyBowPercent(int percent) {
        bowPercent = sanitize(percent);
    }

    public static void applyFluidPercent(int percent) {
        fluidPercent = sanitize(percent);
    }

    public static void setSpeedPercent(int percent) {
        applySpeedPercent(percent);
        persist();
    }

    public static void setFlyingPercent(int percent) {
        applyFlyingPercent(percent);
        persist();
    }

    public static void setBowPercent(int percent) {
        applyBowPercent(percent);
        persist();
    }

    public static void setFluidPercent(int percent) {
        applyFluidPercent(percent);
        persist();
    }

    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    public static float getSpeedFactor() {
        return speedPercent / 100.0F;
    }

    public static float getFlyingFactor() {
        return flyingPercent / 100.0F;
    }

    public static float getBowFactor() {
        return bowPercent / 100.0F;
    }

    public static float getFluidFactor() {
        return fluidPercent / 100.0F;
    }

    private static int sanitize(int value) {
        if (value < MIN_PERCENT) {
            return MIN_PERCENT;
        }
        return Math.min(value, MAX_PERCENT);
    }
}