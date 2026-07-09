package com.github.arrivedbog593.borderlesswindow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Refresca los valores del menu de video de Sodium cuando el modo de
 * pantalla cambia por fuera del menu (F11) MIENTRAS el menu esta abierto.
 * Sodium ya relee los bindings al ABRIR la pantalla, pero no tiene
 * refresco en vivo -- este helper cubre ese hueco.
 * <p>
 * Usa reflection porque ConfigManager/Config son clases internas de
 * Sodium (no estan en el jar de la API contra el que compilamos).
 * Esta blindado a proposito:
 * - Esta clase NO importa nada de Sodium, asi que carga sin problema
 *   aunque Sodium no este instalado.
 * - Solo actua si la pantalla actual ES el menu de video de Sodium.
 * - Cualquier fallo de reflection (Sodium ausente, refactor futuro de
 *   sus internals) se ignora en silencio: lo peor que pasa es que el
 *   menu no se refresca en vivo, igual que antes de este fix.
 */
public final class SodiumMenuRefresher {

    private static final String SODIUM_VIDEO_SCREEN =
            "net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen";
    private static final String SODIUM_CONFIG_MANAGER =
            "net.caffeinemc.mods.sodium.client.config.ConfigManager";

    private SodiumMenuRefresher() {
    }

    public static void refreshIfSodiumMenuOpen() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !SODIUM_VIDEO_SCREEN.equals(screen.getClass().getName())) {
            return;
        }

        try {
            Class<?> configManager = Class.forName(SODIUM_CONFIG_MANAGER);
            Object config = configManager.getField("CONFIG").get(null);
            if (config != null) {
                config.getClass().getMethod("resetAllOptionsFromBindings").invoke(config);
            }
        } catch (Throwable ignored) {
            // Sodium ausente o internals cambiados: sin refresco en vivo,
            // pero nada se rompe.
        }
    }
}