package com.github.arrivedbog593.borderlesswindow.visual;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.network.chat.Component;

/**
 * Holds the four fog toggles, mirroring the role FpsOverlayState plays
 * for the FPS overlay. Both UIs (the vanilla config screen and the
 * Sodium video settings integration) read and write through this class,
 * and FogHandler consults it every time the game sets up fog.
 * <p>
 * true = fog enabled (vanilla behavior, the default); false = that fog
 * is removed. Every setter persists immediately to
 * config/borderlesswindow.json. init() is the only entry that does NOT
 * save: it is called once at startup by
 * BorderlessHandler.initializeFromConfig() with the values just loaded
 * from disk.
 * <p>
 * TERRAIN FOG START (OptiFine's "Fog Start"): where the ordinary distance
 * fog begins, as a percentage of the render distance. Vanilla starts it
 * at the render distance minus a tenth of it (clamped to 4..64 blocks),
 * which is 87.5-90% at every distance, so the fog only ever covers the
 * last stretch. {@link #DEFAULT_START} keeps that; anything else moves
 * the start while the far edge stays at the render distance. Capped at
 * {@link #MAX_START}: a start AT the far edge would make the fog band
 * zero blocks wide, and the shader divides by that width. Slider, so it
 * splits apply from persist like CloudState. See FogRendererMixin.
 */
public final class FogState {

    /** Sentinel: vanilla's own fog start. */
    public static final int DEFAULT_START = 0;
    public static final int MAX_START = 95;
    public static final int START_STEP = 1;

    private static boolean terrainFog = true;
    private static boolean waterFog = true;
    private static boolean lavaFog = true;
    private static boolean powderSnowFog = true;
    private static int terrainStart = DEFAULT_START;

    private FogState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(boolean loadedTerrain, boolean loadedWater,
                            boolean loadedLava, boolean loadedPowderSnow,
                            int loadedTerrainStart) {
        terrainFog = loadedTerrain;
        waterFog = loadedWater;
        lavaFog = loadedLava;
        powderSnowFog = loadedPowderSnow;
        terrainStart = sanitizeStart(loadedTerrainStart);
    }

    public static int getTerrainStart() {
        return terrainStart;
    }

    /** Memory only -- the slider persists on release. */
    public static void applyTerrainStart(int percent) {
        terrainStart = sanitizeStart(percent);
    }

    public static void setTerrainStart(int percent) {
        applyTerrainStart(percent);
        persist();
    }

    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * "Default (~90%)" or a percentage, shared by both UIs. The default
     * names its value because it sits at the left end of the slider, as
     * every "Default" slot in the mod does, while the start it stands for
     * is near the right end.
     */
    public static Component formatStart(int percent) {
        return percent <= DEFAULT_START
                ? Component.translatable("borderlesswindow.options.fog_terrain_start.default")
                : Component.translatable("borderlesswindow.options.distortion.percent", percent);
    }

    private static int sanitizeStart(int value) {
        return Math.clamp(value, DEFAULT_START, MAX_START);
    }

    public static boolean isTerrainFogEnabled() {
        return terrainFog;
    }

    public static void setTerrainFogEnabled(boolean enabled) {
        terrainFog = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isWaterFogEnabled() {
        return waterFog;
    }

    public static void setWaterFogEnabled(boolean enabled) {
        waterFog = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isLavaFogEnabled() {
        return lavaFog;
    }

    public static void setLavaFogEnabled(boolean enabled) {
        lavaFog = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isPowderSnowFogEnabled() {
        return powderSnowFog;
    }

    public static void setPowderSnowFogEnabled(boolean enabled) {
        powderSnowFog = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}