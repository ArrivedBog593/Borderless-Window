package com.github.arrivedbog593.borderlesswindow.enchantment;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the enchantment level numeral style, mirroring the role
 * FogState plays for the fog toggles. Both UIs (the vanilla config
 * screen and the Sodium video settings integration) read and write
 * through this class, and EnchantmentLevelFormatter consults it every
 * time an enchantment name is built.
 * <p>
 * The setter persists immediately to config/borderlesswindow.json.
 * init() is the only entry that does NOT save: it is called once at
 * startup by BorderlessHandler.initializeFromConfig() with the value
 * just loaded from disk.
 */
public final class EnchantmentLevelState {

    private static EnchantmentLevelStyle style = EnchantmentLevelStyle.MIXED;

    private EnchantmentLevelState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(EnchantmentLevelStyle loadedStyle) {
        style = loadedStyle;
    }

    public static EnchantmentLevelStyle getStyle() {
        return style;
    }

    public static void setStyle(EnchantmentLevelStyle newStyle) {
        style = newStyle;
        BorderlessConfigFile.saveCurrent();
    }
}