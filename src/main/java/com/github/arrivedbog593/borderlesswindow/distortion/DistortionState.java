package com.github.arrivedbog593.borderlesswindow.distortion;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the two distortion percentages, splitting what vanilla exposes
 * as a single "Distortion Effects" slider into its two sources: the
 * Nether portal and the Nausea effect.
 * <p>
 * WHY A SPLIT IS POSSIBLE AT ALL. LocalPlayer keeps ONE accumulator,
 * spinningEffectIntensity, and handleConfusionTransitionEffect feeds it
 * from either source through an if/else chain -- the portal ramps it up
 * by 0.0125 per tick, Nausea by 0.0066, and neither leaves it decaying
 * by 0.05. So there is no per-source value to read: the source has to
 * be attributed at render time, by asking whether the player currently
 * has the Nausea effect.
 * <p>
 * Vanilla does exactly the same thing one line further down in
 * GameRenderer.renderLevel, where it picks the spin speed with
 * "hasEffect(CONFUSION) ? 7 : 20". We are matching an existing pattern,
 * not inventing one.
 * <p>
 * KNOWN AMBIGUITY: standing in a portal WHILE under Nausea attributes
 * the whole thing to Nausea, so the Nausea percentage applies even
 * though the portal is what is driving the intensity. Vanilla's spin
 * speed has the identical ambiguity in the identical situation, so
 * this does not introduce a new failure mode -- but it is real, and it
 * is why neither slider can promise to be exact in that overlap.
 * <p>
 * SCALE: percentages, 0..100, default 100 (vanilla). The mixin squares
 * the factor before applying it, because vanilla squares its own scale
 * (f2 * f2) -- matching the curve means 50% here feels like 50% on
 * Minecraft's slider instead of feeling twice as strong.
 * <p>
 * HIERARCHY: both of these multiply vanilla's slider rather than
 * replacing it. With Distortion Effects at 0% in Minecraft's
 * accessibility settings nothing spins at all and neither of these does
 * anything, the same way the bobbing options sit under View Bobbing.
 * Both tooltips say so.
 * <p>
 * Split apply/persist like CloudState, for the same reason: these are
 * sliders, and AbstractSliderButton fires applyValue on every step of a
 * drag. Applying in memory gives a live preview; persist() runs once on
 * release.
 */
public final class DistortionState {

    public static final int MIN_PERCENT = 0;
    public static final int MAX_PERCENT = 100;
    public static final int DEFAULT_PERCENT = 100;

    private static int portalPercent = DEFAULT_PERCENT;
    private static int nauseaPercent = DEFAULT_PERCENT;

    private DistortionState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedPortal, int loadedNausea) {
        portalPercent = sanitize(loadedPortal);
        nauseaPercent = sanitize(loadedNausea);
    }

    public static int getPortalPercent() {
        return portalPercent;
    }

    public static int getNauseaPercent() {
        return nauseaPercent;
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyPortalPercent(int percent) {
        portalPercent = sanitize(percent);
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyNauseaPercent(int percent) {
        nauseaPercent = sanitize(percent);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setPortalPercent(int percent) {
        applyPortalPercent(percent);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setNauseaPercent(int percent) {
        applyNauseaPercent(percent);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /** 0..100 as a 0..1 factor. The mixin squares it. */
    public static float getPortalFactor() {
        return portalPercent / 100.0F;
    }

    /** 0..100 as a 0..1 factor. The mixin squares it. */
    public static float getNauseaFactor() {
        return nauseaPercent / 100.0F;
    }

    private static int sanitize(int value) {
        if (value < MIN_PERCENT) {
            return MIN_PERCENT;
        }
        return Math.min(value, MAX_PERCENT);
    }

    /**
     * Which source last fed spinningEffectIntensity, recorded by
     * LocalPlayerMixin from inside vanilla's own if/else instead of being
     * guessed at render time.
     * <p>
     * Guessing was the first approach and it was wrong: the render side
     * can only ask getActivePortalLocalTransition(), but vanilla's portal
     * branch also requires portalProcess.isInsidePortalThisTick(), which
     * is not reachable from outside. The transition stays set for a few
     * ticks after you leave a portal, so a player with Nausea who stepped
     * through one kept getting the portal percentage for as long as the
     * potion lasted.
     * </p>
     * Render thread only, so no synchronisation needed.
     */
    private static boolean lastSourceWasPortal = false;

    public static void setLastSourceWasPortal(boolean portal) {
        lastSourceWasPortal = portal;
    }

    public static float getActiveFactor() {
        return lastSourceWasPortal ? getPortalFactor() : getNauseaFactor();
    }

    public static boolean isLastSourcePortal() {
        return lastSourceWasPortal;
    }

}