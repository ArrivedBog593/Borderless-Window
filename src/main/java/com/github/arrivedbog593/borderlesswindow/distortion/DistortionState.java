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
 * by 0.0125 per tick, Nausea by 0.006667, and neither leaves it decaying
 * by 0.05. There is no per-source value to read, so the source is
 * attributed once per tick and stored in lastSourceWasPortal:
 * <p>
 * - LocalPlayerMixin reads the rate vanilla picked. That rate IS the
 *   branch that ran, so the portal wins over Nausea when both apply,
 *   exactly as in vanilla's if/else.
 * <p>
 * - EntityMixin records any contact with a Nether portal, because the
 *   rate misses one case: arriving through a portal. The player lands
 *   inside the destination portal on cooldown, vanilla skips its portal
 *   branch, and the rate says Nausea while the player is still standing
 *   in the portal. Contact counts as the portal.
 * <p>
 * - While decaying (or idle) the last attribution is kept, so the
 *   fade-out uses the percentage of whatever caused it.
 * <p>
 * Asking hasEffect(CONFUSION) at render time, the first approach, got
 * the overlap backwards; see LocalPlayerMixin for the approaches that
 * failed and why.
 * <p>
 * THE GREEN NAUSEA TEXTURE follows the Nausea slider on purpose. Vanilla
 * draws it under Nausea whenever Distortion Effects is below 100%, at
 * intensity (1 - scale): it is the static cue that replaces the spin as
 * the spin is turned down. GameRendererMixin folds the Nausea factor into
 * the scale that texture reads, so lowering the Nausea slider trades
 * spin for the green cue the same way Minecraft's own slider does --
 * at 0% there is no spin and the texture is at full strength. While the
 * portal is the source, the texture reads vanilla's scale untouched:
 * the Nausea slider does not speak for the portal.
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

    /**
     * Set by EntityMixin whenever the player touches a Nether portal, even
     * while vanilla ignores it because of the portal cooldown. Consumed
     * once per tick by LocalPlayerMixin. Client thread only.
     */
    private static boolean touchingPortal = false;

    public static void markTouchingPortal() {
        touchingPortal = true;
    }

    /** Returns whether a portal was touched since the last call, and clears it. */
    public static boolean consumeTouchingPortal() {
        boolean touched = touchingPortal;
        touchingPortal = false;
        return touched;
    }

}