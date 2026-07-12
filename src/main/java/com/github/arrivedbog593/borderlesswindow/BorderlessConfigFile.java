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
        String f11_mode = F11Mode.BORDERLESS.name();
    }

    public record LoadedConfig(ScreenMode screenMode, F11Mode f11Mode) {
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
                parseScreenModeOrDefault(data.screen_mode),
                parseF11ModeOrDefault(data.f11_mode));
    }

    public static void save(ScreenMode screenMode, F11Mode f11Mode) {
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

    private static ScreenMode parseScreenModeOrDefault(String value) {
        if (value == null) {
            return ScreenMode.WINDOWED;
        }
        try {
            return ScreenMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ScreenMode.WINDOWED;
        }
    }

    private static F11Mode parseF11ModeOrDefault(String value) {
        if (value == null) {
            return F11Mode.BORDERLESS;
        }
        try {
            return F11Mode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return F11Mode.BORDERLESS;
        }
    }
}