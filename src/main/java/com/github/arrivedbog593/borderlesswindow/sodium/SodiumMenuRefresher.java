package com.github.arrivedbog593.borderlesswindow.sodium;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Refreshes the values shown in Sodium's video settings menu when the
 * screen mode changes from outside the menu (F11) WHILE the menu is open.
 * Sodium already re-reads all bindings when the screen is OPENED, but it
 * has no live refresh -- this helper covers that gap.
 * <p>
 * It uses reflection because ConfigManager/Config are Sodium internal
 * classes (not present in the API jar we compile against). It is
 * deliberately hardened:
 * - This class does NOT import anything from Sodium, so it loads fine
 *   even when Sodium is not installed.
 * - It only acts if the current screen IS Sodium's video settings menu.
 * - Any reflection failure (Sodium absent, future refactor of its
 *   internals) is silently ignored: the worst that can happen is no live
 *   refresh, same as before this fix existed.
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
            // Sodium absent or internals changed: no live refresh, but
            // nothing breaks.
        }
    }
}