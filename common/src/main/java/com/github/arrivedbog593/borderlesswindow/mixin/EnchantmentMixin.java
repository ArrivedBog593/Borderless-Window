package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelFormatter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes enchantment levels above X and lets the player choose the
 * numeral style.
 * <p>
 * Vanilla builds the level suffix in Enchantment.getFullname with
 * Component.translatable("enchantment.level." + level), but only ships
 * keys for levels 1-10. Levels up to 255 are legal
 * (Enchantment.MAX_LEVEL, and EnchantmentDefinition's codec caps
 * max_level at 255), so anything above 10 renders the raw key:
 * "Looting enchantment.level.11". Shipping one lang key per level is
 * not a fix, it is 245 keys per language chasing a number that a
 * datapack can pick freely.
 * <p>
 * Wrapping the translatable call is the smallest possible intervention:
 * <p>
 * - The guard above it (level != 1 || getMaxLevel() != 1) is untouched,
 *   so single-level enchantments like Mending still show no number.
 * - Returning the original Operation for the default style means the
 *   component vanilla produces is the component that ships, byte for
 *   byte, including any styling applied later.
 * - getFullname is the one place every level number comes from, so item
 *   tooltips, enchanted books and the enchanting table are all covered
 *   by this single injection.
 * <p>
 * The Holder and int parameters after Operation are the target method's
 * own arguments, captured by MixinExtras. Parsing the level back out of
 * the translation key string would work too, and would break the first
 * time anyone changed that key.
 * <p>
 * Note on packages: WrapOperation lives in injector.wrapoperation,
 * which is NOT the injector.v2 path that WrapWithCondition needs.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @WrapOperation(
            method = "getFullname",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;"
                            + "translatable(Ljava/lang/String;)"
                            + "Lnet/minecraft/network/chat/MutableComponent;"))
    private static MutableComponent borderlesswindow$levelNumeral(
            String translationKey,
            Operation<MutableComponent> original,
            Holder<Enchantment> enchantment,
            int level) {

        MutableComponent replacement =
                EnchantmentLevelFormatter.format(translationKey, level);
        return replacement != null ? replacement : original.call(translationKey);
    }
}