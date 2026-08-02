package com.github.arrivedbog593.borderlesswindow.enchantment;

/**
 * How enchantment level numbers are rendered next to the enchantment
 * name ("Looting III" vs "Looting 3").
 * <p>
 * Vanilla only ships translation keys for enchantment.level.1 through
 * enchantment.level.10. Above that the raw key leaks into the tooltip
 * ("Looting enchantment.level.11"), which is a plain bug -- levels up
 * to 255 are legal (Enchantment.MAX_LEVEL), and datapacks or commands
 * reach them easily.
 * <p>
 * - MIXED (default): vanilla behavior wherever vanilla has an answer,
 *   plain numbers only where it would otherwise show a broken key. This
 *   is the "fix the bug, change nothing else" option.
 * - ROMAN: generated Roman numerals at every level (XI, XII, ... CCLV).
 *   Consistent look, but hard to read at high levels.
 * - ARABIC: plain numbers at every level, including 1-10.
 * <p>
 * MIXED defers to the language file whenever a key exists, so resource
 * packs that DO define high levels keep working. ROMAN and ARABIC are
 * explicit user choices, so they override the language file on purpose.
 */
public enum EnchantmentLevelStyle {
    MIXED("borderlesswindow.enchantment_level_style.mixed"),
    ROMAN("borderlesswindow.enchantment_level_style.roman"),
    ARABIC("borderlesswindow.enchantment_level_style.arabic");

    private final String translationKey;

    EnchantmentLevelStyle(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}