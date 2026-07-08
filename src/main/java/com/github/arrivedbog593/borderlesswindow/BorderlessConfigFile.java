package com.github.arrivedbog593.borderlesswindow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistencia minima en config/borderlesswindow.json. Guarda el modo de
 * pantalla actual y el modo de F11 para restaurarlos al abrir el juego.
 * <p>
 * Es deliberadamente tolerante a errores: si el archivo no existe, esta
 * corrupto, o tiene valores invalidos, se usan los defaults (Ventana /
 * F11 -> Sin bordes) sin crashear.
 */
public final class BorderlessConfigFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("borderlesswindow.json");

    /** Estructura del JSON. Gson serializa/deserializa esto directo. */
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
            // No es critico: si falla el guardado, simplemente no se
            // recordara el modo la proxima vez. Mejor eso que crashear.
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