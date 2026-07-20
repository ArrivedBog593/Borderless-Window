package com.github.arrivedbog593.borderlesswindow.fog;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

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
 */
public final class FogState {

    private static boolean terrainFog = true;
    private static boolean waterFog = true;
    private static boolean lavaFog = true;
    private static boolean powderSnowFog = true;

    private FogState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(boolean loadedTerrain, boolean loadedWater,
                            boolean loadedLava, boolean loadedPowderSnow) {
        terrainFog = loadedTerrain;
        waterFog = loadedWater;
        lavaFog = loadedLava;
        powderSnowFog = loadedPowderSnow;
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