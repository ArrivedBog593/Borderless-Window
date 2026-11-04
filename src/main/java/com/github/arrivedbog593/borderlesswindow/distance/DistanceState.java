package com.github.arrivedbog593.borderlesswindow.distance;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.network.chat.Component;

/**
 * Holds the maximum draw distances for dropped items, experience orbs,
 * armor stands and particles. EntityRenderDispatcherMixin and
 * LevelRendererMixin consult it.
 * <p>
 * DRAW, NOT EXIST. Every one of these only stops something from being
 * drawn (or, for particles, from being spawned on this client). The
 * entities are still there: items can be picked up, orbs still fly to the
 * player, and nothing changes for the server or other players.
 * <p>
 * WHAT VANILLA ALREADY DOES, verified against 1.21.1. Entity.
 * shouldRenderAtSqrDistance draws an entity up to its bounding box size x
 * 64 x the Entity Distance option: 16 blocks for an item (0.25), 32 for
 * an orb (0.5). ArmorStand overrides it with a far larger reach, so its
 * real limit is the 10-chunk tracking range, about 160 blocks. Particles
 * are refused past 32 blocks in LevelRenderer.addParticleInternal unless
 * they are forced. These caps can only LOWER those numbers; above
 * vanilla's own distance a cap simply does nothing.
 * <p>
 * SCALE: {@link #DEFAULT} (0) means no cap -- vanilla behavior, and the
 * leftmost "Default" slot of every slider, like the cloud height. Any
 * other value is a distance in blocks.
 * <p>
 * Split apply/persist like DistortionState: these are sliders, and
 * AbstractSliderButton fires applyValue on every step of a drag.
 */
public final class DistanceState {

    /** Sentinel meaning "no cap, vanilla distance". */
    public static final int DEFAULT = 0;

    public static final int ITEM_MAX = 64;
    public static final int ITEM_STEP = 4;

    public static final int ORB_MAX = 64;
    public static final int ORB_STEP = 4;

    public static final int ARMOR_STAND_MAX = 160;
    public static final int ARMOR_STAND_STEP = 8;

    /** Vanilla's own particle limit; there is nothing to cap above it. */
    public static final int PARTICLE_MAX = 32;
    public static final int PARTICLE_STEP = 4;

    private static int items = DEFAULT;
    private static int orbs = DEFAULT;
    private static int armorStands = DEFAULT;
    private static int particles = DEFAULT;

    private DistanceState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(int loadedItems, int loadedOrbs,
                            int loadedArmorStands, int loadedParticles) {
        items = sanitize(loadedItems, ITEM_MAX);
        orbs = sanitize(loadedOrbs, ORB_MAX);
        armorStands = sanitize(loadedArmorStands, ARMOR_STAND_MAX);
        particles = sanitize(loadedParticles, PARTICLE_MAX);
    }

    public static int getItems() {
        return items;
    }

    public static int getOrbs() {
        return orbs;
    }

    public static int getArmorStands() {
        return armorStands;
    }

    public static int getParticles() {
        return particles;
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyItems(int blocks) {
        items = sanitize(blocks, ITEM_MAX);
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyOrbs(int blocks) {
        orbs = sanitize(blocks, ORB_MAX);
    }

    /** Memory only -- the change shows on the next frame. */
    public static void applyArmorStands(int blocks) {
        armorStands = sanitize(blocks, ARMOR_STAND_MAX);
    }

    /** Memory only -- applies to particles spawned from now on. */
    public static void applyParticles(int blocks) {
        particles = sanitize(blocks, PARTICLE_MAX);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setItems(int blocks) {
        applyItems(blocks);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setOrbs(int blocks) {
        applyOrbs(blocks);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setArmorStands(int blocks) {
        applyArmorStands(blocks);
        persist();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setParticles(int blocks) {
        applyParticles(blocks);
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
     * True when {@code distanceSqr} is past the cap. Always false on
     * {@link #DEFAULT}, so callers can pass any of the getters straight
     * in.
     */
    public static boolean isBeyond(int cap, double distanceSqr) {
        return cap != DEFAULT && distanceSqr > (double) cap * cap;
    }

    /**
     * Slider label shared by both UIs: "Default" on the leftmost slot
     * (the cloud height's key, same meaning), "N blocks" elsewhere.
     */
    public static Component format(int blocks) {
        return blocks <= DEFAULT
                ? Component.translatable("borderlesswindow.options.cloud_height.default")
                : Component.translatable("borderlesswindow.options.distance.blocks", blocks);
    }

    /** Anything at or below DEFAULT is DEFAULT; the rest is clamped. */
    private static int sanitize(int value, int max) {
        if (value <= DEFAULT) {
            return DEFAULT;
        }
        return Math.min(value, max);
    }
}
