package com.github.arrivedbog593.borderlesswindow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal persistence in config/borderlesswindow.json. Stores the current
 * screen mode and the F11 mode so they can be restored at game startup.
 * <p>
 * Deliberately fault-tolerant: if the file is missing, corrupt, or has
 * invalid values, the defaults are used (Windowed / F11 -> Borderless)
 * without crashing.
 */
public final class BorderlessConfigFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("borderlesswindow.json");

    /** JSON structure. Gson serializes/deserializes this directly. */
    private static class Data {
        String screen_mode = ScreenMode.WINDOWED.name();
        String f11_mode = ScreenMode.BORDERLESS.name();
    }

    public record LoadedConfig(ScreenMode screenMode, ScreenMode f11Mode) {
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
                parseOrDefault(data.screen_mode, ScreenMode.WINDOWED),
                parseOrDefault(data.f11_mode, ScreenMode.BORDERLESS));
    }

    public static void save(ScreenMode screenMode, ScreenMode f11Mode) {
        Data data = new Data();
        data.screen_mode = screenMode.name();
        data.f11_mode = f11Mode.name();
        try {
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException ignored) {
            // Not critical: if saving fails, the mode simply won't be
            // remembered next time. Better that than crashing.
        }
    }

    private static ScreenMode parseOrDefault(String value, ScreenMode fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return ScreenMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}