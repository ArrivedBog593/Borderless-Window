package com.github.arrivedbog593.borderlesswindow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal persistence in config/borderlesswindow.json. Stores the current
 * screen mode, the F11 mode, and the FPS overlay settings so they can be
 * restored at game startup.
 * <p>
 * Deliberately fault-tolerant: if the file is missing, corrupt, or has
 * invalid or absent values, the defaults are used (Windowed / F11 ->
 * Borderless / FPS overlay off, top-left) without crashing. Configs
 * written by older versions of the mod simply lack the newer fields and
 * fall back to those defaults -- no migration needed.
 */
public final class BorderlessConfigFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("borderlesswindow.json");

    /** JSON structure. Gson serializes/deserializes this directly. */
    private static class Data {
        String screen_mode = ScreenMode.WINDOWED.name();
        String f11_mode = F11Mode.BORDERLESS.name();
        String fps_overlay_mode = FpsOverlayMode.OFF.name();
        String fps_overlay_position = FpsOverlayPosition.TOP_LEFT.name();
    }

    public record LoadedConfig(ScreenMode screenMode, F11Mode f11Mode,
                               FpsOverlayMode fpsOverlayMode,
                               FpsOverlayPosition fpsOverlayPosition) {
    }

    private BorderlessConfigFile() {
    }

    public static LoadedConfig load() {
        Data data = new Data();
        if (Files.exists(PATH)) {
            try {
                data = GSON.fromJson(Files.readString(PATH), Data.class);
                if (data == null) {
                    data = new Data();
                }
            } catch (Exception ignored) {
                data = new Data();
            }
        }
        return new LoadedConfig(
                parseOrDefault(ScreenMode.class, data.screen_mode, ScreenMode.WINDOWED),
                parseOrDefault(F11Mode.class, data.f11_mode, F11Mode.BORDERLESS),
                parseOrDefault(FpsOverlayMode.class, data.fps_overlay_mode, FpsOverlayMode.OFF),
                parseOrDefault(FpsOverlayPosition.class, data.fps_overlay_position,
                        FpsOverlayPosition.TOP_LEFT));
    }

    /**
     * Persists the CURRENT live state of every setting, read from its
     * owner (BorderlessHandler for window state, FpsOverlayState for the
     * overlay). Having a single gathering point means new settings can't
     * be accidentally clobbered by a save triggered from an unrelated
     * option, which is what would happen if each caller passed only the
     * values it knows about.
     */
    public static void saveCurrent() {
        Data data = new Data();
        data.screen_mode = BorderlessHandler.getCurrentMode().name();
        data.f11_mode = BorderlessHandler.getF11Target().name();
        data.fps_overlay_mode = FpsOverlayState.getMode().name();
        data.fps_overlay_position = FpsOverlayState.getPosition().name();
        try {
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException ignored) {
            // Not critical: if saving fails, the settings simply won't be
            // remembered next time. Better that than crashing.
        }
    }

    private static <T extends Enum<T>> T parseOrDefault(Class<T> type, String value, T fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
