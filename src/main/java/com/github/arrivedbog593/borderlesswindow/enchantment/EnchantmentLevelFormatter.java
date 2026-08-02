package com.github.arrivedbog593.borderlesswindow.enchantment;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Decides what text replaces vanilla's "enchantment.level.N" component.
 * <p>
 * All the logic lives here instead of inside EnchantmentMixin on
 * purpose: the mixin stays a three-line delegation, and this class can
 * be read, reasoned about and changed without touching bytecode
 * injection at all.
 * <p>
 * format() returns null to mean "let vanilla handle it", which is what
 * the mixin turns into the original Operation call. Returning null is
 * strictly better than rebuilding the translatable ourselves: it keeps
 * the component identical to vanilla's, including anything another mod
 * may have already done to it.
 */
public final class EnchantmentLevelFormatter {

    /** Highest level vanilla ships a translation key for. */
    private static final int VANILLA_MAX_KEY = 10;

    /** Roman numeral generation is only defined for 1..3999. */
    private static final int ROMAN_MAX = 3999;

    private static final int[] ROMAN_VALUES =
            {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS =
            {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private EnchantmentLevelFormatter() {
    }

    /**
     * @param translationKey the key vanilla was about to translate
     *                       ("enchantment.level.N")
     * @param level          the enchantment level being rendered
     * @return the replacement component, or null to keep vanilla's
     */
    public static @Nullable MutableComponent format(String translationKey, int level) {
        return switch (EnchantmentLevelState.getStyle()) {
            // Only step in where vanilla would show a raw key. Resource
            // packs and mods that DO define high levels win over us:
            // fixing a missing translation must not overwrite an
            // existing one.
            case MIXED -> (level > VANILLA_MAX_KEY && !I18n.exists(translationKey))
                    ? Component.literal(Integer.toString(level))
                    : null;

            // Explicit user choices: always generated, so the style is
            // consistent regardless of what the language file says.
            case ROMAN -> Component.literal(toRoman(level));
            case ARABIC -> Component.literal(Integer.toString(level));
        };
    }

    /**
     * Roman numeral for a level, falling back to the plain number
     * outside the representable range. Level 0 and negatives are
     * reachable through NBT editing and malformed datapacks, so they
     * must not hit the loop below.
     */
    private static String toRoman(int level) {
        if (level <= 0 || level > ROMAN_MAX) {
            return Integer.toString(level);
        }

        StringBuilder builder = new StringBuilder();
        int remaining = level;
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (remaining >= ROMAN_VALUES[i]) {
                builder.append(ROMAN_SYMBOLS[i]);
                remaining -= ROMAN_VALUES[i];
            }
        }
        return builder.toString();
    }
}